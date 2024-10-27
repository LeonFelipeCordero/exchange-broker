package com.ph.exchange.orders.model

import java.math.BigDecimal

data class MatchedOrder(
    val buyReference: String,
    val sellReference: String,
    val matchNominals: BigDecimal,
)
