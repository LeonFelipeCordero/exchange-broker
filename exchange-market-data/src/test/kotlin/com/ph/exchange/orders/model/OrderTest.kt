package com.ph.exchange.orders.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.OffsetDateTime


class OrderTest {

    @ParameterizedTest
    @CsvSource(textBlock =
        """
        12.4059084,12.40
        5.8723490,5.80
        8490284.341238,8490284.30
        0.00,0.00"""
    )
    fun `should create the key for an order correct`(
        price: BigDecimal,
        expectedKey: BigDecimal
    ) {
        val order = Order(
            externalReference = "1",
            instrument = "1",
            nominals = BigDecimal.ONE,
            price = price,
            amount = BigDecimal.ONE,
            currency = "EUR",
            type = OrderType.BUY,
            trader = "1",
            timestamp = OffsetDateTime.now(),
            orderReference = "",
            state = OrderState.OPEN
        )
        assertThat(order.priceKey).isEqualTo(expectedKey)
    }
}