package com.ph.exchange.orders.service

import com.ph.exchange.orders.model.MatchedOrder
import com.ph.exchange.orders.repository.MatchedOrderRepository
import com.ph.exchange.orders.repository.entities.MatchedOrderEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class MatchedOrderService {

    @Inject
    private lateinit var matchedOrderRepository: MatchedOrderRepository

    fun persist(marchedOrder: MatchedOrder): MatchedOrder {
        val entity = MatchedOrderEntity.fromDomain(marchedOrder)
        matchedOrderRepository.persist(entity)
        return entity.toDomain()
    }

    fun persist(matchedOrders: List<MatchedOrder>) {
        val entities = matchedOrders.map { MatchedOrderEntity.fromDomain(it) }
        matchedOrderRepository.persist(entities)
    }
}
