/*
 * Created by shadowsparky in 2019
 */

package ru.shadowsparky.screencast

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        var app: App? = null
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}