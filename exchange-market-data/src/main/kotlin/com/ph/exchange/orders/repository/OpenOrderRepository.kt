package com.ph.exchange.orders.repository

import com.ph.exchange.orders.repository.entities.OpenOrderEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import java.math.BigDecimal

@ApplicationScoped
class OpenOrderRepository : PanacheRepository<OpenOrderEntity> {

    fun persist(openOrders: List<OpenOrderEntity>) {
        persist(openOrders.stream())
    }

    fun findByInstrumentTypeAndKey(
        instrument: String,
        orderType: String,
        key: BigDecimal,
    ): List<OpenOrderEntity> {
        return list("instrument = ?1 and type = ?2 and priceKey = ?3", instrument, orderType, key)
    }

    fun delete(openOrdersReference: List<String>) {
        delete("orderReference in ?1", openOrdersReference)
    }
}
