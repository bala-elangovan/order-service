package io.github.balaelangovan.orders.infra.mapper

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.Channel
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderType
import io.github.balaelangovan.orders.infra.generator.OrderIdGenerator
import org.springframework.stereotype.Component
import io.github.balaelangovan.orders.domain.mapper.OrderEventMapper as DomainOrderEventMapper

/**
 * Mapper for converting Kafka order events to domain objects.
 * Translates external events into domain model and infrastructure models.
 */
@Component
class OrderEventMapper(private val orderIdGenerator: OrderIdGenerator) : DomainOrderEventMapper {

    /**
     * Converts OrderCreatedEvent to Order domain aggregate.
     *
     * @param event the Kafka event containing order creation data
     * @return the Order domain aggregate
     */
    override fun toDomain(event: OrderCreatedEvent): Order = mapOrderFromEvent(event)

    private fun mapOrderFromEvent(event: OrderCreatedEvent): Order {
        val channel = Channel.fromNameOrDefault(event.channel)

        return Order.create(
            orderId = OrderId.of(orderIdGenerator.generateOrderId(channel)),
            customerId = CustomerId.of(event.customerId),
            orderType = OrderType.fromNameOrDefault(event.orderType),
            channel = channel,
            lines = event.orderLines.map { mapOrderLine(it) },
            billingAddress = mapAddress(event.billingAddress),
            notes = event.notes,
            externalOrderId = event.externalOrderId,
        )
    }

    private fun mapOrderLine(lineEvent: OrderCreatedEvent.OrderLine): OrderLine = OrderLine.create(
        lineNumber = lineEvent.lineNumber,
        itemId = ItemId.of(lineEvent.itemId),
        itemName = lineEvent.itemName,
        itemDescription = lineEvent.itemDescription,
        quantity = lineEvent.quantity,
        unitPrice = Money.of(lineEvent.unitPrice, lineEvent.currency),
        taxRate = lineEvent.taxRate,
        discountAmount = lineEvent.discountAmount?.let { Money.of(it, lineEvent.currency) },
        fulfillmentType = FulfillmentType.fromCodeOrNameOrDefault(lineEvent.fulfillmentType),
        shippingAddress = mapAddress(lineEvent.shippingAddress),
        estimatedShipDate = lineEvent.estimatedShipDate,
        estimatedDeliveryDate = lineEvent.estimatedDeliveryDate,
    )

    private fun mapAddress(addressEvent: OrderCreatedEvent.Address): Address = Address.of(
        fullName = addressEvent.fullName,
        addressLine1 = addressEvent.addressLine1,
        addressLine2 = addressEvent.addressLine2,
        city = addressEvent.city,
        stateProvince = addressEvent.stateProvince,
        postalCode = addressEvent.postalCode,
        country = addressEvent.country,
        phoneNumber = addressEvent.phoneNumber,
        email = addressEvent.email,
    )
}
