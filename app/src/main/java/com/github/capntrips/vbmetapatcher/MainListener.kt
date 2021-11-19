package com.github.capntrips.vbmetapatcher

internal class MainListener constructor(private val callback: () -> Unit) {
    fun resume() {
        callback.invoke()
    }
}
