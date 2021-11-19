package com.github.capntrips.vbmetapatcher

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

class SlotState(context: Context, private val vbmeta: File, private val _isRefreshing : MutableStateFlow<Boolean>, private val isImage: Boolean = false) : ViewModel(), SlotStateInterface {
    companion object {
        const val TAG: String = "VbmetaPatcher/SlotState"
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#41
        const val AVB_VBMETA_IMAGE_HEADER_SIZE: Int = 256
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/avbtool.py#771
        const val AVB_VBMETA_IMAGE_BLOCK_SIZE: Int = 4096
        const val MAGIC: String = "AVB0"
        const val AVBTOOL: String = "avbtool"
    }

    override lateinit var patchStatus: PatchStatus
    override lateinit var sha1: String

    override val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    init {
        refresh(context)
    }

    override fun refresh(context: Context) {
        if (!hasMagic() || !hasAvbRelease()) {
            log(context, "Unexpected Format", shouldThrow = true)
        }
        val data = Base64.decode(Shell.su("dd if=$vbmeta bs=1 count=$AVB_VBMETA_IMAGE_HEADER_SIZE status=none | base64 -w 0").exec().out[0], Base64.DEFAULT)
        val buffer = ByteBuffer.wrap(data)

        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#134
        buffer.position(12)
        val authenticationDataBlockSize = buffer.long

        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#136
        buffer.position(20)
        val auxiliaryDataBlockSize = buffer.long

        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#66
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/avbtool.py#2332
        val imageSize = roundToMultiple(AVB_VBMETA_IMAGE_HEADER_SIZE + authenticationDataBlockSize + auxiliaryDataBlockSize, AVB_VBMETA_IMAGE_BLOCK_SIZE)

        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#174
        buffer.position(120)
        patchStatus = if (buffer.int == 3) PatchStatus.Patched else PatchStatus.Stock

        if (patchStatus == PatchStatus.Patched) {
            if (isImage) {
                Shell.su("printf '\\x00' | dd of=$vbmeta bs=1 seek=123 count=1 conv=notrunc status=none").exec()
                patchStatus = PatchStatus.Stock
                sha1 = Shell.su("dd if=$vbmeta bs=1 count=$imageSize status=none | sha1sum | awk '{ print \$1 }'").exec().out[0]
            } else {
                Shell.su("dd if=$vbmeta of=vbmeta.img bs=1 count=$imageSize status=none").exec()
                val file = File(context.filesDir, "vbmeta.img")
                val image = SlotState(context, file, _isRefreshing, isImage = true)
                sha1 = image.sha1
                file.delete()
            }
        } else {
            sha1 = Shell.su("dd if=$vbmeta bs=1 count=$imageSize status=none | sha1sum | awk '{ print \$1 }'").exec().out[0]
        }
    }

    private fun refreshPatchStatus() {
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#174
        patchStatus = if (Shell.su("dd if=$vbmeta bs=1 skip=123 count=1 status=none | xxd -p").exec().out[0] == "03") {
            PatchStatus.Patched
        } else {
            PatchStatus.Stock
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.emit(true)
            block()
            _isRefreshing.emit(false)
        }
    }

    private fun log(context: Context, message: String, shouldThrow: Boolean = false) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, message)
        if (shouldThrow) {
            throw Exception(message)
        }
    }

    override fun patch(context: Context) {
        launch {
            if (setFlags(context, PatchStatus.Patched)) {
                log(context, "${vbmeta.name} patched")
            }
        }
    }

    override fun restore(context: Context) {
        launch {
            if (setFlags(context, PatchStatus.Stock)) {
                log(context, "${vbmeta.name} restored")
            }
        }
    }

    private fun setFlags(context: Context, toStatus: PatchStatus): Boolean {
        if (!hasMagic() || !hasAvbRelease()) {
            log(context, "Unexpected Format", shouldThrow = true)
        }
        return if (Shell.su("blockdev --setrw $vbmeta").exec().isSuccess) {
            val byte = if (toStatus == PatchStatus.Patched) "03" else "00"
            Shell.su("printf '\\x$byte' | dd of=$vbmeta bs=1 seek=123 count=1 conv=notrunc status=none").exec()
            Shell.su("blockdev --setro $vbmeta").exec()
            refreshPatchStatus()
            true
        } else {
            log(context, "Failed to setrw ${vbmeta.name}")
            false
        }
    }

    private fun hasMagic() : Boolean {
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#126
        return Shell.su("dd if=$vbmeta bs=1 count=4 status=none").exec().out[0] == MAGIC
    }

    private fun hasAvbRelease() : Boolean {
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#186
        return Shell.su("dd if=$vbmeta bs=1 skip=128 count=48 status=none").exec().out[0].startsWith(AVBTOOL)
    }

    // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/avbtool.py#203
    @Suppress("SameParameterValue")
    private fun roundToMultiple(number: Long, size: Int): Long {
        val remainder = number % size
        return if (remainder == 0L) number else number + size - remainder
    }
}
