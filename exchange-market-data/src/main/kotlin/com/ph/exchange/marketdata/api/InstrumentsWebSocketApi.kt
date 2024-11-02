package com.ph.exchange.marketdata.api

import com.ph.exchange.marketdata.service.MarketDataService
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.quarkus.logging.Log
import io.quarkus.websockets.next.OnClose
import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.WebSocketConnection
import jakarta.inject.Inject

@WebSocket(path = "/ws/instruments")
class InstrumentsWebSocketApi {
    @Inject
    private lateinit var marketDataService: MarketDataService

    @WithSpan("instruments.connection")
    @OnOpen(broadcast = true)
    fun onOpen(webSocketConnection: WebSocketConnection) {
        Log.info("Connection open in instruments endpoint with id ${webSocketConnection.id()}")
        marketDataService.subscribeToInstruments(webSocketConnection.id())
        val instruments = marketDataService.getAvailableInstruments()
        webSocketConnection.sendTextAndAwait(instruments)
    }

    @OnClose
    fun onClose(webSocketConnection: WebSocketConnection) {
        Log.info("Connection ${webSocketConnection.id()} close")
        marketDataService.removeConnectionFromInstruments(webSocketConnection.id())
    }
}
