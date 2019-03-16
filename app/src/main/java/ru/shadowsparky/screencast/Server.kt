package ru.shadowsparky.screencast

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.*

class Server(addr: InetSocketAddress) : WebSocketServer(addr) {
    private val TAG = javaClass.name
    private val clients = HashSet<WebSocket>()

    fun isConnected(): Boolean {
        for (client in clients) {
            if (client.isOpen)
                return true
        }

        return false
    }

    fun send(data: ByteArray) {
        for (client in clients) {
            client.send(data)
        }
        Log.d(TAG, "Data sent $data")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.d(TAG, "Connection open " + conn.getRemoteSocketAddress().getHostName())
        clients.add(conn)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Log.d(TAG, "Connection closed " + conn.getRemoteSocketAddress().getHostName())
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Log.d(TAG, "Message handled " + conn.getRemoteSocketAddress().getHostName() +
                " message: " + message)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        if (conn != null) {
            clients.remove(conn)
        }
        Log.e(TAG, "STUB! " + ex.message)
    }

    override fun onStart() {
        Log.d(TAG, "Server started")
    }
}