/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_CHANNEL
import ru.shadowsparky.screencast.extras.Constants.DEFAULT_NOTIFICATION_CHANNEL_NAME

class Notifications(private val context: Context, private val dismiss: PendingIntent) {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun newNotification(notificationManager: NotificationManager) : Notification {
        notificationChannel(notificationManager)
        return NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                .setContentTitle(DEFAULT_NOTIFICATION_CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .addAction(android.R.drawable.ic_delete, "Отключиться", dismiss)
                .build()
    }

    private fun oldNotification() : Notification {
        return NotificationCompat.Builder(context)
                .setContentTitle(DEFAULT_NOTIFICATION_CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Отключиться", dismiss)
                .build()
    }

    fun provideNotification(notificationManager: NotificationManager) : Notification =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                newNotification(notificationManager)
            } else {
                oldNotification()
            }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(notificationManager: NotificationManager) : NotificationChannel {
        val notificationChannel = NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel
    }
}