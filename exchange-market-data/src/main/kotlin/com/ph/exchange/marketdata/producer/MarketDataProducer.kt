package com.ph.exchange.marketdata.producer

import com.ph.exchange.marketdata.model.Instrument
import com.ph.exchange.marketdata.model.InstrumentState
import com.ph.exchange.marketdata.model.InstrumentStatusChangeRate
import com.ph.exchange.marketdata.model.ProducerConfiguration
import com.ph.exchange.marketdata.model.Quote
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@Startup
@ApplicationScoped
class MarketDataProducer {
    private val mutex = Mutex()
    private var availableInstruments = mutableMapOf<String, Instrument>()
    private var quotes = mutableMapOf<String, Quote>()
    private var availableIsins = mutableListOf<String>()
    private final val job = SupervisorJob()
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)
    private var instrumentChannel: Channel<Instrument> = Channel()
    private var quoteChannel: Channel<Quote> = Channel()

    @Inject
    private lateinit var producerConfiguration: ProducerConfiguration

    @PostConstruct
    fun init() {
        loadInstruments(producerConfiguration.numberOfInstruments())

        if (producerConfiguration.withStreaming()) {
            scope.launch {
                startStreaming()
            }
        }
    }

    suspend fun connectionToInstruments(): Channel<Instrument> = instrumentChannel

    suspend fun connectToQuotes(): Channel<Quote> = quoteChannel

    fun getAvailableInstruments(): List<Instrument> {
        return availableInstruments.values.toList()
    }

    protected fun loadInstruments(expectedInstruments: Int) {
        for (i in 0 until expectedInstruments) {
            val country = producerConfiguration.countries()[Random.nextInt(0, producerConfiguration.countries().size)]
            var isin = country.countryCode()
            var name = country.countryCode()
            for (j in 0..11) {
                isin += Random.nextInt(0, 10)
                name += Random.nextInt(0, 100)
            }
            val instrument = Instrument(
                isin = isin,
                name = name,
                timestamp = ZonedDateTime.now(),
                state = InstrumentState.ADDED,
                currency = country.currency(),
            )
            val quote = Quote(
                isin = isin,
                timestamp = ZonedDateTime.now(),
                currency = country.currency(),
                price = randomPrice(),
            )
            availableIsins.add(isin)
            availableInstruments[instrument.isin] = instrument
            quotes[quote.isin] = quote
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    protected suspend fun startStreaming() = coroutineScope {
        val instrumentTick = ticker(
            delayMillis = producerConfiguration.instrumentsChangeFrequencyInMillis(),
            initialDelayMillis = 1000,
        )
        val quotesTick = ticker(
            delayMillis = producerConfiguration.quotesGenerationFrequencyInMillis(),
            initialDelayMillis = 1000,
        )

        val instrumentsJob = launch {
            for (unit in instrumentTick) {
                updateInstrumentIfPossible()
            }
        }
        val quotesJob = launch {
            for (unit in quotesTick) {
                updateQuotes()
            }
        }

        if (producerConfiguration.streamTerminationAfterMillis() > 0L) {
            delay(producerConfiguration.streamTerminationAfterMillis())
            instrumentsJob.cancel()
            quotesJob.cancel()
        }
    }

    @WithSpan("instrument_update")
    protected suspend fun updateInstrumentIfPossible() {
        val timeInMillis = measureTimeMillis {
            mutex.withLock {
                val targetInstrument = availableInstruments[availableIsins.random()]!!
                val newInstrument = changeInstrumentStatusIfPossible(targetInstrument)

                if (targetInstrument != newInstrument) {
                    availableInstruments[newInstrument.isin] = newInstrument
                    instrumentChannel.send(newInstrument)
                    Log.debug(
                        "Instrument ${newInstrument.isin} change of state ${targetInstrument.state}->${newInstrument.state}",
                    )
                }
            }
        }
        Log.debug("Took $timeInMillis millis to update an instrument")
    }

    private fun changeInstrumentStatusIfPossible(targetInstrument: Instrument): Instrument {
        return if (producerConfiguration.instrumentChangeRate() == InstrumentStatusChangeRate.HIGH) {
            changeInstrumentBaseOnRange(targetInstrument, 1, 10)
        } else if (producerConfiguration.instrumentChangeRate() == InstrumentStatusChangeRate.MID) {
            changeInstrumentBaseOnRange(targetInstrument, 6, 4)
        } else {
            changeInstrumentBaseOnRange(targetInstrument, 8, 2)
        }
    }

    private fun changeInstrumentBaseOnRange(targetInstrument: Instrument, minToDelete: Int, maxToAdd: Int): Instrument {
        val randomInt = Random.nextInt(0, 10)
        return if (randomInt > minToDelete && targetInstrument.state == InstrumentState.ADDED) {
            targetInstrument.copy(state = InstrumentState.DELETED, timestamp = ZonedDateTime.now())
        } else if (randomInt < maxToAdd && targetInstrument.state == InstrumentState.DELETED) {
            targetInstrument.copy(state = InstrumentState.ADDED, timestamp = ZonedDateTime.now())
        } else {
            targetInstrument
        }
    }

    private fun randomPrice(): BigDecimal {
        val randomDouble = Random.nextDouble(1.toDouble(), 1500.toDouble())
        return BigDecimal.valueOf(randomDouble).setScale(6, RoundingMode.HALF_DOWN)
    }

    @WithSpan("quotes_update")
    protected suspend fun updateQuotes() {
        val timeInMillis = measureTimeMillis {
            mutex.withLock {
                for (quote in quotes.values) {
                    val instrument = availableInstruments[quote.isin]!!
                    if (instrument.state != InstrumentState.DELETED) {
                        val newQuote = quote.updatePrice()
                        quotes[newQuote.isin] = newQuote
                        quoteChannel.send(newQuote)
                        Log.debug(
                            "New quote created, Isin: ${newQuote.isin}, Quote: ${newQuote.price} old Quote: ${quote.price}",
                        )
                    }
                }
            }
        }
        Log.debug("Took $timeInMillis millis to create new quotes for ${quotes.size} available instruments")
    }

    fun reset() {
        scope.cancel()
        availableInstruments = mutableMapOf()
        quotes = mutableMapOf()
        availableIsins = mutableListOf()
        init()
    }
}
