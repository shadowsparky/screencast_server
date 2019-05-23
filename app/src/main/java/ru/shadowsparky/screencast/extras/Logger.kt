/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.util.Log
import android.util.Log.DEBUG
import android.util.Log.ERROR

class Logger {
    private fun getThreadName() : String = "THREAD: ${Thread.currentThread().name}"
    private fun getMessage(message: String, threadDump: Boolean) : String = if (threadDump) "${getThreadName()} $message" else message
    private fun print(priority: Int, message: String, TAG: String = Constants.TAG) = Log.println(priority, TAG, message)
    fun printError(message: String, XTAG: String = Constants.TAG, threadDump: Boolean = false) = print(ERROR, getMessage(message, threadDump), XTAG)
    fun printDebug(message: String, XTAG: String = Constants.TAG, threadDump: Boolean = false) = print(DEBUG, getMessage(message, threadDump), XTAG)
}