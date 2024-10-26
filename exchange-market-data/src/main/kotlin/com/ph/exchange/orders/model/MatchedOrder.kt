package com.ph.exchange.orders.model

import java.math.BigDecimal
import java.math.RoundingMode.FLOOR
import java.math.RoundingMode.UNNECESSARY
import java.time.OffsetDateTime

data class MatchedOrder(
    val buyReference: String,
    val sellReference: String,
    val matchNominals: BigDecimal,
)
