package com.github.capntrips.vbmetapatcher

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel : ViewModel(), MainViewModelInterface {
    companion object {
        private const val TAG = "Vbmeta Patcher"
    }

    private var _isRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var _slotSuffix : String
    private var _vbmetaA : File
    private var _vbmetaB : File
    private var _isPatchedA : MutableStateFlow<Boolean>
    private var _isPatchedB : MutableStateFlow<Boolean>

    override val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    override val slotSuffix: String
        get() = _slotSuffix

    override val isPatchedA: StateFlow<Boolean>
        get() = _isPatchedA.asStateFlow()

    override val isPatchedB: StateFlow<Boolean>
        get() = _isPatchedB.asStateFlow()

    init {
        // https://android.googlesource.com/platform/system/update_engine/+/refs/tags/android-12.0.0_r12/aosp/dynamic_partition_control_android.cc#393
        // https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r12/fs_mgr/fs_mgr_fstab.cpp#416
        // https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r12/fs_mgr/fs_mgr_boot_config.cpp#156
        val hardwarePlatform = Shell.su("getprop ro.boot.hardware.platform").exec().out[0]
        val misc = File(Shell.su("cat /vendor/etc/fstab.$hardwarePlatform | grep /misc | awk '{ print \$1 }'").exec().out[0])
        _vbmetaA = misc.resolveSibling("vbmeta_a")
        _vbmetaB = misc.resolveSibling("vbmeta_b")

        // https://android.googlesource.com/device/google/gs101/+/refs/tags/android-12.0.0_r12/interfaces/boot/1.2/BootControl.cpp#194
        _slotSuffix = Shell.su("getprop ro.boot.slot_suffix").exec().out[0]

        // https://android.googlesource.com/platform/external/avb/+/refs/tags/android-12.0.0_r12/libavb/avb_vbmeta_image.h#177
        _isPatchedA = MutableStateFlow(getFlagsVbmetaA() == "03")
        _isPatchedB = MutableStateFlow(getFlagsVbmetaB() == "03")
    }

    private fun getFlagsVbmetaA() : String {
        return Shell.su("dd if=$_vbmetaA bs=1 skip=123 count=1 status=none | xxd -p").exec().out[0]
    }

    private fun getFlagsVbmetaB() : String {
        return Shell.su("dd if=$_vbmetaB bs=1 skip=123 count=1 status=none | xxd -p").exec().out[0]
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            block()
            _isRefreshing.emit(false)
        }
    }

    @Suppress("FunctionName")
    private fun _refresh() {
        _isPatchedA.value = getFlagsVbmetaA() == "03"
        _isPatchedB.value = getFlagsVbmetaB() == "03"
    }

    override fun refresh() {
        launch {
            _refresh()
        }
    }

    override fun togglePatched() {
        launch {
            // TODO: Compare with current state to avoid performing unexpected actions, if things changed externally
            _refresh()
            if (isPatchedA.value && isPatchedB.value) {
                if (slotSuffix == "_a") {
                    setFlags(
                        partition = _vbmetaA,
                        toPatched = false,
                    )
                } else if (slotSuffix == "_b") {
                    setFlags(
                        partition = _vbmetaB,
                        toPatched = false,
                    )
                }
            } else {
                if (!isPatchedA.value) {
                    setFlags(
                        partition = _vbmetaA,
                        toPatched = true,
                    )
                }
                if (!isPatchedB.value) {
                    setFlags(
                        partition = _vbmetaB,
                        toPatched = true,
                    )
                }
            }
            _refresh()
        }
    }

    private fun setFlags(partition: File, toPatched: Boolean) {
        // TODO: verify magic header
        if (Shell.su("blockdev --setrw $partition").exec().isSuccess) {
            val byte = if (toPatched) "03" else "00"
            Shell.su("printf '\\x$byte' | dd of=$partition bs=1 seek=123 count=1 conv=notrunc status=none").exec()
            Shell.su("blockdev --setro $partition").exec()
        } else {
            Log.e(TAG, "Failed to setrw ${partition.name}")
        }
    }
}
