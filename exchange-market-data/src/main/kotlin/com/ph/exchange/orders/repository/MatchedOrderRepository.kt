package com.ph.exchange.orders.repository

import com.ph.exchange.orders.repository.entities.MatchedOrderEntity
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MatchedOrderRepository : PanacheRepository<MatchedOrderEntity>
