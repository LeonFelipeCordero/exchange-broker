package com.ph.exchange.orders.api

import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import io.quarkus.logging.Log
import io.quarkus.websockets.next.OnClose
import io.quarkus.websockets.next.OnOpen
import io.quarkus.websockets.next.OnTextMessage
import io.quarkus.websockets.next.WebSocket
import io.quarkus.websockets.next.WebSocketConnection
import io.smallrye.common.annotation.Blocking
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata
import jakarta.annotation.PostConstruct
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata
import java.time.ZonedDateTime

@WebSocket(path = "/ws/orders/submission")
class OrderSubmissionWebSocketApi {
    private var outgoingRabbitMQMetadata: OutgoingRabbitMQMetadata =
        OutgoingRabbitMQMetadata.builder()
            .withRoutingKey("order.created")
            .withTimestamp(ZonedDateTime.now())
            .build()

    @Inject
    @Channel("exchange_order_updates")
    private lateinit var emitter: Emitter<String>

    @Inject
    private lateinit var meter: Meter

    private lateinit var orderSubmissionCounter: LongCounter

    @PostConstruct
    fun init() {
        orderSubmissionCounter = meter.counterBuilder("order.submitted")
            .setDescription("Counter for number of order received")
            .setUnit("count")
            .build()
    }

    @Blocking
    @OnTextMessage
    fun onMessage(orderMessage: String) {
        orderSubmissionCounter.add(1)
        emitter.send(
            Message.of(
                orderMessage,
                Metadata.of(outgoingRabbitMQMetadata),
            ),
        )
        // todo reply back for order submission confirmation
    }

    @OnOpen(broadcast = true)
    fun onOpen(webSocketConnection: WebSocketConnection) {
        Log.info("Connection open in order submission endpoint with id ${webSocketConnection.id()}")
    }

    @OnClose
    fun onClose(webSocketConnection: WebSocketConnection) {
        Log.info("Connection ${webSocketConnection.id()} close")
    }
}
