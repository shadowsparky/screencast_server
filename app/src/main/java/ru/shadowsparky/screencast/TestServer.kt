package ru.shadowsparky.screencast
import android.content.Context
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.IO
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
    var isSuccess: Boolean = false
        private set

    init {
        GlobalScope.async {
            try {
                server = ServerSocket(5050)
                client = server.accept()
                log.print("connected")
                outStream = DataOutputStream(client.getOutputStream())
                inStream = DataInputStream(client.getInputStream())
                isSuccess = true
            } catch(e: Exception) {
                log.print(e.toString())
            }
        }
    }

    suspend fun getClientMessageAsync() : String {
        val result = GlobalScope.async { inStream.readUTF() }
        log.print("MESSAGE HANDLED: ${result.await()}")
        return result.await()
    }

    fun sendMessageAsync(message: String) = GlobalScope.async {
        outStream.writeUTF(message)
        log.print("MESSAGE SENT: $message")
    }

    fun dispose() {
        outStream.close()
        inStream.close()
        client.close()
        server.close()
    }
}