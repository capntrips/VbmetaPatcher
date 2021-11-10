package com.github.capntrips.vbmetapatcher

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface SlotStateInterface {
    var patchStatus: PatchStatus
    var sha1: String
    val isRefreshing: StateFlow<Boolean>
    fun refresh(context: Context)
    fun patch(context: Context)
    fun restore(context: Context)
}
