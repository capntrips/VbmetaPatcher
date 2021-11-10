package com.github.capntrips.vbmetapatcher

import android.content.Context
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

class SlotState(context: Context, private val vbmeta: File, private val _isRefreshing : MutableStateFlow<Boolean>, private val isImage: Boolean = false) : ViewModel(), SlotStateInterface {
    companion object {
        const val TAG: String = "VbmetaPatcher/SlotState"
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
        refreshPatchStatus()
        if (patchStatus == PatchStatus.Patched) {
            if (isImage) {
                Shell.su("printf '\\x00' | dd of=$vbmeta bs=1 seek=123 count=1 conv=notrunc status=none").exec()
                patchStatus = PatchStatus.Stock
                sha1 = Shell.su("dd if=$vbmeta bs=1 count=8192 status=none | sha1sum | awk '{ print \$1 }'").exec().out[0]
            } else {
                Shell.su("dd if=$vbmeta of=vbmeta.img bs=1 count=8192 status=none").exec()
                val file = File(context.filesDir, "vbmeta.img")
                val image = SlotState(context, file, _isRefreshing, isImage = true)
                sha1 = image.sha1
                file.delete()
            }
        } else {
            sha1 = Shell.su("dd if=$vbmeta bs=1 count=8192 status=none | sha1sum | awk '{ print \$1 }'").exec().out[0]
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
        return Shell.su("dd if=$vbmeta bs=1 count=4 status=none").exec().out[0] == "AVB0"
    }

    private fun hasAvbRelease() : Boolean {
        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#186
        return Shell.su("dd if=$vbmeta bs=1 skip=128 count=48 status=none").exec().out[0].startsWith("avbtool")
    }
}
