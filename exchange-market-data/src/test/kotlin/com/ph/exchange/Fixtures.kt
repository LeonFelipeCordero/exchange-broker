package com.ph.exchange

import com.ph.exchange.orders.model.OpenOrder
import com.ph.exchange.orders.model.Order
import com.ph.exchange.orders.model.OrderState
import com.ph.exchange.orders.model.OrderType
import com.ph.exchange.orders.model.events.internal.OrderFilledEvent
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

object Fixtures {
    fun anOrder(
        orderReference: String? = null,
        instrument: String = "DE1122334455",
        nominals: BigDecimal = BigDecimal.ONE,
        price: BigDecimal = BigDecimal.ONE,
        amount: BigDecimal = nominals * price,
        type: OrderType = OrderType.BUY,
        timestamp: OffsetDateTime = OffsetDateTime.now(),
        externalReference: String = "external_reference",
        state: OrderState = OrderState.OPEN,
    ): Order {
        return Order(
            orderReference = orderReference,
            instrument = instrument,
            nominals = nominals,
            price = price,
            amount = amount,
            type = type,
            currency = "EUR",
            trader = "a",
            institution = "test",
            externalReference = externalReference,
            state = state,
            timestamp = timestamp,
        )
    }

    fun anOpenOrder(
        orderReference: String = UUID.randomUUID().toString(),
        instrument: String = "DE1122334455",
        nominals: BigDecimal = BigDecimal.ONE,
        price: BigDecimal = BigDecimal.ONE,
        priceKey: BigDecimal = BigDecimal.ONE,
        type: OrderType = OrderType.BUY,
        timestamp: OffsetDateTime = OffsetDateTime.now(),
    ): OpenOrder {
        return OpenOrder(
            orderReference = orderReference,
            instrument = instrument,
            nominals = nominals,
            price = price,
            priceKey = priceKey,
            type = type,
            timestamp = timestamp,
        )
    }

    fun anOrderFilledEvent(
        orderReference: String = "test12345",
        instrument: String = "DE1122334455",
        nominals: BigDecimal = BigDecimal.ONE,
        price: BigDecimal = BigDecimal.ONE,
        timestamp: OffsetDateTime = OffsetDateTime.now().minusHours(1),
        externalReference: String = "external_reference",
        institution: String = "test"
    ): OrderFilledEvent {
        return OrderFilledEvent(
            orderReference = orderReference,
            externalReference = externalReference,
            instrument = instrument,
            nominals = nominals,
            originalPrice = price,
            institution = institution,
            submissionTimestamp = timestamp,
            filledTimestamp = OffsetDateTime.now()
        )
    }
}
