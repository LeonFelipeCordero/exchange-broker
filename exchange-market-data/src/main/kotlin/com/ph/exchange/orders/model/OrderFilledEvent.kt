package com.ph.exchange.orders.model

import java.math.BigDecimal
import java.time.OffsetDateTime



data class OrderFilledEvent(
    val orderReference: String,
    val externalReference: String,
    val instrument: String,
    val nominals: BigDecimal,
    val originalPrice: BigDecimal,
    val matchedPriced: BigDecimal,
    val submissionTimestamp: OffsetDateTime,
    val filledTimestamp: OffsetDateTime,
)
