package ru.shadowsparky.screencast
import android.content.Context
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import ru.shadowsparky.screencast.Utils.Injection
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class TestServer {
    private val log = Injection.provideLogger()
    private lateinit var outStream: DataOutputStream
    private lateinit var inStream: DataInputStream
    private lateinit var server: ServerSocket
    private lateinit var client: Socket
    var Success_Connection: Boolean = false
        private set

    init {
        GlobalScope.async {
            try {
                server = ServerSocket(5050)
                client = server.accept()
                log.print("connected")
                outStream = DataOutputStream(client.getOutputStream())
                inStream = DataInputStream(client.getInputStream())
                Success_Connection = true
            } catch(e: Exception) {
                log.print(e.toString())
            }
        }
    }

    fun getClientMessage() : String = inStream.readUTF()

    fun sendMessage() {
        outStream.writeUTF("SENT MESSAGE")
    }

    fun dispose() {
        outStream.close()
        inStream.close()
        client.close()
        server.close()
    }
}