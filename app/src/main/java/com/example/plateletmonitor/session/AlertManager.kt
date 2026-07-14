package com.example.plateletmonitor.session

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class AlertManager(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Donor Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when GSR and pulse deviate from baseline"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showStressAlert(sessionId: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Stress pattern detected")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(sessionId.hashCode(), notification)
        vibrateWatch()
    }

    private fun vibrateWatch() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 250), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    companion object {
        const val CHANNEL_ID = "stress-alerts"
    }
}
