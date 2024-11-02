package com.ph.exchange.transactionaloutbox

import com.ph.exchange.orders.model.events.internal.InternalEventingMessageStatus
import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxConfiguration
import com.ph.exchange.transactionaloutbox.service.TransactionalOutboxService
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.SkipPredicate
import io.quarkus.scheduler.ScheduledExecution
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional

@ApplicationScoped
class InternalTransactionalOutbox {

    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    private lateinit var eventBus: EventBus

    @WithSpan("tx-outbox")
    @Scheduled(
        every = "PT1S",
        identity = "transactional-outbox",
        skipExecutionIf = TransactionalOutboxSkipPredicate::class,
    )
    @Transactional
    fun task() {
        transactionalOutboxService.getAvailableMessages()
            .forEach {
                Log.debug("Sending message to ${it.event}, ${it.sequence}")
                val status = eventBus.requestAndAwait<String>(it.event, it.message).body()
                if (status == InternalEventingMessageStatus.OK.name) {
                    Log.debug("Got answer for ${it.event}, ${it.sequence} -> $status")
                    transactionalOutboxService.markAsSent(it.sequence!!)
                }
            }
    }
}

@Singleton
class TransactionalOutboxSkipPredicate : SkipPredicate {

    @Inject
    private lateinit var transactionalOutboxConfiguration: TransactionalOutboxConfiguration

    override fun test(execution: ScheduledExecution): Boolean {
        return !transactionalOutboxConfiguration.enabled()
    }
}
