package ru.shadowsparky.screencast.Utils

import android.util.Log
import android.util.Log.DEBUG

class Logger {
    fun print(message: String) {
        Log.println(DEBUG, "MAIN_TAG", "THREAD: ${Thread.currentThread().name}; $message")
    }
}