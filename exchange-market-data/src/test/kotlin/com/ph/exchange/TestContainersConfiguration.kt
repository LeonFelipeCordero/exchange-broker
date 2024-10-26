package com.ph.exchange

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class TimescaleDBTestResource : QuarkusTestResourceLifecycleManager {

    companion object {
        val timescaledbContainer: PostgreSQLContainer<*> = PostgreSQLContainer(
            DockerImageName.parse("timescaledev/timescaledb-ha:pg16")
                .asCompatibleSubstituteFor("postgres")
        ).withDatabaseName("exchange")
            .withUsername("exchange")
            .withPassword("exchange")
    }

    override fun start(): MutableMap<String, String> {
        timescaledbContainer.start()
        return mutableMapOf(
            "quarkus.datasource.jdbc.url" to timescaledbContainer.jdbcUrl,
            "quarkus.datasource.username" to timescaledbContainer.username,
            "quarkus.datasource.password" to timescaledbContainer.password
        )
    }

    override fun stop() {
        timescaledbContainer.stop()
    }
}