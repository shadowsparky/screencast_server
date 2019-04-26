/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast.views

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_main.*
import ru.shadowsparky.screencast.CommunicationReceiver
import ru.shadowsparky.screencast.R
import ru.shadowsparky.screencast.interfaces.Main
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Injection

class MainFragment : Fragment(), Main.View {
    private val log = Injection.provideLogger()
    private val toast = Injection.provideToaster()
    private val address = Injection.provideIpHandler().getIpv4()
    private val presenter = Injection.provideMainPresenter()
    private lateinit var manager : MediaProjectionManager
    private lateinit var receiver: CommunicationReceiver
    private var server: Intent? = null

    override fun setLocking(flag: Boolean) {
        if (flag) {
            setButtonText("Отключиться")
        } else {
            context?.stopService(server)
            setButtonText("Подключиться")
            print("Произошло отключение от клиента")
        }
        capRequest.isEnabled = true
    }

    override fun setButtonText(text: String) {
        capRequest.text = text
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(receiver)
    }

    override fun print(message: String) {
        status.text = message
    }

    override fun showToast(message: String) {
        toast.show(context!!, message)
    }

    override fun sendCaptureRequest() {
        startActivityForResult(manager.createScreenCaptureIntent(), Constants.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                log.printDebug("РАЗРЕШЕНИЕ ВЫДАНО")
                presenter.projectionRequest(data!!, context!!)
            } else {
                toast.show(context!!, "Вы не выдали разрешение, я не буду работать.")
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
        capRequest.setOnClickListener {
            if (capRequest.text == "Подключиться")
                sendCaptureRequest()
            else {
                setLocking(false)
            }
        }
        ipv4.text = address
        manager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val filter = IntentFilter(Constants.BROADCAST_ACTION)
        receiver = CommunicationReceiver(this)
        context?.registerReceiver(receiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun startServer(server: Intent) {
        context?.startService(server)
        this.server = server
        print("Ожидание подключения...")
        capRequest.isEnabled = false
    }
}