package com.github.capntrips.vbmetapatcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SlotStatePreview constructor(private val _isRefreshing : MutableStateFlow<Boolean>, isActive: Boolean) : ViewModel(), SlotStateInterface {
    override var patchStatus: PatchStatus = if (isActive) PatchStatus.Patched else PatchStatus.Stock
    override var sha1: String = "0a1b2c3d"

    override val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            block()
            _isRefreshing.emit(false)
        }
    }

    override fun refresh(context: Context) {
        launch {
            delay(500)
        }
    }

    override fun patch(context: Context) {
        launch {
            delay(500)
            patchStatus = PatchStatus.Patched
        }
    }

    override fun restore(context: Context) {
        launch {
            delay(500)
            patchStatus = PatchStatus.Stock
        }
    }
}
