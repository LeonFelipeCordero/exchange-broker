package com.ph.exchange.transactionaloutbox

import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxConfiguration
import com.ph.exchange.transactionaloutbox.service.TransactionalOutboxService
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import io.quarkus.scheduler.Scheduled.SkipPredicate
import io.quarkus.scheduler.ScheduledExecution
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Message
import org.eclipse.microprofile.reactive.messaging.Metadata
import java.time.ZonedDateTime

@ApplicationScoped
class InternalTransactionalOutbox {
    private var outgoingRabbitMQMetadata: OutgoingRabbitMQMetadata =
        OutgoingRabbitMQMetadata.builder()
            .withRoutingKey("order.filled")
            .withTimestamp(ZonedDateTime.now())
            .build()

    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    @Channel("exchange_order_updates")
    private lateinit var emitter: Emitter<String>

    @Scheduled(
        every = "PT1S",
        delay = 1,
        identity = "transactional-outbox",
        skipExecutionIf = TransactionalOutboxSkipPredicate::class,
    )
    @Transactional
    fun task() {
        transactionalOutboxService.getAvailableMessages()
            .forEach {
                Log.debug("Sending message to ${it.event}, ${it.sequence}")
                emitter.send(
                    Message.of(
                        it.message,
                        Metadata.of(outgoingRabbitMQMetadata),
                    ),
                )
                transactionalOutboxService.markAsSent(it.sequence!!)
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
