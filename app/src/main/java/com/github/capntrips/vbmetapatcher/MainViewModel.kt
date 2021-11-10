package com.github.capntrips.vbmetapatcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel constructor(context: Context) : ViewModel(), MainViewModelInterface {
    private val _isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var _uiState: MutableStateFlow<DeviceStateInterface> = MutableStateFlow(DeviceState(context, _isRefreshing))

    override val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()
    override val uiState: StateFlow<DeviceStateInterface>
        get() = _uiState.asStateFlow()

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.emit(true)
            block()
            _isRefreshing.emit(false)
        }
    }

    override fun refresh(context: Context) {
        launch {
            uiState.value.refresh(context)
        }
    }
}
