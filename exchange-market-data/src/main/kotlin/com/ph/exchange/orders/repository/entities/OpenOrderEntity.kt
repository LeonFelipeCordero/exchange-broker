package com.ph.exchange.orders.repository.entities

import com.ph.exchange.orders.model.OpenOrder
import com.ph.exchange.orders.model.OrderType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity(name = "open_orders")
class OpenOrderEntity(
    @Id
    val orderReference: String,
    val instrument: String,
    val nominals: BigDecimal,
    val price: BigDecimal,
    val priceKey: BigDecimal,
    val type: String,
    val timestamp: OffsetDateTime,
    @CreationTimestamp
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @UpdateTimestamp
    val updatedAt: OffsetDateTime = OffsetDateTime.now()
) : PanacheEntityBase {
    companion object {
        fun fromDomain(openOrder: OpenOrder): OpenOrderEntity {
            return OpenOrderEntity(
                orderReference = openOrder.orderReference!!,
                instrument = openOrder.instrument,
                nominals = openOrder.nominals,
                price = openOrder.price,
                priceKey = openOrder.priceKey,
                type = openOrder.type.name,
                timestamp = openOrder.timestamp,
            )
        }
    }

    fun toDomain(): OpenOrder {
        return OpenOrder(
            orderReference = orderReference,
            instrument = instrument,
            nominals = nominals,
            price = price,
            priceKey = priceKey,
            type = OrderType.valueOf(type),
            timestamp = timestamp
        )
    }
}