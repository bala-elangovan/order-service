package io.github.balaelangovan.orders.domain.port.outbound

import io.github.balaelangovan.orders.domain.aggregate.Order

/**
 * Output port for sending notifications.
 * Can be implemented with email, SMS, or event publishing.
 */
interface NotificationPort {
    /**
     * Sends a notification when an order is created.
     *
     * @param order the created order
     */
    suspend fun notifyOrderCreated(order: Order)

    /**
     * Sends notification when an order enters partial release state.
     *
     * @param order the order in IN_RELEASE status
     */
    suspend fun notifyOrderInRelease(order: Order)

    /**
     * Sends notification when an order is fully released.
     *
     * @param order the released order
     */
    suspend fun notifyOrderReleased(order: Order)

    /**
     * Sends notification when an order enters partial shipment state.
     *
     * @param order the order in IN_SHIPMENT status
     */
    suspend fun notifyOrderInShipment(order: Order)

    /**
     * Sends notification when an order is shipped.
     *
     * @param order the shipped order
     */
    suspend fun notifyOrderShipped(order: Order)

    /**
     * Sends a notification when an order is delivered.
     *
     * @param order the delivered order
     */
    suspend fun notifyOrderDelivered(order: Order)

    /**
     * Sends notification when an order is canceled.
     *
     * @param order the canceled order
     */
    suspend fun notifyOrderCancelled(order: Order)
}
