package com.ph.exchange.marketdata.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.marketdata.MarketDataProducerWithStreamingProfile
import com.ph.exchange.marketdata.model.Instrument
import com.ph.exchange.marketdata.model.Quote
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
@TestProfile(MarketDataProducerWithStreamingProfile::class)
class FullApiMarketDataServiceTest : IntegrationTestBase() {

    @Inject
    private lateinit var instrumentsConnector: BasicWebSocketConnector

    @Inject
    private lateinit var quotesConnector: BasicWebSocketConnector

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @TestHTTPResource("/ws/instruments")
    private lateinit var instrumentsUri: URI

    @TestHTTPResource("/ws/quotes")
    private lateinit var quotesUri: URI

    @Test
    fun `test websocket connections dose not merge connection types`() {
        val latch1 = CountDownLatch(2)
        val latch2 = CountDownLatch(2)
        val instruments = mutableListOf<Instrument>()
        val quotes = mutableListOf<Quote>()

        instrumentsConnector
            .baseUri(instrumentsUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                if (latch1.count == 1L) {
                    instruments.addAll(
                        objectMapper.readValue(m, object : TypeReference<List<Instrument>>() {})
                    )
                }
                latch1.countDown()
            }
            .connectAndAwait()

        quotesConnector
            .baseUri(quotesUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                quotes.add(objectMapper.readValue(m, object : TypeReference<Quote>() {}))
                latch2.countDown()
            }
            .connectAndAwait()

        while (instruments.size < 1 && quotes.size < 2) {
            latch1.await(2, TimeUnit.SECONDS)
        }

        assertThat(instruments.size).isGreaterThanOrEqualTo(1)
        assertThat(quotes.size).isGreaterThanOrEqualTo(2)
    }
}
