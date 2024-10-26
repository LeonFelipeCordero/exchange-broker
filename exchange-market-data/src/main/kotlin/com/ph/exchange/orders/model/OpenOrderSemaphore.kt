package com.ph.exchange.orders.model

import java.time.OffsetDateTime

data class OpenOrderSemaphore(
    val orderReference: String,
    val createdAt: OffsetDateTime,
)
