package com.ph.exchange.orders.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.OrderSubmissionEvent
import com.ph.exchange.orders.service.OrderMatcher
import io.quarkus.logging.Log
import io.smallrye.common.annotation.Blocking
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Incoming

@ApplicationScoped
class OrderCreatedHandler {

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Inject
    private lateinit var orderMatcher: OrderMatcher

    @Blocking
    @Incoming("exchange_order_created")
    fun handler(orderCreationMessage: String) {
        val order = objectMapper.readValue(orderCreationMessage, object : TypeReference<OrderSubmissionEvent>() {})
        Log.debug("got message from for order creation: ${order.loggingReference()}")
        orderMatcher.processOrder(order.toOrder())
    }
}
