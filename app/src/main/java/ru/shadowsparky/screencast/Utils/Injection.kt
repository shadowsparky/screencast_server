package ru.shadowsparky.screencast.Utils

import ru.shadowsparky.screencast.Main
import ru.shadowsparky.screencast.MainPresenter
import ru.shadowsparky.screencast.ProjectionServer
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
        fun provideByteQueue() : LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue()
    }
}