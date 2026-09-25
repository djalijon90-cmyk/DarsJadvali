package uz.ies.darsjadvali

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("title") ?: "Dars eslatmasi"
        val text = intent.getStringExtra("text") ?: "Dars vaqti yaqinlashdi"

        val soundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Vibratsiyasiz ovozli kanal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                "lesson_alarm_v2",
                "Dars signallari",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Dars vaqti uchun ovozli eslatmalar"
                enableVibration(false)
                setVibrationPattern(null)
                setSound(soundUri, audioAttributes)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            manager.createNotificationChannel(channel)
        }

        // Bildirishnomani bosganda ilova ochiladi
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            "lesson_alarm_v2"
        )
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0L))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat
                .from(context)
                .notify(
                    (System.currentTimeMillis() % 100000).toInt(),
                    notification
                )
        } catch (_: SecurityException) {
        }
    }
}
