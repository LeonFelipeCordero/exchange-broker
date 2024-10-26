package com.ph.exchange.transactionaloutbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.ph.exchange.transactionaloutbox.repository.TransactionalOutboxInternalMessageRepository
import com.ph.exchange.transactionaloutbox.service.TransactionalOutboxService
import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import io.vertx.mutiny.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@ApplicationScoped
class InternalTransactionalOutbox {
    @Inject
    private lateinit var transactionalOutboxService: TransactionalOutboxService

    @Inject
    private lateinit var eventBus: EventBus

    @Transactional
    @Scheduled(every = "PT0.1S", identity = "transactional-outbox")
    fun task() {
        transactionalOutboxService.getAvailableMessages()
            .forEach {
                Log.info("Sending message to ${it.event}, ${it.sequence}")
                val status = eventBus.requestAndAwait<String>(it.event, it.message).body()
                Log.info("Got answer for ${it.event}, ${it.sequence}")
                if (status == InternalEventingMessageStatus.OK.name) {
                    transactionalOutboxService.markAsSent(it.sequence!!)
                }
            }
    }
}
