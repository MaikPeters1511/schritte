package de.schritte.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.schritte.app.ui.MainActivity
import java.util.Locale

class GoalNotification(private val context: Context) {
    fun show(steps: Int, goal: Int) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Tagesziel", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentTitle("Tagesziel erreicht 🎉")
            .setContentText(
                String.format(Locale.GERMANY, "Du hast heute %,d Schritte geschafft (Ziel: %,d).", steps, goal)
            )
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        // Permission is checked via areNotificationsEnabled() above.
        @Suppress("MissingPermission")
        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL = "goal"
        const val NOTIFICATION_ID = 2
    }
}
