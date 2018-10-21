package ru.shadowsparky.screencast.Utils

import ru.shadowsparky.screencast.TestServer


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

        fun provideServer() : TestServer = TestServer()
    }
}