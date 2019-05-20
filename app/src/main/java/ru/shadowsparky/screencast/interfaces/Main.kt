/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.interfaces

import android.content.Context
import android.content.Intent

interface Main {
    interface View {
        fun startServer(data: Intent)
        fun sendCaptureRequest()
        fun setButtonText(text: String)
        fun setStatusText(text: String)
        fun showToast(text: String)
    }
    interface Presenter {
        fun attachView(view: View)
        fun projectionRequest(data: Intent, context: Context)
    }
    interface Model {
        // nothing
    }
}