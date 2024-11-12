package com.ph.exchange.transactionaloutbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.Fixtures
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.orders.model.events.internal.OrderEventTypes
import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxInternalMessage
import com.ph.exchange.transactionaloutbox.service.TransactionalOutboxService
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class InternalTransactionalOutboxTest : IntegrationTestBase() {
    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    lateinit var transactionalOutbox: InternalTransactionalOutbox

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @Inject
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun setUp() {
        clean()
    }

    @Test
    fun `should fetched a message and send it and then mark it as sent`() {
        val orderFilledEvent = Fixtures.anOrderFilledEvent()
        val message = TransactionalOutboxInternalMessage(
            event = OrderEventTypes.ORDER_FILLED.name,
            message = objectMapper.writeValueAsString(orderFilledEvent),
        )
        transactionalOutboxService.persist(message)

        transactionalOutbox.task()

        entityManager.clear()
        val availableMessages = transactionalOutboxMessageRepository.findAll().list()
        assertThat(availableMessages).hasSize(1)
        assertThat(availableMessages.first().sent).isTrue()
        assertThat(availableMessages.first().sentAt).isNotNull()
    }
}
