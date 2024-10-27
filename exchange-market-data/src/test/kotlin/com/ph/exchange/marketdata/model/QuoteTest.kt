package com.ph.exchange.marketdata.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.ZonedDateTime

class QuoteTest {

    @Test
    fun updatePrice() {
        val quote = Quote(
            isin = "DE1122334455",
            timestamp = ZonedDateTime.now(),
            currency = "EUR",
            price = BigDecimal("12.50").setScale(2),
        )
        Thread.sleep(100)
        val updatedQuote = quote.updatePrice()
        assertThat(quote.price).isNotEqualTo(updatedQuote.price)
        assertThat(quote.timestamp).isBefore(updatedQuote.timestamp)
    }
}
