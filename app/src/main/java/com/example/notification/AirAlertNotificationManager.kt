package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.AirQualityLevel

class AirAlertNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "แจ้งเตือนค่าฝุ่น PM2.5 วิกฤต",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "แจ้งเตือนแบบเรียลไทม์เมื่อตรวจพบฝุ่นละออง PM2.5 สูงเกินเกณฑ์มาตรฐาน"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendAirQualityAlert(
        pm25: Float,
        aqi: Int,
        level: AirQualityLevel,
        locationName: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ แจ้งเตือนด่วน: PM2.5 เกินมาตรฐาน (${pm25} µg/m³)"
        val content = "พื้นที่ $locationName: ${level.titleTh} (AQI $aqi) - ${level.maskRecommendation}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\nคำแนะนำ: ${level.outdoorRecommendation}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+ yet
        }
    }

    companion object {
        const val CHANNEL_ID = "pm25_air_alerts_channel"
        const val NOTIFICATION_ID = 1001
    }
}
