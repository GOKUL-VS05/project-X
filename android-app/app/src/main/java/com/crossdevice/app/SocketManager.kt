package com.crossdevice.app

import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {

    lateinit var socket: Socket
        private set

    fun connect(
        serverUrl: String,
        clientId: String,
        onOffer: ((String) -> Unit)? = null,
        onAnswer: ((String) -> Unit)? = null,
        onCandidate: ((String) -> Unit)? = null,
        onControlMessage: (String) -> Unit = {}
    ) {
        if (!::socket.isInitialized) {
            socket = IO.socket(serverUrl)
        }

        socket.off("offer")
        socket.off("answer")
        socket.off("candidate")
        socket.off("control")
        socket.off(Socket.EVENT_CONNECT)

        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("register", clientId)
        }

        socket.on("offer") { args ->
            onOffer?.invoke(args.firstOrNull()?.toString().orEmpty())
        }

        socket.on("answer") { args ->
            onAnswer?.invoke(args.firstOrNull()?.toString().orEmpty())
        }

        socket.on("candidate") { args ->
            onCandidate?.invoke(args.firstOrNull()?.toString().orEmpty())
        }

        socket.on("control") { args ->
            val data = args.firstOrNull()?.toString().orEmpty()
            onControlMessage(data)
        }

        if (!socket.connected()) {
            socket.connect()
        } else {
            socket.emit("register", clientId)
        }
    }

    fun disconnect() {
        if (::socket.isInitialized) {
            socket.disconnect()
        }
    }
}
