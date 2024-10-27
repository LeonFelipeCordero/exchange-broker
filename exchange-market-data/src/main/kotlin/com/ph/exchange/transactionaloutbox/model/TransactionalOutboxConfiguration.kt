package com.ph.exchange.transactionaloutbox.model

import io.smallrye.config.ConfigMapping
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
@ConfigMapping(prefix = "application.transactional-outbox")
interface TransactionalOutboxConfiguration {
    fun enabled(): Boolean
}
