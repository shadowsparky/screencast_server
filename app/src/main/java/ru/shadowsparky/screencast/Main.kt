/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast

import android.content.Context
import android.content.Intent

interface Main {
    interface View {
        fun startServer(server: Intent)
    }
    interface Presenter {
        fun attachView(view: Main.View)
        fun projectionRequest(data: Intent, context: Context)
    }
    interface Model {

    }
}