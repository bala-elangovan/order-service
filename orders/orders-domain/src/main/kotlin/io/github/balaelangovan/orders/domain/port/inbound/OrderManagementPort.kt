package io.github.balaelangovan.orders.domain.port.inbound

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.exception.InvalidStateTransitionException
import io.github.balaelangovan.orders.domain.exception.ResourceNotFoundException
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus

/**
 * Inbound port defining all order management use cases.
 * This consolidated interface provides a single contract for order CRUD operations
 * and status transitions, simplifying dependency injection while maintaining
 * cohesion around the Order aggregate.
 */
interface OrderManagementPort {

    /**
     * Creates a new order for the specified customer.
     *
     * @param customerId identifier of the customer
     * @param lines list of order lines to include
     * @return the created Order
     */
    suspend fun createOrder(customerId: CustomerId, lines: List<OrderLine>): Order

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param id the order identifier
     * @return the Order
     * @throws ResourceNotFoundException if the order is not found
     */
    suspend fun getOrderById(id: OrderId): Order

    /**
     * Retrieves all orders for a specific customer.
     *
     * @param customerId the customer identifier
     * @return list of orders for the customer
     */
    suspend fun getOrdersByCustomerId(customerId: CustomerId): List<Order>

    /**
     * Retrieves all orders with pagination support.
     *
     * @param page zero-based page number
     * @param size number of orders per page
     * @return list of orders for the requested page
     */
    suspend fun getAllOrders(page: Int = 0, size: Int = 20): List<Order>

    /**
     * Updates the order status directly.
     *
     * @param id the order identifier
     * @param newStatus the target status
     * @return the updated Order
     * @throws ResourceNotFoundException if the order is not found
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    suspend fun updateStatus(id: OrderId, newStatus: OrderStatus): Order

    /**
     * Transitions the order to SHIPPED status.
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if the order is not found
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    suspend fun shipOrder(id: OrderId): Order

    /**
     * Transitions the order to DELIVERED status.
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if the order is not found
     * @throws InvalidStateTransitionException if transition is not allowed
     */
    suspend fun deliverOrder(id: OrderId): Order

    /**
     * Cancels the order.
     *
     * @param id the order identifier
     * @return the canceled Order
     * @throws ResourceNotFoundException if the order is not found
     */
    suspend fun cancelOrder(id: OrderId): Order
}
