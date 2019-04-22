/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import ru.shadowsparky.screencast.interfaces.Main
import ru.shadowsparky.screencast.presenters.MainPresenter
import java.util.concurrent.LinkedBlockingQueue

object Injection {
    private var logInstance: Logger? = null
    fun provideIpHandler() : IpHandler = IpHandler()

    fun provideLogger() : Logger {
        if (logInstance == null)
            logInstance = Logger()

        return logInstance!!
    }

    fun provideToaster() : Toaster = Toaster()
    fun provideUtils() : Utils = Utils()
    fun provideMainPresenter() : Main.Presenter = MainPresenter()
    fun provideByteQueue() : LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()

}