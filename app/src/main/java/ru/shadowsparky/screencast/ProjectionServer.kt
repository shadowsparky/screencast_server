package ru.shadowsparky.screencast

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import ru.shadowsparky.screencast.Utils.Constants.Companion.DATA
import ru.shadowsparky.screencast.Utils.Constants.Companion.DEFAULT_NOTIFICATION_ID
import ru.shadowsparky.screencast.Utils.Injection
import ru.shadowsparky.screencast.Utils.Logger
import ru.shadowsparky.screencast.Utils.Notifications

@SuppressLint("Registered")
class ProjectionServer : Service() {
    private lateinit var mData: Intent
    private lateinit var mProjectionManager: MediaProjectionManager
    private val log: Logger = Injection.provideLogger()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.printDebug("STARTED")
        mData = intent!!.getParcelableExtra(DATA)
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = Notifications(this).provideNotification(notificationService)
        startForeground(DEFAULT_NOTIFICATION_ID, notification)
        log.printDebug("FOREGROUND STARTED")
        return START_NOT_STICKY
    }
}
