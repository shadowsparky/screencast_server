package ru.shadowsparky.screencast

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.async
import kotlinx.android.synthetic.main.activity_main.*
import ru.shadowsparky.screencast.Utils.Injection

class Main : AppCompatActivity() {
    private var server = Injection.provideServer()
    private val log = Injection.provideLogger()
    private val toast = Injection.provideToaster()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            test()
        }
    }

    fun test() {
        val context = this
        if (server.Success_Connection) {
            GlobalScope.async(Dispatchers.IO) {
                toast.show(context, server.getClientMessage())
            }
        } else {
            toast.show(context, "Нет соединения с клиентом")
        }
    }
}
