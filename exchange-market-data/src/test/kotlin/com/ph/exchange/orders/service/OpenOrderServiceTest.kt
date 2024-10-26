package com.ph.exchange.orders.service

import com.ph.exchange.Fixtures.anOpenOrder
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.orders.model.OrderType
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@QuarkusTest
@Transactional
class OpenOrderServiceTest : IntegrationTestBase() {
    val instrument = "1"
    val pricesAndKeys = listOf(
        Pair(BigDecimal("12.54"), BigDecimal("12.545803")),
        Pair(BigDecimal("12.54"), BigDecimal("12.5499")),
        Pair(BigDecimal("12.54"), BigDecimal("12.5400")),
        Pair(BigDecimal("12.55"), BigDecimal("12.55555")),
        Pair(BigDecimal("12.53"), BigDecimal("12.5399"))
    )

    @Inject
    private lateinit var openOrderService: OpenOrderService

    @BeforeEach
    fun setUp() {
        clean()
        openOrderService.persist(pricesAndKeys.map {
            anOpenOrder(instrument = instrument, priceKey = it.first, price = it.second)
        })
    }

    @Test
    fun `should fetch a list of orders under the same key`() {
        val firstKeyOrders =
            openOrderService.findByInstrumentTypeAndKey(
                instrument, OrderType.BUY, BigDecimal("12.54")
            )
        val secondKeyOrders =
            openOrderService.findByInstrumentTypeAndKey(
                instrument, OrderType.BUY, BigDecimal("12.53")
            )
        val thirdKeyOrders =
            openOrderService.findByInstrumentTypeAndKey(
                instrument, OrderType.BUY, BigDecimal("12.55")
            )

        assertThat(firstKeyOrders).hasSize(3)
        assertThat(firstKeyOrders.map { it.priceKey }.toSet()).isEqualTo(setOf(BigDecimal("12.54")))
        assertThat(secondKeyOrders).hasSize(1)
        assertThat(secondKeyOrders.map { it.priceKey }.toSet()).isEqualTo(setOf(BigDecimal("12.53")))
        assertThat(thirdKeyOrders).hasSize(1)
        assertThat(thirdKeyOrders.map { it.priceKey }.toSet()).isEqualTo(setOf(BigDecimal("12.55")))
    }

    @Test
    fun `should not fetch a single with a wrong key`() {
        val unknownKey =
            openOrderService.findByInstrumentTypeAndKey(instrument, OrderType.BUY, BigDecimal("12.45"))

        assertThat(unknownKey).isEmpty()
    }

    @Test
    fun `should not fetch a single with a wrong instrument`() {
        val unknownKey =
            openOrderService.findByInstrumentTypeAndKey("a", OrderType.BUY, BigDecimal("12.54"))

        assertThat(unknownKey).isEmpty()
    }

    @Test
    fun `should not fetch a single with a wrong order type`() {
        val unknownKey =
            openOrderService.findByInstrumentTypeAndKey(instrument, OrderType.SELL, BigDecimal("12.54"))

        assertThat(unknownKey).isEmpty()
    }

    @Test
    fun `should delete all given orders`() {
        val openOrders =
            openOrderService.findByInstrumentTypeAndKey(
                instrument, OrderType.BUY, BigDecimal("12.54")
            )
        openOrderService.removeAll(openOrders.map { it.orderReference!! })
        val foundByKey =
            openOrderService.findByInstrumentTypeAndKey(
                instrument, OrderType.BUY, BigDecimal("12.54")
            )
        assertThat(foundByKey).isEmpty()
    }
}