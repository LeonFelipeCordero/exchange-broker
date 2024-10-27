package com.ph.exchange.orders.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.orders.model.Order
import com.ph.exchange.orders.model.events.internal.OrderEventTypes
import com.ph.exchange.orders.model.events.internal.OrderFilledEvent
import com.ph.exchange.orders.repository.OrderRepository
import com.ph.exchange.orders.repository.entities.OrderEntity
import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxInternalMessage
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
            .map { OrderFilledEvent.fromDomain(it) }
            .forEach {
                val message = objectMapper.writeValueAsString(it)
                transactionalOutboxService.persist(
                    TransactionalOutboxInternalMessage(
                        event = OrderEventTypes.ORDER_FILLED.name,
                        message = message,
                    ),
                )
            }
    }
}
