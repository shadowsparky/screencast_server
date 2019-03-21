/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.shadowsparky.screencast.extras.Constants
import ru.shadowsparky.screencast.extras.Constants.Companion.REQUEST_CODE
import ru.shadowsparky.screencast.extras.Injection

class MainView : AppCompatActivity(), Main.View {
    private val log = Injection.provideLogger()

    private val toast = Injection.provideToaster()
    private val address = Injection.provideIpHandler().getIpv4()
    private val presenter = Injection.provideMainPresenter()
    private lateinit var manager : MediaProjectionManager
    private lateinit var receiver: CommunicationReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        presenter.attachView(this)
        ipv4.text = address
        capRequest.setOnClickListener { sendCaptureRequest() }
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val filter = IntentFilter(Constants.BROADCAST_ACTION)
        receiver = CommunicationReceiver(this)
        registerReceiver(receiver, filter)
    }

    override fun setLocking(flag: Boolean) {
        capRequest.isEnabled = !flag
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun print(message: String) {
        status.text = message
    }

    override fun showToast(message: String) {
        toast.show(this, message)
    }

    override fun sendCaptureRequest() {
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                log.printDebug("РАЗРЕШЕНИЕ ВЫДАНО")
                presenter.projectionRequest(data!!, this)
            } else {
                toast.show(this, "Вы не выдали разрешение, я не буду работать.")
            }
        }
    }

    override fun startServer(server: Intent) {
        startService(server)
        print("Ожидание подключения...")
        setLocking(true)
    }
}
