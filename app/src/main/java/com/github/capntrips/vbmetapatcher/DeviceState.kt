package com.github.capntrips.vbmetapatcher

import android.content.Context
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DeviceState constructor(context: Context, _isRefreshing : MutableStateFlow<Boolean>) : DeviceStateInterface {
    private var _slotA: MutableStateFlow<SlotStateInterface>
    private var _slotB: MutableStateFlow<SlotStateInterface>?
    override val slotSuffix: String?

    override val slotA: StateFlow<SlotStateInterface>
        get() = _slotA.asStateFlow()
    override val slotB: StateFlow<SlotStateInterface>?
        get() = _slotB?.asStateFlow()

    override fun refresh(context: Context) {
        slotA.value.refresh(context)
        if (slotB != null) {
            slotB!!.value.refresh(context)
        }
    }

    init {
        val byName = File("/dev/block/by-name/")

        // https://android.googlesource.com/device/google/gs101/+/refs/tags/android-12.0.0_r12/interfaces/boot/1.2/BootControl.cpp#194
        val slotSuffixResult = Shell.su("getprop ro.boot.slot_suffix").exec()
        @Suppress("LiftReturnOrAssignment")
        if (slotSuffixResult.out.isEmpty() || slotSuffixResult.out[0].isEmpty()) {
            val vbmeta = File(byName, "vbmeta")
            _slotA = MutableStateFlow(SlotState(context, vbmeta, _isRefreshing))
            _slotB = null
            slotSuffix = null
        } else {
            val vbmetaA = File(byName, "vbmeta_a")
            val vbmetaB = File(byName, "vbmeta_b")
            _slotA = MutableStateFlow(SlotState(context, vbmetaA, _isRefreshing))
            _slotB = MutableStateFlow(SlotState(context, vbmetaB, _isRefreshing))
            slotSuffix = slotSuffixResult.out[0]
        }
    }
}
