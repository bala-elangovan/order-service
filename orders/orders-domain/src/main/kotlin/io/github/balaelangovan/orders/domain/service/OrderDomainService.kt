package io.github.balaelangovan.orders.domain.service

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent
import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot
import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot
import io.github.balaelangovan.orders.domain.exception.ConflictException
import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.exception.ResourceNotFoundException
import io.github.balaelangovan.orders.domain.mapper.OrderEventMapper
import io.github.balaelangovan.orders.domain.port.inbound.OrderEventHandlerPort
import io.github.balaelangovan.orders.domain.port.inbound.OrderManagementPort
import io.github.balaelangovan.orders.domain.port.inbound.ReleaseEventHandlerPort
import io.github.balaelangovan.orders.domain.port.inbound.ShipmentEventHandlerPort
import io.github.balaelangovan.orders.domain.port.outbound.NotificationPort
import io.github.balaelangovan.orders.domain.port.outbound.OrderRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ReleaseSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ShipmentSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus

/**
 * Domain service implementing all order-related business operations.
 * Coordinates between repositories, notifications, and domain aggregates.
 *
 * Implements inbound ports:
 * - OrderManagementPort: REST/API operations
 * - OrderEventHandlerPort: Order creation events
 * - ReleaseEventHandlerPort: Release snapshot events
 * - ShipmentEventHandlerPort: Shipment snapshot events
 */
class OrderDomainService(
    private val orderRepository: OrderRepositoryPort,
    private val notificationPort: NotificationPort,
    private val releaseSnapshotRepository: ReleaseSnapshotRepositoryPort,
    private val shipmentSnapshotRepository: ShipmentSnapshotRepositoryPort,
    private val orderEventMapper: OrderEventMapper,
) : OrderManagementPort,
    OrderEventHandlerPort,
    ReleaseEventHandlerPort,
    ShipmentEventHandlerPort {

    /**
     * Creates and persists a new order, sending creation notification.
     * Checks for duplicate external order IDs before creating.
     *
     * @param order the domain Order to save
     * @return the saved Order with generated IDs
     * @throws ConflictException if an order with the same external order ID already exists
     */
    suspend fun createOrder(order: Order): Order {
        order.externalOrderId?.let { externalId ->
            if (orderRepository.existsByExternalOrderId(externalId)) {
                throw ConflictException(
                    "Order with external ID '$externalId' already exists",
                    ErrorCode.DUPLICATE_ORDER,
                )
            }
        }

        val savedOrder = orderRepository.save(order)
        notificationPort.notifyOrderCreated(savedOrder)
        return savedOrder
    }

    override suspend fun createOrder(customerId: CustomerId, lines: List<OrderLine>): Order =
        throw UnsupportedOperationException("Use createOrder(order: Order) instead")

    /**
     * Handles OrderCreatedEvent from Kafka.
     *
     * @param event the Kafka event containing order data
     * @return the created Order
     */
    override suspend fun handleOrderCreatedEvent(event: OrderCreatedEvent): Order =
        createOrder(orderEventMapper.toDomain(event))

    /**
     * Handles release snapshot events from Kafka.
     *
     * @param releaseSnapshot the release snapshot data
     */
    override suspend fun handleReleaseEvent(releaseSnapshot: ReleaseSnapshot) {
        releaseSnapshotRepository.upsert(releaseSnapshot)
    }

    /**
     * Handles shipment snapshot events from Kafka.
     *
     * @param shipmentSnapshot the shipment snapshot data
     */
    override suspend fun handleShipmentEvent(shipmentSnapshot: ShipmentSnapshot) {
        shipmentSnapshotRepository.upsert(shipmentSnapshot)
    }

    /**
     * Retrieves all release snapshots for an order.
     *
     * @param orderId the order identifier
     * @return list of release snapshots
     */
    suspend fun getReleasesForOrder(orderId: String): List<ReleaseSnapshot> =
        releaseSnapshotRepository.findByOrderId(orderId)

    /**
     * Retrieves all shipment snapshots for an order.
     *
     * @param orderId the order identifier
     * @return list of shipment snapshots
     */
    suspend fun getShipmentsForOrder(orderId: String): List<ShipmentSnapshot> =
        shipmentSnapshotRepository.findByOrderId(orderId)

    /**
     * Retrieves an order by ID.
     *
     * @param id the order identifier
     * @return the Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun getOrderById(id: OrderId): Order = orderRepository.findById(id)
        ?: throw ResourceNotFoundException("Order", "id", id.value, ErrorCode.ORDER_NOT_FOUND)

    /**
     * Retrieves all orders for a customer.
     *
     * @param customerId the customer identifier
     * @return list of orders
     */
    override suspend fun getOrdersByCustomerId(customerId: CustomerId): List<Order> =
        orderRepository.findByCustomerId(customerId)

    /**
     * Retrieves all orders with pagination.
     *
     * @param page zero-based page number
     * @param size number of orders per page
     * @return list of orders for the page
     */
    override suspend fun getAllOrders(page: Int, size: Int): List<Order> = orderRepository.findAll(page, size)

    /**
     * Updates order notes and/or billing address.
     *
     * @param id the order identifier
     * @param notes new notes value (null to skip)
     * @param billingAddress new billing address (null to skip)
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    suspend fun updateOrder(id: OrderId, notes: String?, billingAddress: Address?): Order {
        var order = findOrderOrThrow(id)

        notes?.let { order = order.updateNotes(it) }
        billingAddress?.let { order = order.updateBillingAddress(it) }

        return orderRepository.save(order)
    }

    /**
     * Updates order status and sends appropriate notification.
     *
     * @param id the order identifier
     * @param newStatus the target status
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun updateStatus(id: OrderId, newStatus: OrderStatus): Order = when (newStatus) {
        OrderStatus.IN_RELEASE -> inReleaseOrder(id)
        OrderStatus.RELEASED -> releaseOrder(id)
        OrderStatus.IN_SHIPMENT -> inShipmentOrder(id)
        OrderStatus.SHIPPED -> shipOrder(id)
        OrderStatus.DELIVERED -> deliverOrder(id)
        OrderStatus.CANCELLED -> cancelOrder(id)
        else -> findOrderOrThrow(id)
    }

    /**
     * Transitions order to IN_RELEASE status (partial release).
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun inReleaseOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::inRelease, notificationPort::notifyOrderInRelease)

    /**
     * Transitions order to RELEASED status (full release).
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun releaseOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::release, notificationPort::notifyOrderReleased)

    /**
     * Transitions order to IN_SHIPMENT status (partial shipment).
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun inShipmentOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::inShipment, notificationPort::notifyOrderInShipment)

    /**
     * Transitions order to SHIPPED status.
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun shipOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::ship, notificationPort::notifyOrderShipped)

    /**
     * Transitions order to DELIVERED status.
     *
     * @param id the order identifier
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun deliverOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::deliver, notificationPort::notifyOrderDelivered)

    /**
     * Cancels the order.
     *
     * @param id the order identifier
     * @return the canceled Order
     * @throws ResourceNotFoundException if not found
     */
    override suspend fun cancelOrder(id: OrderId): Order =
        transitionOrderStatus(id, Order::cancel, notificationPort::notifyOrderCancelled)

    /**
     * Updates the status of a specific line item.
     *
     * @param orderId the order identifier
     * @param lineId the line item identifier
     * @param newStatus the target status
     * @param notes optional notes for the status change
     * @return the updated Order
     * @throws ResourceNotFoundException if not found
     */
    suspend fun updateLineStatus(
        orderId: OrderId,
        lineId: LineItemId,
        newStatus: LineStatusType,
        notes: String? = null,
    ): Order {
        val order = findOrderOrThrow(orderId)
        val updatedOrder = order.updateLineStatus(lineId, newStatus, notes)
        return orderRepository.save(updatedOrder)
    }

    /**
     * Soft-deletes an order by marking it as canceled.
     *
     * @param id the order identifier
     * @throws ResourceNotFoundException if not found
     */
    suspend fun deleteOrder(id: OrderId) {
        val order = findOrderOrThrow(id)
        orderRepository.save(order.cancel())
    }

    /**
     * Retrieves an order by ID or throws an exception.
     *
     * @param id the order identifier
     * @return the Order
     * @throws ResourceNotFoundException if not found
     */
    private suspend fun findOrderOrThrow(id: OrderId): Order = orderRepository.findById(id)
        ?: throw ResourceNotFoundException("Order", "id", id.value, ErrorCode.ORDER_NOT_FOUND)

    /**
     * Transitions an order to a new status, saves it, and sends notification.
     *
     * @param id the order identifier
     * @param transition function to apply status transition on the order
     * @param notify function to send notification after successful transition
     * @return the updated and saved Order
     * @throws ResourceNotFoundException if not found
     */
    private suspend fun transitionOrderStatus(
        id: OrderId,
        transition: (Order) -> Order,
        notify: suspend (Order) -> Unit,
    ): Order {
        val order = findOrderOrThrow(id)
        val updatedOrder = transition(order)
        val savedOrder = orderRepository.save(updatedOrder)
        notify(savedOrder)
        return savedOrder
    }
}
