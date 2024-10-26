package com.ph.exchange.marketdata.api

import com.ph.exchange.marketdata.service.MarketDataService
import io.quarkus.logging.Log
import io.quarkus.websockets.next.OnClose
import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.WebSocketConnection
import jakarta.inject.Inject

@WebSocket(path = "/ws/quotes")
class QuotesWebSocketApi {
    @Inject
    private lateinit var marketDataService: MarketDataService

    @OnOpen(broadcast = true)
    fun onOpen(webSocketConnection: WebSocketConnection) {
        Log.info("Connection open in quotes endpoint with id ${webSocketConnection.id()}")
        marketDataService.subscribeToQuotes(webSocketConnection.id())
    }

    @OnClose
    fun onClose(webSocketConnection: WebSocketConnection) {
        Log.info("Connection ${webSocketConnection.id()} close")
        marketDataService.removeConnectionFromQuotes(webSocketConnection.id())
    }
}
