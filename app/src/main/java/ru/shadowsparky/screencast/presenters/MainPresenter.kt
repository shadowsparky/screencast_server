/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.presenters

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Main
import ru.shadowsparky.screencast.models.MainModel
import ru.shadowsparky.screencast.views.MainFragment

class MainPresenter(private val view: MainFragment, private val model: Main.Model = MainModel()) : Main.Presenter {
    private val TAG = "MainPresenter"
    private lateinit var binder: ProjectionService.ProjectionBinder
    private lateinit var mConnection: ServiceConnection
    private val log = Injection.provideLogger()
    override fun onFragmentCreated() {
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                view.mBound = false
                log.printDebug("Service Disconnected", TAG, true)
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                view.mBound = true
                binder = service as ProjectionService.ProjectionBinder
                val mService = binder.getService()
                view.setService(mService)
                log.printDebug("Service Connected ${mService.hashCode()}", TAG, true)
            }
        }
        view.bindService(mConnection)
    }

    override fun onFragmentLoaded() {
        view.setIPV4Text(model.getIpV4Request())
    }

    override fun onFragmentDestroyed() {
        if (view.mBound) view.unbindService(mConnection)
    }

    override fun onLaunchButtonClicked() {
        log.printDebug("LAUNCH BUTTON CLICKED. CURRENT STATUS: ${view.mCurrentStatus.name}")
        when(view.mCurrentStatus) {
            MainFragment.ConnectionStatus.NONE -> {
                view.sendCaptureRequest()
                log.printDebug("Connection started", TAG)
            }
            MainFragment.ConnectionStatus.CONNECTED -> {
                view.reset()
                log.printDebug("Connection closed ${view.mCurrentStatus.name}", TAG)
            }
            else -> throw RuntimeException("Unrecognized Status")
        }
    }

    override fun onLaunchServiceRequest(data: Intent?) = GlobalScope.launch {
        val result = model.launchService(view.mService, data!!)
        view.setLoading(false)
        if (!result)
            view.reset()
        else
            view.setButtonStatus(MainFragment.ConnectionStatus.CONNECTED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                log.printDebug("РАЗРЕШЕНИЕ ВЫДАНО", TAG)
                view.setLoading(true)
                onLaunchServiceRequest(data!!)
            } else
                view.showToast("Вы не выдали разрешение, я не буду работать.")
        }
    }
}