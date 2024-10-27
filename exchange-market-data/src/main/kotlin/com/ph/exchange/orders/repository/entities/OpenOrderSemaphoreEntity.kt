package com.ph.exchange.orders.repository.entities

import com.ph.exchange.orders.model.OpenOrderSemaphore
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity(name = "order_matching_semaphore")
class OpenOrderSemaphoreEntity(
    @Id
    val orderReference: String,
    @CreationTimestamp
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
) : PanacheEntityBase {
    companion object {
        fun fromDomain(openOrderSemaphore: OpenOrderSemaphore): OpenOrderSemaphoreEntity {
            return OpenOrderSemaphoreEntity(
                orderReference = openOrderSemaphore.orderReference,
                createdAt = openOrderSemaphore.createdAt,
            )
        }
    }

    fun toDomain(): OpenOrderSemaphore {
        return OpenOrderSemaphore(
            orderReference = orderReference,
            createdAt = createdAt,
        )
    }
}
