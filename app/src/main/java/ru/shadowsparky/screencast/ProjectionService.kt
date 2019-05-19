/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext as async

class ProjectionService : ServerBase() {
    override val TAG: String = "ProjectionService"

    suspend fun launch(mData: Intent) : Boolean = async (IO) {
        this@ProjectionService.mData = mData
        var result = async(IO) { createServer() }
        if (!result)
            return@async result
        result = async(IO) { accept() }
        if (!result)
            return@async result
        createNotification()
        setupProjection()
        start()
        return@async true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if ((mServer != null) and (mServer?.isClosed == false)) {
            GlobalScope.launch(IO) {
                log.printDebug("Configuration Changed", TAG)
                stop()
                setupProjection()
                start()
            }
        }
    }

    private val mBinder = ProjectionBinder()
    override fun onBind(intent: Intent?): IBinder = mBinder
    inner class ProjectionBinder : Binder() {
        fun getService() = this@ProjectionService
    }
}