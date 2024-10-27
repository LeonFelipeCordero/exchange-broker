package com.ph.exchange.transactionaloutbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.Fixtures
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.orders.model.events.internal.OrderEventTypes
import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxInternalMessage
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class TransactionalOutboxServiceTest : IntegrationTestBase() {

    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        clean()
    }

    @Test
    fun `should persis and fetch value`() {
        val orderFilledEvent = Fixtures.anOrderFilledEvent()
        val message = TransactionalOutboxInternalMessage(
            event = OrderEventTypes.ORDER_FILLED.name,
            message = objectMapper.writeValueAsString(orderFilledEvent),
        )
        transactionalOutboxService.persist(message)

        val availableMessages = transactionalOutboxService.getAvailableMessages()

        assertThat(availableMessages).hasSize(1)
        assertThat(availableMessages.first().message).isEqualTo(objectMapper.writeValueAsString(orderFilledEvent))
    }

    @Test
    fun `should not fetched a sent message`() {
        val anOrderFilledEvent = Fixtures.anOrder()
        val message = TransactionalOutboxInternalMessage(
            event = OrderEventTypes.ORDER_FILLED.name,
            message = objectMapper.writeValueAsString(anOrderFilledEvent),
        )
        transactionalOutboxService.persist(message)

        var availableMessages = transactionalOutboxService.getAvailableMessages()
        transactionalOutboxService.markAsSent(availableMessages.first().sequence!!)

        availableMessages = transactionalOutboxService.getAvailableMessages()
        assertThat(availableMessages).hasSize(0)
    }
}
