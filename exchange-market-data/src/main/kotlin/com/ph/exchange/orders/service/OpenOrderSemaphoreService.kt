package com.ph.exchange.orders.service

import com.ph.exchange.orders.model.OpenOrderSemaphore
import com.ph.exchange.orders.repository.OpenOrderSemaphoreRepository
import com.ph.exchange.orders.repository.entities.OpenOrderSemaphoreEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class OpenOrderSemaphoreService {

    @Inject
    private lateinit var openOrderSemaphoreRepository: OpenOrderSemaphoreRepository

    fun lock(orderReference: String) {
        openOrderSemaphoreRepository.persist(OpenOrderSemaphoreEntity(orderReference))
    }

    fun release(orderReference: String) {
        openOrderSemaphoreRepository.delete(orderReference)
    }

    fun release(ordersReference: List<String>) {
        openOrderSemaphoreRepository.delete(ordersReference)
    }

    fun findByReference(orderReference: String): OpenOrderSemaphore? {
        return openOrderSemaphoreRepository.findByReference(orderReference)?.toDomain()
    }
}
