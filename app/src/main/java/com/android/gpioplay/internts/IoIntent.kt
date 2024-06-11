package com.android.gpioplay.internts

data class IoState(
    val io1: Int = -1,
    val io2: Int = 0,
    val io3: Int = 0
)

sealed interface IoEvent {
    data class Io1(val v: Int) : IoEvent
    data class Io2(val v: Int) : IoEvent
    data class Io3(val v: Int) : IoEvent
}
