package com.android.gpioplay.utils

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.gpioplay.App
import com.android.gpioplay.BuildConfig

fun Any.logCat(tab: String = "GPIO_PLAY_LOG") {
    if (!BuildConfig.DEBUG) return
    if (this is String) Log.d(tab, this) else Log.d(tab, this.toString())
}

fun String.showToast() {
    Toast.makeText(App.app, this, Toast.LENGTH_SHORT).show()
}

inline fun <reified T> Context.intent(builder: Intent.() -> Unit = {}): Intent =
    Intent(this, T::class.java).apply(builder)

inline fun <reified T : Service> Context.startService(block: Intent.() -> Unit = {}) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent<T>(block))
    else startService(intent<T>(block))