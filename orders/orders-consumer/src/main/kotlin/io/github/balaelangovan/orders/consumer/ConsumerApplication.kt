package io.github.balaelangovan.orders.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Main entry point for Order Event Consumer Service.
 *
 * This service is responsible for:
 * - Consuming order creation events from checkout service
 * - Consuming release events from release service
 * - Consuming shipment events from shipment service
 *
 * Separated from the API service for:
 * - Independent scaling (consumers vs API have different load patterns)
 * - Fault isolation (consumer issues don't affect API availability)
 * - Deployment flexibility (can deploy/restart independently)
 */
@SpringBootApplication(
    scanBasePackages = [
        "io.github.balaelangovan.orders.domain",
        "io.github.balaelangovan.orders.consumer",
        "io.github.balaelangovan.orders.infra",
    ],
)
@EnableJpaRepositories(basePackages = ["io.github.balaelangovan.orders.infra.repository"])
@EntityScan(basePackages = ["io.github.balaelangovan.orders.infra.entity"])
@EnableKafka
@EnableTransactionManagement
class ConsumerApplication

fun main(args: Array<String>) {
    runApplication<ConsumerApplication>(*args)
}
