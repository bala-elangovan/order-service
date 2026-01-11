package io.github.balaelangovan.orders.app.mapper

import io.github.balaelangovan.orders.app.dto.request.AddressRequest
import io.github.balaelangovan.orders.app.dto.request.CreateOrderRequest
import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.mapper.DomainMapper
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

/**
 * Mapper for converting REST request DTOs to domain objects.
 * Implements one-way conversion (DTO → Domain) using domain factory methods
 * which enforce validation rules. The fromDomain() method is not supported
 * as request DTOs are input-only.
 */
@Component
class RequestMapper(private val orderIdGenerator: OrderIdGenerator) : DomainMapper<Order, CreateOrderRequest> {

    /**
     * Converts a CreateOrderRequest DTO to an Order domain aggregate.
     *
     * @param source the request DTO containing order data
     * @return the Order domain object with generated ID
     */
    override fun toDomain(source: CreateOrderRequest): Order {
        val channel = Channel.fromNameOrDefault(source.channel)
        val orderType = OrderType.fromNameOrDefault(source.orderType)

        return Order.create(
            orderId = OrderId.of(orderIdGenerator.generateOrderId(channel)),
            customerId = CustomerId.of(source.customerId),
            orderType = orderType,
            channel = channel,
            lines = source.lines.mapIndexed { index, lineRequest -> toOrderLine(index + 1, lineRequest) },
            billingAddress = toAddress(source.billingAddress),
            notes = source.notes,
        )
    }

    /**
     * Converts a CreateOrderRequest.OrderLine DTO to an OrderLine domain object.
     *
     * @param lineNumber the sequential line number within the order
     * @param request the line request DTO
     * @return the OrderLine domain object
     */
    fun toOrderLine(lineNumber: Int, request: CreateOrderRequest.OrderLine): OrderLine = OrderLine.create(
        lineNumber = lineNumber,
        itemId = ItemId.of(request.itemId),
        itemName = request.itemName,
        itemDescription = request.itemDescription,
        quantity = request.quantity,
        unitPrice = Money.of(request.unitPrice, request.currency),
        taxRate = request.taxRate,
        discountAmount = request.discountAmount?.let { Money.of(it, request.currency) },
        fulfillmentType = FulfillmentType.fromCodeOrNameOrDefault(request.fulfillmentType),
        shippingAddress = toAddress(request.shippingAddress),
        estimatedShipDate = request.estimatedShipDate,
        estimatedDeliveryDate = request.estimatedDeliveryDate,
        promisedShipDate = request.promisedShipDate,
        promisedDeliveryDate = request.promisedDeliveryDate,
    )

    /**
     * Converts an AddressRequest DTO to an Address value object.
     *
     * @param request the address request DTO
     * @return the Address value object
     */
    fun toAddress(request: AddressRequest): Address = Address.of(
        fullName = request.fullName,
        addressLine1 = request.addressLine1,
        addressLine2 = request.addressLine2,
        city = request.city,
        stateProvince = request.stateProvince,
        postalCode = request.postalCode,
        country = request.country,
        phoneNumber = request.phoneNumber,
        email = request.email,
    )

    /**
     * Unsupported operation: cannot convert domain Order to CreateOrderRequest.
     *
     * @throws UnsupportedOperationException always
     */
    override fun fromDomain(domain: Order): CreateOrderRequest =
        throw UnsupportedOperationException("Cannot convert domain Order to CreateOrderRequest")
}
