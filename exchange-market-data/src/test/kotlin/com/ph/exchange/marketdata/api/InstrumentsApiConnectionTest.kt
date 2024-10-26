package com.ph.exchange.marketdata.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.marketdata.MarketDataProducerWithoutStreamingProfile
import com.ph.exchange.marketdata.model.Instrument
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.quarkus.websockets.next.BasicWebSocketConnector
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestProfile(MarketDataProducerWithoutStreamingProfile::class)
class InstrumentsApiConnectionTest : IntegrationTestBase() {

    @Inject
    private lateinit var basicWebSocketConnector: BasicWebSocketConnector

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @TestHTTPResource("/ws/instruments")
    private lateinit var instrumentsUri: URI

    @Test
    fun `test websocket connection returns initial batch instruments`() {
        val latch = CountDownLatch(1)
        val initialLoad = mutableListOf<Instrument>()

        basicWebSocketConnector
            .baseUri(instrumentsUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                initialLoad.addAll(objectMapper.readValue(m, object : TypeReference<List<Instrument>>() {}))
                latch.countDown()
            }
            .connectAndAwait()

        latch.await(1, TimeUnit.SECONDS)

        assertThat(initialLoad).hasSize(5)
    }
}
