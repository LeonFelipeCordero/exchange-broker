package com.ph.exchange.marketdata.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.marketdata.model.Instrument
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.websockets.next.BasicWebSocketConnector
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@QuarkusTest
class InstrumentsApiMarketDataServiceTest : IntegrationTestBase() {

    @Inject
    private lateinit var basicWebSocketConnector: BasicWebSocketConnector

    @Inject
    private lateinit var basicWebSocketConnector2: BasicWebSocketConnector

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @TestHTTPResource("/ws/instruments")
    private lateinit var instrumentsUri: URI

    @Test
    fun `test websocket connection returns instrument updates to all subscribers`() {
        val latch1 = CountDownLatch(2)
        val latch2 = CountDownLatch(2)
        val changedInstruments1 = mutableListOf<Instrument>()
        val changedInstruments2 = mutableListOf<Instrument>()

        basicWebSocketConnector
            .baseUri(instrumentsUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                if (latch1.count == 1L) {
                    changedInstruments1.addAll(objectMapper.readValue(m, object : TypeReference<List<Instrument>>() {}))
                }
                latch1.countDown()
            }
            .connectAndAwait()

        basicWebSocketConnector2
            .baseUri(instrumentsUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                if (latch2.count == 1L) {
                    changedInstruments2.addAll(objectMapper.readValue(m, object : TypeReference<List<Instrument>>() {}))
                }
                latch2.countDown()
            }
            .connectAndAwait()

        while (changedInstruments1.size < 1) {
            latch1.await(2, TimeUnit.SECONDS)
        }

        assertThat(changedInstruments1).hasSizeGreaterThanOrEqualTo(1)
        assertThat(changedInstruments2).hasSizeGreaterThanOrEqualTo(1)
        assertThat(changedInstruments1).isEqualTo(changedInstruments2)
    }
}
