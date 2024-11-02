package com.ph.exchange.orders.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.events.internal.InternalEventingMessageStatus
import com.ph.exchange.orders.model.events.internal.OrderFilledEvent
import io.quarkus.logging.Log
import io.quarkus.vertx.ConsumeEvent
import io.quarkus.websockets.next.OpenConnections
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class OrderFilledEventListener {
    private val ordersConnections = mutableMapOf<String, String>()

    @Inject
    private lateinit var openConnections: OpenConnections

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Blocking
    @ConsumeEvent("ORDER_FILLED")
    fun consume(message: String): Uni<String> {
        Log.debug("Got message order filled $message")
        val order =
            objectMapper.readValue(message, object : TypeReference<OrderFilledEvent>() {})
        val connection = openConnections.find {
            ordersConnections.keys.contains(it.id()) &&
                ordersConnections[it.id()] == order.institution
        }
        if (connection != null) {
            Log.info("sending message $message FILLED -> ${connection.id()}")
            connection.sendTextAndAwait(message)
        }
        return Uni.createFrom().item(InternalEventingMessageStatus.OK.name)
    }

    fun addConnection(wsConnId: String, institution: String) {
        ordersConnections[wsConnId] = institution
    }

    fun removeConnection(wsConnId: String) {
        ordersConnections.remove(wsConnId)
    }
}
