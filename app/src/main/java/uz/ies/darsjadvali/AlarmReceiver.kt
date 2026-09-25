
package uz.ies.darsjadvali

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Dars eslatmasi"
        val text = intent.getStringExtra("text") ?: "Dars vaqti yaqinlashdi"
        val n = NotificationCompat.Builder(context, "lessons")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        try { NotificationManagerCompat.from(context).notify((System.currentTimeMillis()%100000).toInt(), n) } catch (_: SecurityException) {}
    }
}
