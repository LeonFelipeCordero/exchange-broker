package com.ph.exchange.transactionaloutbox.repository

import com.ph.exchange.transactionaloutbox.repository.entities.TransactionalOutboxEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TransactionalOutboxMessageRepository : PanacheRepository<TransactionalOutboxEntity> {

    fun getAvailableMessages(): List<TransactionalOutboxEntity> {
        return list("sent = false", Sort.by("sequence"))
    }

    fun markAsSent(sequence: Long) {
        update(
            "sent = true, sentAt = CURRENT_TIMESTAMP, updatedAt = CURRENT_TIMESTAMP" +
                " where sequence = ?1",
            sequence,
        )
    }
}
