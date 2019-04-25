/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.interfaces

import android.content.Context
import android.content.Intent

interface Main {
    interface View {
        fun startServer(server: Intent)
        fun print(message: String)
        fun showToast(message: String)
        fun sendCaptureRequest()
        fun setLocking(flag: Boolean)
        fun setButtonText(text: String)
    }
    interface Presenter {
        fun attachView(view: View)
        fun projectionRequest(data: Intent, context: Context)
    }
    interface Model {
        // nothing
    }
}