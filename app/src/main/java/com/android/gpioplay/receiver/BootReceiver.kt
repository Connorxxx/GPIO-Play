package com.android.gpioplay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.gpioplay.service.GpioService
import com.android.gpioplay.utils.startService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.startService<GpioService>()
        }
    }

}