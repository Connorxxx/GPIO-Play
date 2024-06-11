package com.android.gpioplay.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android.gpioplay.MainActivity
import com.android.gpioplay.internts.IoState
import com.android.gpioplay.models.data.GpioData
import com.android.gpioplay.models.repository.GpioRepository
import com.android.gpioplay.models.repository.VolumeRepository
import com.android.gpioplay.utils.LimitedList
import com.android.gpioplay.utils.logCat
import com.android.gpioplay.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class GpioService : LifecycleService() {

    @Inject lateinit var gpioRepository: GpioRepository

    @Inject lateinit var volumeRepository: VolumeRepository

    private val list = LimitedList(ArrayList<IoState>())

    private var job: Job? = null


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "gpio_service", "Gpio Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
            val intent = Intent(this, MainActivity::class.java)
            val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(this, "gpio_service")
                .setContentTitle("Gpio Running...")
                .setContentIntent(pi)
                .build()
            startForeground(1, notification)
        }
        "Gpio 感应服务启动".showToast()
        lifecycleScope.launch {
            gpioRepository.apply {
                openAllIo()
                launch { readGpio() }
                launch {
                    state.collect { io ->
                        list.add(io)
                        //list.joinToString().logCat()
                        checkIoStates()?.let {
                            when (it.io) {
                                1 -> onOffScreen(it.state)
                                2 -> {
                                    "上一个节目 ${it.state}".logCat()
                                    if (it.state == 1) gpioRepository.lastProgram()
                                }
                                3 -> {
                                    "下一个节目 ${it.state}".logCat()
                                    if (it.state == 1) gpioRepository.nextProgram()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun CoroutineScope.onOffScreen(v: Int) {

        when (v) {
            1 -> {
                job?.cancel()
                "open screen".logCat()
                volumeRepository.recover()
                gpioRepository.lcdOnOff(1)
               // gpioRepository.awake()
            }

            0 -> {
                job = launch(Dispatchers.IO) {
                    "2 minutes later close screen".logCat()
                    delay(2.minutes)
                    volumeRepository.turnOff()
                    gpioRepository.lcdOnOff(0)
                    "close screen".logCat()
                }
            }
        }
    }


    private fun checkIoStates(): GpioData? {
        if (list.size <= 1) return null
        if (list[0].io1 != list[1].io1) return GpioData(1, list.last().io1)
        if (list[0].io2 != list[1].io2) return GpioData(2, list.last().io2)
        if (list[0].io3 != list[1].io3) return GpioData(3, list.last().io3)
        return null
    }

}