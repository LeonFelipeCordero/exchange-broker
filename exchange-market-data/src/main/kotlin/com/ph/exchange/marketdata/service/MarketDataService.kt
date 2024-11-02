package com.ph.exchange.marketdata.service

import com.ph.exchange.marketdata.model.Instrument
import com.ph.exchange.marketdata.producer.MarketDataProducer
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.quarkus.websockets.next.OpenConnections
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Startup
@ApplicationScoped
class MarketDataService {
    private final val job = SupervisorJob()
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)
    private val instrumentsConnection = mutableMapOf<String, String>()
    private val quotesConnection = mutableMapOf<String, String>()

    @Inject
    private lateinit var openConnections: OpenConnections

    @Inject
    private lateinit var marketDataProducer: MarketDataProducer

    @PostConstruct
    fun init() {
        scope.launch {
            val instrumentChannel = marketDataProducer.connectionToInstruments()
            for (instrument in instrumentChannel) {
                openConnections
                    .filter { instrumentsConnection.values.contains(it.id()) }
                    .forEach {
                        Log.debug("sending message ${instrument.isin} -> ${instrument.state} -> ${it.id()}")
                        it.sendTextAndAwait(listOf(instrument))
                    }
            }
        }
        scope.launch {
            val quoteChannel = marketDataProducer.connectToQuotes()
            for (quote in quoteChannel) {
                openConnections
                    .filter { quotesConnection.values.contains(it.id()) }
                    .forEach {
                        Log.debug("sending message ${quote.isin} ${quote.price} -> ${it.id()}")
                        it.sendTextAndAwait(quote)
                    }
            }
        }
    }

    fun subscribeToInstruments(wsConnId: String) {
        instrumentsConnection[wsConnId] = wsConnId
    }

    fun subscribeToQuotes(wsConnId: String) {
        quotesConnection[wsConnId] = wsConnId
    }

    fun removeConnectionFromInstruments(wsConnId: String) {
        instrumentsConnection.remove(wsConnId)
    }

    fun removeConnectionFromQuotes(wsConnId: String) {
        quotesConnection.remove(wsConnId)
    }

    fun getAvailableInstruments(): List<Instrument> {
        return marketDataProducer.getAvailableInstruments()
    }
}
