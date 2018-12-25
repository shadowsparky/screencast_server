package ru.shadowsparky.screencast

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import ru.shadowsparky.screencast.Utils.Constants.Companion.REQUEST_CODE
import ru.shadowsparky.screencast.Utils.Injection

class MainView : AppCompatActivity(), Main.View {
    private val log = Injection.provideLogger()
    private val toast = Injection.provideToaster()
    private val presenter = Injection.provideMainPresenter()
    private lateinit var manager : MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                log.printDebug("РАЗРЕШЕНИЕ ВЫДАНО")
                presenter.projectionRequest(data!!, this)
            } else {
                log.printError("РАЗРЕШЕНИЕ НЕ БЫЛО ВЫДАНО")
            }
        }
    }

    override fun startServer(server: Intent) {
        startService(server)
        finish()
        log.printDebug("SERVER STARTED")
    }
}