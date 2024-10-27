package com.ph.exchange.transactionaloutbox.service

import com.ph.exchange.transactionaloutbox.model.TransactionalOutboxInternalMessage
import com.ph.exchange.transactionaloutbox.repository.TransactionalOutboxInternalMessageRepository
import com.ph.exchange.transactionaloutbox.repository.entities.TransactionalOutboxInternalEntity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class TransactionalOutboxService {

    @Inject
    private lateinit var transactionalOutboxInternalMessageRepository: TransactionalOutboxInternalMessageRepository

    fun persist(transactionalOutboxInternalMessage: TransactionalOutboxInternalMessage) {
        val entity = TransactionalOutboxInternalEntity.fromDomain(transactionalOutboxInternalMessage)
        transactionalOutboxInternalMessageRepository.persist(entity)
    }

    fun getAvailableMessages(): List<TransactionalOutboxInternalMessage> {
        return transactionalOutboxInternalMessageRepository
            .getAvailableMessages()
            .map { it.toDomain() }
    }

    fun markAsSent(sequence: Long) {
        transactionalOutboxInternalMessageRepository.markAsSent(sequence)
    }
}
