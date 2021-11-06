package com.github.capntrips.vbmetapatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModelPreview : ViewModel(), MainViewModelInterface {
    private var _isRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var _slotSuffix : String = "_b"
    private var _isPatchedA : MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var _isPatchedB : MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    override val slotSuffix: String
        get() = _slotSuffix

    override val isPatchedA: StateFlow<Boolean>
        get() = _isPatchedA.asStateFlow()

    override val isPatchedB: StateFlow<Boolean>
        get() = _isPatchedB.asStateFlow()

    private fun launch() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            delay(500)
            _isRefreshing.emit(false)
        }
    }

    override fun refresh() {
        launch()
    }

    override fun togglePatched() {
        launch()
        _isPatchedB.value = !isPatchedB.value
    }
}
