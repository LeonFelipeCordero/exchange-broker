package com.ph.exchange.transactionaloutbox.repository.entities

import com.ph.exchange.transactionaloutbox.TransactionalOutboxInternalMessage
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity(name = "internal_transactional_outbox")
class TransactionalOutboxInternalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val sequence: Long? = null,
    val event: String,
    val message: String,
    val sent: Boolean,
    val sentAt: OffsetDateTime?,
    @CreationTimestamp
    val createdAt: OffsetDateTime,
    @UpdateTimestamp
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun fromDomain(transactionalOutboxInternalMessage: TransactionalOutboxInternalMessage): TransactionalOutboxInternalEntity {
            return TransactionalOutboxInternalEntity(
                sequence = transactionalOutboxInternalMessage.sequence,
                event = transactionalOutboxInternalMessage.event,
                message = transactionalOutboxInternalMessage.message,
                sent = transactionalOutboxInternalMessage.sent,
                sentAt = transactionalOutboxInternalMessage.sentAt,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now()
            )
        }
    }

    fun toDomain(): TransactionalOutboxInternalMessage {
        return TransactionalOutboxInternalMessage(
            sequence = sequence,
            event = event,
            message = message,
            sent = sent,
            sentAt = sentAt,
        )
    }
}