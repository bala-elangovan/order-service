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
