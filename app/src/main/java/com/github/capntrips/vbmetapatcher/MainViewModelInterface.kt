package com.github.capntrips.vbmetapatcher

import kotlinx.coroutines.flow.StateFlow

interface MainViewModelInterface {
    val isRefreshing: StateFlow<Boolean>
    val slotSuffix: String
    val isPatchedA: StateFlow<Boolean>
    val isPatchedB: StateFlow<Boolean>
    fun refresh()
    fun togglePatched()
}