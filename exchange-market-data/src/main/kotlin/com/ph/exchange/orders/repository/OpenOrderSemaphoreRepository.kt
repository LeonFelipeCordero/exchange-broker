package com.ph.exchange.orders.repository

import com.ph.exchange.orders.repository.entities.OpenOrderSemaphoreEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class OpenOrderSemaphoreRepository : PanacheRepository<OpenOrderSemaphoreEntity> {
    fun findByReference(orderReference: String): OpenOrderSemaphoreEntity? {
        return find("orderReference = ?1", orderReference).firstResult()
    }

    fun delete(orderReference: String) {
        delete("orderReference = ?1", orderReference)
    }

    fun delete(ordersReference: List<String>) {
        delete("orderReference in ?1", ordersReference)
    }
}
