package io.github.balaelangovan.orders.app

import io.github.balaelangovan.spring.webmvc.starter.annotation.BaseWebMvcApp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Main entry point for Order Management API Service.
 *
 * This service handles REST API requests for order management.
 * Kafka event processing is handled by the separate orders-consumer module.
 *
 * This service demonstrates:
 * - Hexagonal Architecture (Ports & Adapters)
 * - Domain-Driven Design with a rich domain model
 * - Multi-module structure (domain, app, infra, consumer)
 * - Clean separation of concerns
 * - Kotlin coroutines for async operations
 */
@BaseWebMvcApp
@SpringBootApplication(
    scanBasePackages = [
        "io.github.balaelangovan.orders.domain",
        "io.github.balaelangovan.orders.app",
        "io.github.balaelangovan.orders.infra",
        "io.github.balaelangovan.spring",
    ],
)
@EnableJpaRepositories(basePackages = ["io.github.balaelangovan.orders.infra.repository"])
@EntityScan(basePackages = ["io.github.balaelangovan.orders.infra.entity"])
@EnableTransactionManagement
class OrderApplication

fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}
