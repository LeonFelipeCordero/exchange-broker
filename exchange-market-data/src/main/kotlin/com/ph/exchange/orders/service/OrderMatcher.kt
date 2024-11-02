package com.ph.exchange.orders.service

import com.ph.exchange.orders.model.MatchedOrder
import com.ph.exchange.orders.model.OpenOrder
import com.ph.exchange.orders.model.Order
import com.ph.exchange.orders.model.OrderType
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.math.BigDecimal

@ApplicationScoped
class OrderMatcher {
    @Inject
    private lateinit var orderService: OrderService

    @Inject
    private lateinit var openOrderService: OpenOrderService

    @Inject
    private lateinit var openOrderSemaphoreService: OpenOrderSemaphoreService

    @Inject
    private lateinit var matchedOrderService: MatchedOrderService

    @Inject
    private lateinit var entityManager: EntityManager

    @WithSpan("order.matching")
    @Transactional
    fun processOrder(order: Order) {
        // todo validate if instrument is tradeable
        // to make it possible is has to subscribe and keep in memory a list of tradeable instruments
        // think how to get a fast cache later (sqlite maybe? )
        val savedOrder = orderService.persist(order)
        matchOrder(savedOrder)
    }

    private fun matchOrder(order: Order) {
        val key = order.priceKey
        val contraType = order.contraType()
        val contraOrders = openOrderService.findByInstrumentTypeAndKey(order.instrument, contraType, key)

        var resultOrder = order.toOpenOrder()
        val ordersToPersist = mutableListOf<OpenOrder>()
        val matchedOrders = mutableListOf<MatchedOrder>()
        val ordersToDelete = mutableListOf<String>()
        val filledOrders = mutableListOf<String>()
        for (contraOrder in contraOrders) {
            // todo this can be in a single query - but maybe better locking directly in the DB
            val lockOrder = openOrderSemaphoreService.findByReference(contraOrder.orderReference)
            if (lockOrder != null) {
                continue
            }
            openOrderSemaphoreService.lock(contraOrder.orderReference)

            Log.debug("Matching order ${resultOrder.orderReference} to ${contraOrder.orderReference}")

            ordersToDelete.add(contraOrder.orderReference)

            val matchedNominals = contraOrder.nominals.min(resultOrder.nominals)
            val matchedContraOrder = contraOrder.copy(nominals = contraOrder.nominals - matchedNominals)
            resultOrder = resultOrder.copy(nominals = resultOrder.nominals - matchedNominals)

            val matchedOrder = if (contraType == OrderType.BUY) {
                MatchedOrder(
                    buyReference = matchedContraOrder.orderReference,
                    sellReference = resultOrder.orderReference,
                    matchNominals = matchedNominals,
                )
            } else {
                MatchedOrder(
                    buyReference = resultOrder.orderReference,
                    sellReference = matchedContraOrder.orderReference,
                    matchNominals = matchedNominals,
                )
            }
            matchedOrders.add(matchedOrder)

            if (matchedContraOrder.nominals > BigDecimal.ZERO) {
                ordersToPersist.add(matchedContraOrder)
            } else {
                filledOrders.add(matchedContraOrder.orderReference)
            }

            // create domain to do comparison easier
            if (resultOrder.nominals == BigDecimal("0.00")) {
                break
            }
        }

        if (resultOrder.nominals > BigDecimal.ZERO) {
            ordersToPersist.add(resultOrder)
        } else {
            filledOrders.add(resultOrder.orderReference)
        }
        openOrderService.removeAll(ordersToDelete)
        // todo this could goto the test and not here
        entityManager.clear()
        openOrderService.persist(ordersToPersist)
        matchedOrderService.persist(matchedOrders)
        orderService.markOrdersAsFilled(filledOrders)
        openOrderSemaphoreService.release(ordersToDelete)
    }
}
