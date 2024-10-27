package com.ph.exchange.marketdata.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.IntegrationTestBase
import com.ph.exchange.marketdata.model.Quote
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
class QuotesApiMarketDataServiceTest : IntegrationTestBase() {

    @Inject
    private lateinit var basicWebSocketConnector: BasicWebSocketConnector

    @Inject
    private lateinit var basicWebSocketConnector2: BasicWebSocketConnector

    @Inject
    private lateinit var objectMapper: ObjectMapper

    @TestHTTPResource("/ws/quotes")
    private lateinit var quotesUri: URI

    @Test
    fun `test websocket connection returns new quotes updates to all subscribers`() {
        val latch1 = CountDownLatch(2)
        val latch2 = CountDownLatch(2)
        val quotes1 = mutableListOf<Quote>()
        val quotes2 = mutableListOf<Quote>()

        basicWebSocketConnector
            .baseUri(quotesUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                quotes1.add(objectMapper.readValue(m, object : TypeReference<Quote>() {}))
                latch1.countDown()
            }
            .connectAndAwait()

        basicWebSocketConnector2
            .baseUri(quotesUri)
            .executionModel(BasicWebSocketConnector.ExecutionModel.NON_BLOCKING)
            .onTextMessage { _, m ->
                quotes2.add(objectMapper.readValue(m, object : TypeReference<Quote>() {}))
                latch2.countDown()
            }
            .connectAndAwait()

        while (quotes1.size < 2) {
            latch1.await(2, TimeUnit.SECONDS)
        }

        assertThat(quotes1).hasSizeGreaterThanOrEqualTo(2)
        assertThat(quotes1).containsSequence(quotes2)
    }
}
