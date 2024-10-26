package com.ph.exchange.orders.service

import com.ph.exchange.IntegrationTestBase
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
@Transactional
class OpenOrderSemaphoreServiceTest : IntegrationTestBase() {
    val orderReference = "12345"

    @Inject
    private lateinit var openOrderSemaphoreService: OpenOrderSemaphoreService

    @BeforeEach
    fun setUp() {
        clean()
    }

    @Test
    fun `should lock and find order locked by reference`() {
        openOrderSemaphoreService.lock(orderReference)

        val findByReference = openOrderSemaphoreService.findByReference(orderReference)
        assertThat(findByReference?.orderReference).isEqualTo(orderReference)
    }

    @Test
    fun `should not find order if not locked`() {
        val findByReference = openOrderSemaphoreService.findByReference(orderReference)
        assertThat(findByReference?.orderReference).isNull()
    }

    @Test
    fun `should release lock and not find order locked by reference`() {
        openOrderSemaphoreService.lock(orderReference)
        openOrderSemaphoreService.release(orderReference)

        val findByReference = openOrderSemaphoreService.findByReference(orderReference)
        assertThat(findByReference?.orderReference).isNull()
    }

    @Test
    fun `should batch release lock and not find order locked by reference`() {
        openOrderSemaphoreService.lock(orderReference)
        openOrderSemaphoreService.lock("${orderReference}6")
        openOrderSemaphoreService.release(listOf(orderReference, "${orderReference}6"))

        val findByReference1 = openOrderSemaphoreService.findByReference(orderReference)
        val findByReference2 = openOrderSemaphoreService.findByReference("${orderReference}6")
        assertThat(findByReference1?.orderReference).isNull()
        assertThat(findByReference2?.orderReference).isNull()
    }
}