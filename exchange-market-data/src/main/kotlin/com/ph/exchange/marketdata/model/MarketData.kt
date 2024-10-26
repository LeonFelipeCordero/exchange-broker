package com.ph.exchange.marketdata.model

import io.smallrye.config.ConfigMapping
import jakarta.enterprise.context.ApplicationScoped
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZonedDateTime
import kotlin.random.Random

data class Instrument(
    val isin: String,
    val timestamp: ZonedDateTime,
    val name: String,
    val state: InstrumentState,
    val currency: String
)

data class Quote(
    val isin: String,
    val timestamp: ZonedDateTime,
    val currency: String,
    val price: BigDecimal
) {
    fun updatePrice(): Quote {
        val up = Random.nextBoolean()
        val changeRate = Random.nextFloat() / 2
        val priceChange = (this.price.times(changeRate.toBigDecimal())).div(BigDecimal("100"))
            .setScale(6, RoundingMode.HALF_DOWN)
        val newPrice = if (up) {
            this.price - priceChange
        } else {
            this.price + priceChange
        }
        return this.copy(price = newPrice, timestamp = ZonedDateTime.now())
    }
}

enum class InstrumentState {
    ADDED,
    DELETED
}

interface CountryCurrency {
    fun countryCode(): String
    fun currency(): String
}

@ApplicationScoped
@ConfigMapping(prefix = "application.producer")
interface ProducerConfiguration {
    fun withStreaming(): Boolean
    fun numberOfInstruments(): Int
    fun instrumentChangeRate(): InstrumentStatusChangeRate
    fun instrumentsChangeFrequencyInMillis(): Long
    fun quotesGenerationFrequencyInMillis(): Long
    fun countries(): List<CountryCurrency>
    fun streamTerminationAfterMillis(): Long
}

enum class InstrumentStatusChangeRate {
    LOW,
    MID,
    HIGH
}
