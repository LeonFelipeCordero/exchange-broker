package com.ph.exchange.marketdata

import io.quarkus.test.junit.QuarkusTestProfile

class MarketDataProducerWithoutStreamingProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): MutableMap<String, String> {
        return mutableMapOf(
            "application.producer.with-streaming" to "false",
            "application.producer.number-of-instruments" to "5",
        )
    }
}

class MarketDataProducerWithStreamingProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): MutableMap<String, String> {
        return mutableMapOf(
            "application.producer.with-streaming" to "true",
            "application.producer.number-of-instruments" to "3",
            "application.producer.instrument-change-rate" to "HIGH",
            "application.producer.instruments-change-frequency-in-millis" to "1000",
            "application.producer.quotes-generation-frequency-in-millis" to "250",
        )
    }
}
