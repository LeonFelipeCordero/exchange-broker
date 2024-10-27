package com.ph.exchange

import com.ph.exchange.orders.repository.MatchedOrderRepository
import com.ph.exchange.orders.repository.OpenOrderRepository
import com.ph.exchange.orders.repository.OpenOrderSemaphoreRepository
import com.ph.exchange.orders.repository.OrderRepository
import com.ph.exchange.transactionaloutbox.repository.TransactionalOutboxInternalMessageRepository
import io.quarkus.test.common.QuarkusTestResource
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@Transactional
@QuarkusTestResource(TimescaleDBTestResource::class)
open class IntegrationTestBase {

    @Inject
    protected lateinit var openOrderSemaphoreRepository: OpenOrderSemaphoreRepository

    @Inject
    protected lateinit var openOrderRepository: OpenOrderRepository

    @Inject
    protected lateinit var orderRepository: OrderRepository

    @Inject
    protected lateinit var matchedOrderRepository: MatchedOrderRepository

    @Inject
    protected lateinit var transactionalOutboxInternalMessageRepository: TransactionalOutboxInternalMessageRepository

    protected fun clean() {
        matchedOrderRepository.deleteAll()
        openOrderRepository.deleteAll()
        openOrderSemaphoreRepository.deleteAll()
        orderRepository.deleteAll()
        transactionalOutboxInternalMessageRepository.deleteAll()
    }
}
