/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.shadowsparky.screencast.extras.Constants.Companion.ACTION
import ru.shadowsparky.screencast.extras.Constants.Companion.CONNECTION_CLOSED_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.CONNECTION_STARTED_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.NOTHING
import ru.shadowsparky.screencast.extras.Constants.Companion.REASON
import ru.shadowsparky.screencast.extras.Constants.Companion.RECEIVER_CODE
import ru.shadowsparky.screencast.extras.Constants.Companion.RECEIVER_DEFAULT_CODE

class CommunicationReceiver(private val view: Main.View) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val receiver_code = intent!!.getIntExtra(RECEIVER_CODE, -1)
        if (receiver_code == RECEIVER_DEFAULT_CODE) {
            val action = intent.getIntExtra(ACTION, -1)
            val reason = intent.getStringExtra(REASON)
            if (action != -1) {
                when(action) {
                    CONNECTION_STARTED_CODE -> {
                        view.print("Соединение установлено")
                        view.setLocking(true)
                    }
                    CONNECTION_CLOSED_CODE -> {
                        val message = if (reason != NOTHING) {
                            "Во время подключения произошла ошибка. $reason"
                        } else {
                            "Соединение разорвано"
                        }
                        view.print(message)
                        view.setLocking(false)
                    }
                }
            }
        }
    }

}