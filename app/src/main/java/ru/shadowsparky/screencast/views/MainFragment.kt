/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*
import ru.shadowsparky.screencast.ProjectionService
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.ServerBase
import ru.shadowsparky.screencast.ServerBase.ConnectionResult.*
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Injection
import ru.shadowsparky.screencast.interfaces.Main
import ru.shadowsparky.screencast.interfaces.Actionable
import ru.shadowsparky.screencast.presenters.MainPresenter

class MainFragment : Fragment(), Actionable, Main.View {
    private lateinit var mediaManager : MediaProjectionManager
    private val TAG = "MainFragment"
    private val log = Injection.provideLogger()
    private val presenter: Main.Presenter = MainPresenter(this)
    lateinit var mService: ProjectionService; private set
    var mBound = false
    private val toast = Injection.provideToaster()
    var mCurrentStatus: ConnectionStatus = ConnectionStatus.NONE; private set

    enum class ConnectionStatus {
        NONE,
        CONNECTED
    }

    override fun invoke(action: ServerBase.ConnectionResult) {
        status.text = when(action) {
            ADDRESS_ALREADY_IN_USE -> "Данный адрес уже используется"
            WAITING_FOR_CONNECTION -> "Ожидание подключения"
            TIMEOUT -> "Превышено время ожидания"
            UNEXPECTEDLY_DISCONNECTED -> "Соединение нежиданно прервалось"
            ESTABLISHED -> "Соединение установлено"
            BROKEN -> "Соединение разорвано"
        }
        if ((action != WAITING_FOR_CONNECTION) and (action != ESTABLISHED)) reset()
    }

    override fun reset() {
        mService.close()
        status.text = ""
        setButtonStatus(ConnectionStatus.NONE)
    }

    override fun showToast(message: String) { toast.show(context!!, message) }

    override fun setService(service: ProjectionService) {
        mService = service
        mService.action = this
    }

    override fun setLoading(status: Boolean) = activity!!.runOnUiThread {
        capRequest?.isEnabled = !status
    }

    override fun setIPV4Text(text: String) = activity!!.runOnUiThread {
        ipv4.text = text
    }

    override fun setButtonStatus(status: ConnectionStatus) = activity!!.runOnUiThread {
        capRequest.text = if (status == ConnectionStatus.NONE) "Старт сервера" else "Отключиться"
        mCurrentStatus = status
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun bindService(connection: ServiceConnection) {
        val intent = Intent(context, ProjectionService::class.java)
        context?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun unbindService(connection: ServiceConnection) {
        context?.unbindService(connection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaManager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        presenter.onFragmentCreated()
    }

    override fun sendCaptureRequest() = startActivityForResult(mediaManager.createScreenCaptureIntent(), Constants.REQUEST_CODE)

    override fun onStart() {
        super.onStart()
        presenter.onFragmentLoaded()
        capRequest.setOnClickListener { presenter.onLaunchButtonClicked() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        log.printDebug("On Save Instance State", TAG)
        outState.putBoolean("LOADING", !capRequest!!.isEnabled)
        outState.putBoolean("BOUND", mBound)
        outState.putString("STATUS", status.text.toString())
        outState.putString("CAPREQUEST", capRequest.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        log.printDebug("On View State Restored", TAG)
        if (savedInstanceState != null) {
            setLoading(savedInstanceState.getBoolean("LOADING", false))
            mBound = savedInstanceState.getBoolean("BOUND", false)
            status.text = savedInstanceState.getString("STATUS")
            capRequest.text = savedInstanceState.getString("CAPREQUEST")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onFragmentDestroyed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }
}