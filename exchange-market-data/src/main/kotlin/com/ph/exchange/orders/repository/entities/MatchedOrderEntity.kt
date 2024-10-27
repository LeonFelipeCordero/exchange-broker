package com.ph.exchange.orders.repository.entities

import com.ph.exchange.orders.model.MatchedOrder
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity(name = "matched_orders")
class MatchedOrderEntity(
    @EmbeddedId
    val matchedOrderId: MatchedOrderId,
    val matchedNominals: BigDecimal,
    @CreationTimestamp
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @UpdateTimestamp
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) : PanacheEntityBase {
    companion object {
        fun fromDomain(matchedOrder: MatchedOrder): MatchedOrderEntity {
            return MatchedOrderEntity(
                matchedOrderId = MatchedOrderId(
                    buyReference = matchedOrder.buyReference,
                    sellReference = matchedOrder.sellReference,
                ),
                matchedNominals = matchedOrder.matchNominals,
            )
        }
    }

    fun toDomain(): MatchedOrder {
        return MatchedOrder(
            buyReference = matchedOrderId.buyReference,
            sellReference = matchedOrderId.sellReference,
            matchNominals = matchedNominals,
        )
    }
}

@Embeddable
class MatchedOrderId(
    val buyReference: String,
    val sellReference: String,
)
