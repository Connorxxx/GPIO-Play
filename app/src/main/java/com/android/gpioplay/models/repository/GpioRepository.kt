package com.android.gpioplay.models.repository

import android.content.Context
import android.content.Intent
import com.android.gpioplay.App
import com.android.gpioplay.internts.IoState
import com.android.gpioplay.models.data.GpioData
import com.android.gpioplay.utils.logCat
import com.softwinner.Gpio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpioRepository @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val gpio = intArrayOf(1, 2, 3, 4)

    private val _state = MutableStateFlow(IoState())
    val state = _state.asStateFlow()

    suspend fun readGpio() = withContext(Dispatchers.IO) {
        while (true) {
            ioList.forEach { io ->
                delay(100)
                val v = Gpio.readGpio('O', gpio[io])
                //"io: $io value: $v".logCat()
                when (io) {
                    0 -> _state.update { it.copy(io1 = v) }
                    1 -> _state.update { it.copy(io2 = v) }
                    2 -> _state.update { it.copy(io3 = v) }
                }
            }
        }
    }

    fun openAllIo() {
        repeat(4) {
            if (it <= 1) Gpio.setMulSel('O', gpio[it], 0)
            else Gpio.setMulSel('O', gpio[it], 1)
        }
    }

    fun lcdOnOff(i: Int) {
        when (i) {
            1 -> Intent("com.zc.zclcdon").apply {
               // putExtra("LcdHdmiOn", 1)
            }.also(App.app::sendBroadcast)

            0 -> Intent("com.zc.zclcdoff").apply {
               // putExtra("LcdHdmiOff", 1)
            }.also(App.app::sendBroadcast)
        }
    }

    fun lastProgram() {
        sendBroadcast("com.zckjService.PROGRAMPREVIOUS")
    }

    fun nextProgram() {
        sendBroadcast("com.zckjService.PROGRAMNEXT")
    }

    fun sleep() {
        sendBroadcast("com.zckjService.SLEEP")
    }

    fun awake() {
        sendBroadcast("com.zckjService.AWAKE")
    }

    private fun sendBroadcast(action: String, builder: Intent.() -> Unit = {}) {
        "发送广播: $action ".logCat()
        Intent(action).apply {
            builder()
        }.also(ctx::sendBroadcast)
    }

    companion object {
        val ioList = listOf(0, 1, 2)
    }
}