package io.github.balaelangovan.orders.infra.repository

import io.github.balaelangovan.orders.infra.entity.OrderEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Spring Data JPA repository for OrderEntity.
 * Uses @EntityGraph for efficient fetching to avoid N+1 problems.
 *
 * Available entity graphs (defined on OrderEntity):
 * - order-with-lines: Fetches order with lines and their statuses
 * - order-with-snapshots: Fetches order with shipment and release snapshots
 * - order-full: Fetches order with all associations
 */
@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {

    /**
     * Find order by business ID with lines and line statuses eagerly loaded.
     * Uses entity graph to avoid N+1 queries.
     */
    @EntityGraph(value = "order-with-lines", type = EntityGraph.EntityGraphType.LOAD)
    fun findByOrderId(orderId: String): Optional<OrderEntity>

    /**
     * Find all orders for a customer with lines eagerly loaded.
     */
    @EntityGraph(value = "order-with-lines", type = EntityGraph.EntityGraphType.LOAD)
    fun findByCustomerId(customerId: String): List<OrderEntity>

    /**
     * Find all orders with pagination and lines eagerly loaded.
     */
    @EntityGraph(value = "order-with-lines", type = EntityGraph.EntityGraphType.LOAD)
    fun findAllBy(pageable: Pageable): List<OrderEntity>

    /**
     * Check if the order exists by business ID (no eager loading needed).
     */
    fun existsByOrderId(orderId: String): Boolean

    /**
     * Check if an order exists by external order ID (for duplicate detection).
     */
    fun existsByExternalOrderId(externalOrderId: java.util.UUID): Boolean

    /**
     * Delete order by business ID.
     */
    fun deleteByOrderId(orderId: String)
}
