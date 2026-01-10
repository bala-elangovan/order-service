package io.github.balaelangovan.orders.domain.port.outbound

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import java.util.UUID

/**
 * Output port (repository) for Order aggregate persistence.
 * This is a pure interface with NO framework dependencies.
 *
 * Implementation will be provided by the persistence adapter in the infrastructure layer.
 */
interface OrderRepositoryPort {
    /**
     * Saves an order (create or update).
     *
     * @param order the order to save
     * @return the saved order
     */
    suspend fun save(order: Order): Order

    /**
     * Finds an order by its ID.
     *
     * @param id the order identifier
     * @return the order or null if not found
     */
    suspend fun findById(id: OrderId): Order?

    /**
     * Finds all orders for a specific customer.
     *
     * @param customerId the customer identifier
     * @return list of orders for the customer
     */
    suspend fun findByCustomerId(customerId: CustomerId): List<Order>

    /**
     * Finds all orders with pagination.
     *
     * @param page zero-based page number
     * @param size number of orders per page
     * @return list of orders for the requested page
     */
    suspend fun findAll(page: Int, size: Int): List<Order>

    /**
     * Deletes an order by ID.
     *
     * @param id the order identifier to delete
     */
    suspend fun deleteById(id: OrderId)

    /**
     * Checks if an order exists.
     *
     * @param id the order identifier to check
     * @return true if the order exists
     */
    suspend fun existsById(id: OrderId): Boolean

    /**
     * Checks if an order exists by external order ID (for duplicate detection).
     *
     * @param externalOrderId the external order ID from upstream system
     * @return true if an order with this external ID already exists
     */
    suspend fun existsByExternalOrderId(externalOrderId: UUID): Boolean

    /**
     * Counts total orders.
     *
     * @return total number of orders
     */
    suspend fun count(): Long
}
