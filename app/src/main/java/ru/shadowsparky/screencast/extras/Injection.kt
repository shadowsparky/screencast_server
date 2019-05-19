/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import android.content.Context
import java.util.concurrent.LinkedBlockingQueue

object Injection {
    private var logInstance: Logger? = null
    fun provideIpHandler() : IpHandler = IpHandler()
    fun provideSharedUtils(context: Context) : SharedUtils = SharedUtils(context)
    fun provideSettingsParser(context: Context) : SettingsParser = SettingsParser(context)

    fun provideLogger() : Logger {
        if (logInstance == null)
            logInstance = Logger()

        return logInstance!!
    }

    fun provideToaster() : Toaster = Toaster()
    fun provideUtils() : Utils = Utils()
    fun provideByteQueue() : LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()

}