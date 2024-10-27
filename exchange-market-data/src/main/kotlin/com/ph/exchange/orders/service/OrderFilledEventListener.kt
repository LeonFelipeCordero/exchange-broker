package com.ph.exchange.orders.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.events.internal.InternalEventingMessageStatus
import com.ph.exchange.orders.model.events.internal.OrderFilledEvent
import io.quarkus.logging.Log
import io.quarkus.vertx.ConsumeEvent
import io.quarkus.websockets.next.OpenConnections
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

    // todo this needs to broadcast only to the target connection
    @ConsumeEvent("ORDER_FILLED")
    fun consume(message: String): Uni<String> {
        Log.debug("Got message order filled $message")
        val order =
            objectMapper.readValue(message, object : TypeReference<OrderFilledEvent>() {})
        openConnections
            .filter {
                ordersConnections.values.contains(it.id()) &&
                        ordersConnections[it.id()] == order.institution
            }
            .forEach {
                Log.debug("sending message $message FILLED -> ${it.id()}")
                it.sendTextAndAwait(message)
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
