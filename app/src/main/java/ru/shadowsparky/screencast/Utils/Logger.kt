package ru.shadowsparky.screencast.Utils

import android.util.Log
import android.util.Log.DEBUG
import android.util.Log.ERROR
import ru.shadowsparky.screencast.Utils.Constants.Companion.TAG

class Logger {
    private fun getThreadName() : String = "THREAD: ${Thread.currentThread().name}"
    private fun getMessage(message: String, threadDump: Boolean) : String =
            if (threadDump)
                "${getThreadName()} $message"
            else
                message
    private fun print(priority: Int, message: String) {
        Log.println(priority, TAG, message)
    }

    fun printError(message: String, threadDump: Boolean = false) {
        print(ERROR, getMessage(message, threadDump))
    }

    fun printDebug(message: String, threadDump: Boolean = false) {
        print(DEBUG, getMessage(message, threadDump))
    }
}