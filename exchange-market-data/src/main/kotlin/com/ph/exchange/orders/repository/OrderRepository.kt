package com.ph.exchange.orders.repository

import com.ph.exchange.orders.repository.entities.OrderEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OrderRepository : PanacheRepository<OrderEntity> {
    fun findByExternalReference(externalReference: String): OrderEntity {
        return find("externalReference = ?1", externalReference).singleResult()
    }

    fun findAllByExternalReference(orderReferences: List<String>): List<OrderEntity> {
        return list("orderReference in ?1", orderReferences)
    }

    fun marketAsFilled(orderReferences: List<String>) {
        update("state = 'FILLED', updatedAt = CURRENT_TIMESTAMP where orderReference in ?1", orderReferences)
    }
}