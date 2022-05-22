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

class DeviceState constructor(context: Context, _isRefreshing : MutableStateFlow<Boolean>) : ViewModel(), DeviceStateInterface {
    companion object {
        const val TAG: String = "VbmetaPatcher/DeviceState"
    }

    private var _slotA: MutableStateFlow<SlotStateInterface>
    private var _slotB: MutableStateFlow<SlotStateInterface>
    override val slotSuffix: String

    override val slotA: StateFlow<SlotStateInterface>
        get() = _slotA.asStateFlow()
    override val slotB: StateFlow<SlotStateInterface>
        get() = _slotB.asStateFlow()

    override fun refresh(context: Context) {
        slotA.value.refresh(context)
        slotB.value.refresh(context)
    }

    init {
        // https://android.googlesource.com/platform/system/update_engine/+/refs/tags/android-12.0.0_r12/aosp/dynamic_partition_control_android.cc#393
        // https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r12/fs_mgr/fs_mgr_fstab.cpp#416
        // https://android.googlesource.com/platform/system/core/+/refs/tags/android-12.0.0_r12/fs_mgr/fs_mgr_boot_config.cpp#156
        val hardwarePlatformResult = Shell.su("getprop ro.boot.hardware.platform").exec()
        if (hardwarePlatformResult.out.isEmpty()) {
            log(context, "Failed to get ro.boot.hardware.platform", shouldThrow = true)
        }
        val hardwarePlatform = hardwarePlatformResult.out[0]
        val miscResult = Shell.su("cat /vendor/etc/fstab.$hardwarePlatform | grep /misc | awk '{ print \$1 }'").exec()
        if (miscResult.out.isEmpty()) {
            log(context, "Failed to find /misc", shouldThrow = true)
        }
        val misc = File(miscResult.out[0])
        val vbmetaA = misc.resolveSibling("vbmeta_a")
        val vbmetaB = misc.resolveSibling("vbmeta_b")

        // https://android.googlesource.com/device/google/gs101/+/refs/tags/android-12.0.0_r12/interfaces/boot/1.2/BootControl.cpp#194
        val slotSuffixResult = Shell.su("getprop ro.boot.slot_suffix").exec()
        if (slotSuffixResult.out.isEmpty()) {
            log(context, "Failed to get ro.boot.slot_suffix", shouldThrow = true)
        }
        slotSuffix = slotSuffixResult.out[0]

        _slotA = MutableStateFlow(SlotState(context, vbmetaA, _isRefreshing))
        _slotB = MutableStateFlow(SlotState(context, vbmetaB, _isRefreshing))
    }

    private fun log(context: Context, message: String, shouldThrow: Boolean = false) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        Log.d(SlotState.TAG, message)
        if (shouldThrow) {
            throw Exception(message)
        }
    }
}
