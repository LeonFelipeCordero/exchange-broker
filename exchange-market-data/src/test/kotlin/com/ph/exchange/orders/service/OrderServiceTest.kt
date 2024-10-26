package com.ph.exchange.orders.service

import com.ph.exchange.Fixtures.anOrder
import com.ph.exchange.IntegrationTestBase
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@Transactional
class OrderServiceTest : IntegrationTestBase() {

    @Inject
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        clean()
    }

    @Test
    fun `should save and get the id from an order`() {
        val savedOrder = orderService.persist(anOrder(orderReference = null))
        assertThat(savedOrder.orderReference).isNotNull()
    }
}