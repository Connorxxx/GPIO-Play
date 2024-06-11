package com.android.gpioplay.models.repository

import android.content.Context
import android.media.AudioManager
import com.android.gpioplay.utils.logCat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeRepository @Inject constructor(@ApplicationContext private val ctx: Context) {

    private val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val getCurrVolume get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    private var temp = getCurrVolume

    private var hasClose = false

    fun turnOff() {
        hasClose = true
        temp = getCurrVolume
        setMediaVolume(0)
    }

    fun recover() {
        if (hasClose) {
            setMediaVolume(temp)
            hasClose = false
        }

    }

    private fun setMediaVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = if (volume < 0) 0 else if (volume > maxVolume) maxVolume else volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
    }
}