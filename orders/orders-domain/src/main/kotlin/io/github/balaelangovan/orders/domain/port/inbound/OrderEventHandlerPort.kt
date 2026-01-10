package io.github.balaelangovan.orders.domain.port.inbound

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent

/**
 * Inbound port for handling order events from external systems.
 * This port is used by event consumer adapters (e.g., Kafka consumers)
 * to process incoming order events.
 */
fun interface OrderEventHandlerPort {
    /**
     * Handles an order-created event from an external system.
     * Processes the event, creates the order, and persists it with any associated snapshot data.
     *
     * @param event The order created event from the external system
     * @return The created and persisted Order domain aggregate
     */
    suspend fun handleOrderCreatedEvent(event: OrderCreatedEvent): Order
}
