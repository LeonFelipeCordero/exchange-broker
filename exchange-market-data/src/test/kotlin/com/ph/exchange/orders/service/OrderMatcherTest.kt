package com.ph.exchange.orders.service

import com.ph.exchange.Fixtures.anOrder
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.orders.model.OrderState
import com.ph.exchange.orders.model.OrderType
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@QuarkusTest
class OrderMatcherTest : IntegrationTestBase() {
    private val instrument = "1"
    private val targetPriceAndKey = Pair(BigDecimal("2.55"), BigDecimal("2.555555"))
    private val externalReference = "abc"

    @Inject
    private lateinit var orderMatcher: OrderMatcher

    @BeforeEach
    fun setUp() {
        clean()
    }

    @Test
    fun `should put an incoming order without matching in open orders`() {
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                nominals = BigDecimal("2.00"),
                price = targetPriceAndKey.second,
                externalReference = externalReference,
            ),
        )
        val order = orderRepository.findByExternalReference(externalReference).toDomain()

        val storedOpenOrder = openOrderRepository.findByInstrumentTypeAndKey(
            instrument = instrument,
            orderType = order.type.name,
            key = order.priceKey,
        )
        val orders = orderRepository.findAll().list()

        assertThat(storedOpenOrder).hasSize(1)
        assertThat(storedOpenOrder.first().orderReference).isEqualTo(order.orderReference)
        assertThat(orders.map { it.state }.toSet()).isEqualTo(setOf(OrderState.OPEN.name))
    }

    @Test
    fun `should match an incoming order with matching open orders`() {
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
            ),
        )
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
            ),
        )
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
                type = OrderType.SELL,
                nominals = BigDecimal("2.00"),
            ),
        )

        val openOrders = openOrderRepository.findAll().list()
        val locks = openOrderSemaphoreRepository.findAll().list()
        val matchedOrders = matchedOrderRepository.findAll().list()
        val orders = orderRepository.findAll().list()

        assertThat(openOrders).isEmpty()
        assertThat(matchedOrders).hasSize(2)
        assertThat(matchedOrders[0].matchedNominals.toInt()).isEqualTo(1)
        assertThat(matchedOrders[1].matchedNominals.toInt()).isEqualTo(1)
        assertThat(locks).isEmpty()
        assertThat(orders.map { it.state }.toSet()).isEqualTo(setOf(OrderState.FILLED.name))
    }

    @Test
    fun `should match an incoming and save remaining`() {
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
            ),
        )
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
                type = OrderType.SELL,
                nominals = BigDecimal("2.00"),
                externalReference = externalReference,
            ),
        )
        val order = orderRepository.findByExternalReference(externalReference).toDomain()

        val openOrders = openOrderRepository.findAll().list()
        val locks = openOrderSemaphoreRepository.findAll().list()
        val matchedOrders = matchedOrderRepository.findAll().list()
        val orders = orderRepository.findAll().list()

        assertThat(openOrders).hasSize(1)
        assertThat(matchedOrders).hasSize(1)
        assertThat(matchedOrders[0].matchedNominals.toInt()).isEqualTo(1)
        assertThat(matchedOrders[0].matchedOrderId.sellReference).isEqualTo(order.orderReference)
        assertThat(openOrders.first().orderReference).isEqualTo(order.orderReference)
        assertThat(locks).isEmpty()
        assertThat(orders.map { it.state }.toSet()).isEqualTo(setOf(OrderState.OPEN.name, OrderState.FILLED.name))
    }

    @Test
    fun `should match an incoming and save remaining of the matched account`() {
        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
                nominals = BigDecimal("2.00"),
                externalReference = externalReference,
            ),
        )
        val order = orderRepository.findByExternalReference(externalReference).toDomain()

        orderMatcher.processOrder(
            anOrder(
                instrument = instrument,
                price = targetPriceAndKey.second,
                type = OrderType.SELL,
            ),
        )

        val openOrders = openOrderRepository.findAll().list()
        val locks = openOrderSemaphoreRepository.findAll().list()
        val matchedOrders = matchedOrderRepository.findAll().list()
        val orders = orderRepository.findAll().list()

        assertThat(openOrders).hasSize(1)
        assertThat(matchedOrders[0].matchedNominals.toInt()).isEqualTo(1)
        assertThat(matchedOrders[0].matchedOrderId.buyReference).isEqualTo(order.orderReference)
        assertThat(openOrders.first().orderReference).isEqualTo(order.orderReference)
        assertThat(locks).isEmpty()
        assertThat(orders.map { it.state }.toSet()).isEqualTo(setOf(OrderState.OPEN.name, OrderState.FILLED.name))
    }
}
