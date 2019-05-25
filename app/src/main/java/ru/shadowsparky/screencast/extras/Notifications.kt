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

/**
 * Обёртка используемая для работы с уведомлениям
 * Вместо использования Builder'ов достаточно просто прописать [provideNotification] и уведомление отобразится.
 * Полная поддержка [Build.VERSION_CODES.O] присутствует
 *
 * @param context текущий контекст [Context]
 * @param dismiss callback, срабатывающий при нажатии на кнопку "отключиться" в уведомлении
 * @since v1.0.0
 * @author shadowsparky
 */
class Notifications(private val context: Context, private val dismiss: PendingIntent) {

    @RequiresApi(Build.VERSION_CODES.O)
    /**
     * Генерация "нового" уведомления. Оно называется новым потому что такие уведомления появились только с появлением Android Oreo.
     *
     * @param notificationManager менеджер уведомлений, используемый для создания [NotificationManager] в методе [notificationChannel]
     * @since v1.0.0
     * @author shadowsparky
     * @return готовое уведомление, выведенное в уведомлениях смартфона
     */
    private fun newNotification(notificationManager: NotificationManager) : Notification {
        notificationChannel(notificationManager)
        return NotificationCompat.Builder(context, DEFAULT_NOTIFICATION_CHANNEL)
                .setContentTitle(DEFAULT_NOTIFICATION_CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .setAutoCancel(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .addAction(android.R.drawable.ic_delete, "Отключиться", dismiss)
                .build()
    }

    /**
     * Генерация "старого" уведомления. Так как приложение поддерживает Android 6 и выше, то о поддержке устройств,
     * на которых Android ниже чем Oreo никто не забывал.
     *
     * @return готовое уведомление, выведенное в уведомлениях смартфона
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun oldNotification() : Notification {
        return NotificationCompat.Builder(context)
                .setContentTitle(DEFAULT_NOTIFICATION_CHANNEL_NAME)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_delete, "Отключиться", dismiss)
                .build()
    }

    /**
     * Генерирование уведомления в зависимости от версии Android.
     *
     * @param notificationManager менеджер уведомлений, используемый для создания [NotificationManager] в методе [notificationChannel]
     *
     * @since v1.0.0
     * @author shadowsparky
     */
    fun provideNotification(notificationManager: NotificationManager) : Notification =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                newNotification(notificationManager)
            } else {
                oldNotification()
            }

    @RequiresApi(Build.VERSION_CODES.O)
    /**
     * Создание [NotificationChannel], для интеграции в него уведомления, на устройствах с Oreo и более новых версий
     *
     * @param notificationManager менеджер уведомлений, используемый для создания [NotificationManager]
     * @since v1.0.0
     * @author shadowsparky
     */
    private fun notificationChannel(notificationManager: NotificationManager) : NotificationChannel {
        val notificationChannel = NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN)
        notificationManager.createNotificationChannel(notificationChannel)
        return notificationChannel
    }
}