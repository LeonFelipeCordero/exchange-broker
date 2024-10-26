package com.ph.exchange.orders.service

import com.ph.exchange.orders.model.OpenOrder
import com.ph.exchange.orders.model.OrderType
import com.ph.exchange.orders.repository.entities.OpenOrderEntity
import com.ph.exchange.orders.repository.OpenOrderRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.math.BigDecimal

@ApplicationScoped
class OpenOrderService {

    @Inject
    private lateinit var openOrderRepository: OpenOrderRepository

    fun persist(openOrder: OpenOrder) {
        openOrderRepository.persist(OpenOrderEntity.fromDomain(openOrder))
    }

    fun persist(openOrders: List<OpenOrder>) {
        val entities = openOrders.map { OpenOrderEntity.fromDomain(it) }
        openOrderRepository.persist(entities)
    }

    fun findByInstrumentTypeAndKey(
        instrument: String,
        orderType: OrderType,
        key: BigDecimal
    ): List<OpenOrder> {
        return openOrderRepository
            .findByInstrumentTypeAndKey(instrument, orderType.name, key)
            .map { it.toDomain() }
    }

    fun removeAll(openOrdersReference: List<String>) {
        return openOrderRepository.delete(openOrdersReference)
    }
}