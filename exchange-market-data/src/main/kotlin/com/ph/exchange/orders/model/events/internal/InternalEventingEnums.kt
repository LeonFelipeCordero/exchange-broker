package com.ph.exchange.orders.model.events.internal

enum class OrderEventTypes {
    ORDER_FILLED,
    ORDER_PARTIALLY_FILLED,
    ORDER_CANCELLED,
}

enum class InternalEventingMessageStatus {
    OK,
    ERROR_HANDLED,
}
