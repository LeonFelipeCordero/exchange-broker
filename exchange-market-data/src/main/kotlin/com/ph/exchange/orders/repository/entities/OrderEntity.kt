package com.ph.exchange.orders.repository.entities

import com.ph.exchange.orders.model.Order
import com.ph.exchange.orders.model.OrderState
import com.ph.exchange.orders.model.OrderType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    val orderReference: String? = null,
    val externalReference: String,
    val instrument: String,
    val nominals: BigDecimal,
    val price: BigDecimal,
    val amount: BigDecimal,
    val currency: String,
    val type: String,
    val trader: String,
    val state: String,
    val timestamp: OffsetDateTime,
    @CreationTimestamp
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @UpdateTimestamp
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
) : PanacheEntityBase {
    companion object {
        fun fromDomain(order: Order): OrderEntity {
            return OrderEntity(
                orderReference = order.orderReference,
                externalReference = order.externalReference,
                instrument = order.instrument,
                nominals = order.nominals,
                price = order.price,
                amount = order.amount,
                currency = order.currency,
                type = order.type.name,
                trader = order.trader,
                state = order.state.name,
                timestamp = order.timestamp,
            )
        }
    }

    fun toDomain(): Order {
        return Order(
            orderReference = orderReference,
            externalReference = externalReference,
            instrument = instrument,
            nominals = nominals,
            price = price,
            amount = amount,
            currency = currency,
            type = OrderType.valueOf(type),
            trader = trader,
            state = OrderState.valueOf(state),
            timestamp = timestamp
        )
    }
}