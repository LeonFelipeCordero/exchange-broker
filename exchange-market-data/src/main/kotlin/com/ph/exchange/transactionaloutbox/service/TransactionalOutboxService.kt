package com.ph.exchange.transactionaloutbox.service

import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxInternalMessage
import com.ph.exchange.transactionaloutbox.repository.TransactionalOutboxMessageRepository
import com.ph.exchange.transactionaloutbox.repository.entities.TransactionalOutboxEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class TransactionalOutboxService {

    @Inject
    private lateinit var transactionalOutboxMessageRepository: TransactionalOutboxMessageRepository

    fun persist(transactionalOutboxInternalMessage: TransactionalOutboxInternalMessage) {
        val entity = TransactionalOutboxEntity.fromDomain(transactionalOutboxInternalMessage)
        transactionalOutboxMessageRepository.persist(entity)
    }

    fun getAvailableMessages(): List<TransactionalOutboxInternalMessage> {
        return transactionalOutboxMessageRepository
            .getAvailableMessages()
            .map { it.toDomain() }
    }

    fun markAsSent(sequence: Long) {
        transactionalOutboxMessageRepository.markAsSent(sequence)
    }
}
