/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.content.Context
import android.content.Intent
import java.io.Serializable

interface Main {
    interface View {
        fun startServer(server: Intent)
        fun print(message: String)
        fun showToast(message: String)
        fun sendCaptureRequest()
        fun setLocking(flag: Boolean)
    }
    interface Presenter {
        fun attachView(view: Main.View)
        fun projectionRequest(data: Intent, context: Context)
    }
    interface Model {

    }
}