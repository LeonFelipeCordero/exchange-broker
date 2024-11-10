package com.ph.exchange.orders.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.events.internal.OrderFilledEvent
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.quarkus.websockets.next.OpenConnections
import io.smallrye.common.annotation.Blocking
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming

@ApplicationScoped
class OrderFilledHandler {
    private val ordersConnections = mutableMapOf<String, String>()

    @Inject
    private lateinit var openConnections: OpenConnections

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Blocking
    @WithSpan("order_filled")
    @Incoming("exchange_order_filled")
    fun consume(message: String) {
        val order = objectMapper.readValue(message, object : TypeReference<OrderFilledEvent>() {})
        val connection = openConnections.find {
            ordersConnections.keys.contains(it.id()) && ordersConnections[it.id()] == order.institution
        }
        connection?.sendTextAndAwait(message)
    }

    fun addConnection(wsConnId: String, institution: String) {
        ordersConnections[wsConnId] = institution
    }

    fun removeConnection(wsConnId: String) {
        ordersConnections.remove(wsConnId)
    }
}
