package com.ph.exchange.orders.api

import com.ph.exchange.orders.handler.OrderFilledHandler
import io.quarkus.logging.Log
import io.quarkus.websockets.next.OnClose
import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.OnTextMessage
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.WebSocketConnection
import jakarta.inject.Inject

@WebSocket(path = "/ws/orders/update/{institution}")
class OrderUpdatesWebSocketApi {
    private val institutionPathName = "institution"

    @Inject
    private lateinit var orderFilledHandler: OrderFilledHandler

    @OnTextMessage
    fun onMessage(orderMessage: String) {
    }

    @OnOpen(broadcast = true)
    fun onOpen(webSocketConnection: WebSocketConnection) {
        val institution = webSocketConnection.pathParam(institutionPathName)
        Log.info(
            "Connection open in order updates with id ${webSocketConnection.id()}, institution $institution"
        )
        orderFilledHandler.addConnection(webSocketConnection.id(), institution)
    }

    @OnClose
    fun onClose(webSocketConnection: WebSocketConnection) {
        Log.info("Connection ${webSocketConnection.id()} close")
        orderFilledHandler.removeConnection(webSocketConnection.id())
    }
}
