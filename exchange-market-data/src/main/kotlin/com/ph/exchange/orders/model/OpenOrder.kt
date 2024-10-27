package com.ph.exchange.orders.model

import java.math.BigDecimal
import java.time.OffsetDateTime

data class OpenOrder(
    val orderReference: String,
    val instrument: String,
    val nominals: BigDecimal,
    val price: BigDecimal,
    val priceKey: BigDecimal,
    val type: OrderType,
    val timestamp: OffsetDateTime,
)
