package com.ph.exchange.orders.model.events.internal

import com.ph.exchange.orders.model.Order
import java.math.BigDecimal
import java.time.OffsetDateTime

/**
 * And order-filled event can be more informative my adding the parties that are matched
 */
data class OrderFilledEvent(
    val orderReference: String,
    val externalReference: String,
    val instrument: String,
    val nominals: BigDecimal,
    val originalPrice: BigDecimal,
    val institution: String,
    val submissionTimestamp: OffsetDateTime,
    val filledTimestamp: OffsetDateTime,
) {
    companion object {
        fun fromDomain(order: Order): OrderFilledEvent {
            return OrderFilledEvent(
                orderReference = order.orderReference!!,
                externalReference = order.externalReference,
                instrument = order.instrument,
                nominals = order.nominals,
                originalPrice = order.price,
                institution = order.institution,
                submissionTimestamp = order.timestamp,
                filledTimestamp = OffsetDateTime.now()
            )
        }
    }
}
