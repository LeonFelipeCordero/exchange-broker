package com.ph.exchange.transactionaloutbox.repository

import com.ph.exchange.transactionaloutbox.repository.entities.TransactionalOutboxInternalEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

@ApplicationScoped
class TransactionalOutboxInternalMessageRepository : PanacheRepository<TransactionalOutboxInternalEntity> {

    fun getAvailableMessages(): List<TransactionalOutboxInternalEntity> {
        return list("sent = false", Sort.by("sequence"))
    }

    fun markAsSent(sequence: Long) {
        update(
            "sent = true, sentAt = CURRENT_TIMESTAMP, updatedAt = CURRENT_TIMESTAMP" +
                    " where sequence = ?1", sequence
        )
    }
}