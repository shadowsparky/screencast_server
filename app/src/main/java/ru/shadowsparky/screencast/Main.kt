package ru.shadowsparky.screencast

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.*
import ru.shadowsparky.screencast.Utils.Injection

class Main : AppCompatActivity() {
    private var server = Injection.provideServer()
    private val log = Injection.provideLogger()
    private val toast = Injection.provideToaster()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            GlobalScope.async(Dispatchers.Unconfined) {test()}
        }
    }

    suspend fun test() {
        val context = this
        if (server.isSuccess) {
//            toast.show(context, "HANDLED ${server.getClientMessageAsync()}")
            server.sendMessageAsync("Message: ")
        } else {
            toast.show(context, "Нет соединения с клиентом")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server.dispose()
    }
}
