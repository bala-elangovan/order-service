package io.github.balaelangovan.orders.domain.mapper

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent

/**
 * Mapper interface for converting Kafka order creation events to domain objects.
 * Implementation is provided by the infrastructure layer.
 */
fun interface OrderEventMapper {

    /**
     * Converts an OrderCreatedEvent to an Order domain aggregate.
     *
     * @param event the Kafka event containing order creation data
     * @return the Order domain aggregate
     */
    fun toDomain(event: OrderCreatedEvent): Order
}
