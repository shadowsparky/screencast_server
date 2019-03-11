/*
 * Created by shadowsparky in 2018
 */

package ru.shadowsparky.screencast.extras

import ru.shadowsparky.screencast.*
import ru.shadowsparky.screencast.extras.Constants.Companion.DEFAULT_PORT
import java.net.InetSocketAddress
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

        fun provideJSServer() : Server = Server(InetSocketAddress(provideIpHandler().getIpv4(), DEFAULT_PORT + 9    ))

        fun provideToaster() : Toaster = Toaster()
        fun provideServer() : ProjectionServer = ProjectionServer()
        fun provideUtils() : Utils = Utils()
        fun provideMainPresenter() : Main.Presenter = MainPresenter()
        fun provideByteQueue() : LinkedBlockingQueue<TransferByteArray> = LinkedBlockingQueue()
    }
}