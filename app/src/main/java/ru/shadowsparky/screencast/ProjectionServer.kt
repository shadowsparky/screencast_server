package ru.shadowsparky.screencast

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

class ProjectionServer : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }
}
