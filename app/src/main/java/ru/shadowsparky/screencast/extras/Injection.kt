/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import ru.shadowsparky.screencast.Main
import ru.shadowsparky.screencast.MainPresenter
import ru.shadowsparky.screencast.ProjectionServer
import ru.shadowsparky.screencast.TransferByteArray
import java.util.concurrent.LinkedBlockingQueue

class Injection {
    companion object {
        private var logInstance: Logger? = null

        fun provideIpHandler() : IpHandler = IpHandler()

        fun provideLogger() : Logger {
            if (logInstance == null)
                logInstance = Logger()

            return logInstance!!
        }

        fun provideToaster() : Toaster = Toaster()
        fun provideServer() : ProjectionServer = ProjectionServer()
        fun provideMainPresenter() : Main.Presenter = MainPresenter()
        fun provideByteQueue() : LinkedBlockingQueue<TransferByteArray> = LinkedBlockingQueue()
    }
}