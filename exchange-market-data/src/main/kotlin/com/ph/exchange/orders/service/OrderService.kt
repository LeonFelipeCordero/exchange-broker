package com.ph.exchange.orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.Order
import com.ph.exchange.orders.model.OrderEventTypes
import com.ph.exchange.orders.repository.entities.OrderEntity
import com.ph.exchange.orders.repository.OrderRepository
import com.ph.exchange.transactionaloutbox.TransactionalOutboxInternalMessage
import com.ph.exchange.transactionaloutbox.service.TransactionalOutboxService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class OrderService {

    @Inject
    private lateinit var orderRepository: OrderRepository

    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    private lateinit var objectMapper: ObjectMapper

    fun persist(order: Order): Order {
        val entity = OrderEntity.fromDomain(order)
        orderRepository.persistAndFlush(entity)
        return entity.toDomain()
    }

    fun markOrdersAsFilled(orderReferences: List<String>) {
        orderRepository.marketAsFilled(orderReferences)

        orderRepository.findAllByExternalReference(orderReferences)
            .map { it.toDomain() }
            .forEach {
                val message = objectMapper.writeValueAsString(it)
                transactionalOutboxService.persist(
                    TransactionalOutboxInternalMessage(
                        event = OrderEventTypes.ORDER_FILLED.name,
                        message = message,
                    )
                )
            }
    }
}
