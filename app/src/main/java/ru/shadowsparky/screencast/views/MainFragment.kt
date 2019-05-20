/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Printeable
import java.lang.RuntimeException

class MainFragment : Fragment(), Printeable {
    private val TAG = "MainFragment"
    private lateinit var binder: ProjectionService.ProjectionBinder
    private lateinit var mConnection: ServiceConnection
    private lateinit var manager : MediaProjectionManager
    private val log = Injection.provideLogger()
    private lateinit var mService: ProjectionService
    private val toast = Injection.provideToaster()
    private var mBound = false
    private var loading = false
    set(value) {
        activity?.runOnUiThread {
            capRequest?.isEnabled = !value
            field = value
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.printDebug("On Create", TAG)
        manager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                mBound = false
                log.printDebug("Service Disconnected ${mService.hashCode()}", TAG, true)
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mBound = true
                binder = service as ProjectionService.ProjectionBinder
                mService = binder.getService()
                mService.printeable = this@MainFragment
                log.printDebug("Service Connected ${mService.hashCode()}", TAG, true)
            }
        }
        val intent = Intent(context, ProjectionService::class.java)
        context?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        ipv4.text = Injection.provideIpHandler().getIpv4()
        capRequest.setOnClickListener {
            when {
                capRequest.text == "Старт сервера" -> sendCaptureRequest()
                capRequest.text == "Отключиться" -> {
                    mService.close()
                    capRequest.text = "Старт сервера"
                }
                else -> throw RuntimeException("Unrecognized text ${capRequest.text}")
            }
        }
    }

    override fun print(msg: String) {
        status.text = msg
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        log.printDebug("On Save Instance State", TAG)
        outState.putBoolean("LOADING", loading)
        outState.putBoolean("BOUND", mBound)
        outState.putString("STATUS", status.text.toString())
        outState.putString("CAPREQUEST", capRequest.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        log.printDebug("On View State Restored", TAG)
        if (savedInstanceState != null) {
            loading = savedInstanceState.getBoolean("LOADING", false)
            mBound = savedInstanceState.getBoolean("BOUND", false)
            status.text = savedInstanceState.getString("STATUS")
            capRequest.text = savedInstanceState.getString("CAPREQUEST")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBound) {
            context?.unbindService(mConnection)
            mBound = false
            log.printDebug("Service is untied", TAG)
        }
    }

    private fun startServer(data: Intent) {
        GlobalScope.launch {
            log.printDebug("Requested trying to connect to server...", TAG)
            val result = mService.launch(data)
            loading = false
            if (!result)
                mService.close()
            else
                capRequest.text = "Отключиться"
            log.printDebug("$result", TAG)
        }
    }

    private fun sendCaptureRequest() = startActivityForResult(manager.createScreenCaptureIntent(), Constants.REQUEST_CODE)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                log.printDebug("РАЗРЕШЕНИЕ ВЫДАНО", TAG)
                loading = true
                startServer(data!!)
            } else
                toast.show(context!!, "Вы не выдали разрешение, я не буду работать.")
        }
    }
}