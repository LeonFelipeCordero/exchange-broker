package com.ph.exchange.orders.model

import java.math.BigDecimal
import java.math.RoundingMode.FLOOR
import java.math.RoundingMode.UNNECESSARY
import java.time.OffsetDateTime

sealed interface BasicOrder {
    val instrument: String
    val nominals: BigDecimal
    val price: BigDecimal
    val amount: BigDecimal
    val currency: String
    val type: OrderType
    val trader: String
    val timestamp: OffsetDateTime

    fun loggingReference(): String {
        return "${instrument}_${nominals}_${price}"
    }
}

enum class OrderType {
    BUY,
    SELL
}

enum class OrderState {
    OPEN,
    FILLED
}

data class OrderSubmissionEvent(
    override val instrument: String,
    override val nominals: BigDecimal,
    override val price: BigDecimal,
    override val amount: BigDecimal,
    override val currency: String,
    override val type: OrderType,
    override val trader: String,
    override val timestamp: OffsetDateTime,
    val reference: String,
) : BasicOrder {
    fun toNewOrderDomain(): Order {
        return Order(
            orderReference = null,
            externalReference = reference,
            instrument = instrument,
            nominals = nominals,
            price = price,
            amount = amount,
            currency = currency,
            type = type,
            trader = trader,
            state = OrderState.OPEN,
            timestamp = timestamp
        )
    }
}

data class Order(
    override val instrument: String,
    override val nominals: BigDecimal,
    override val price: BigDecimal,
    override val amount: BigDecimal,
    override val currency: String,
    override val type: OrderType,
    override val trader: String,
    override val timestamp: OffsetDateTime,
    val orderReference: String?,
    val externalReference: String,
    val state: OrderState,
) : BasicOrder {

    val priceKey: BigDecimal
        get() {
            val rangeKey = price.setScale(1, FLOOR)
            return rangeKey.setScale(2, UNNECESSARY)
        }

    fun contraType(): OrderType {
        return if (type == OrderType.BUY)
            OrderType.SELL
        else
            OrderType.BUY
    }

    fun toOpenOrder(): OpenOrder {
        return OpenOrder(
            orderReference = orderReference!!,
            instrument = instrument,
            nominals = nominals,
            price = price,
            priceKey = priceKey,
            type = type,
            timestamp = timestamp,
        )
    }
}
