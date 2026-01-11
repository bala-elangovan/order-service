package io.github.balaelangovan.orders.app.mapper

import io.github.balaelangovan.orders.app.dto.response.AddressResponse
import io.github.balaelangovan.orders.app.dto.response.OrderResponse
import io.github.balaelangovan.orders.domain.aggregate.LineStatus
import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot
import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot
import io.github.balaelangovan.orders.domain.mapper.DomainMapper
import io.github.balaelangovan.orders.domain.valueobject.Address
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Mapper for converting domain objects to response DTOs.
 * Implements DomainMapper for one-way conversion: Domain → DTO
 *
 * This mapper:
 * - Converts rich domain objects to flat API response DTOs
 * - Flattens value objects and aggregates for API consumption
 * - Handles ID conversion (UUID to String)
 * - Exposes only the necessary fields to external clients
 * - toDomain() throws UnsupportedOperationException (cannot convert response back to domain)
 */
@Component
class ResponseMapper : DomainMapper<Order, OrderResponse> {

    /**
     * Not supported - cannot convert OrderResponse DTO back to domain Order.
     * @throws UnsupportedOperationException always
     */
    override fun toDomain(source: OrderResponse): Order =
        throw UnsupportedOperationException("Cannot convert OrderResponse to domain Order")

    /**
     * Converts Order domain aggregate to OrderResponse DTO.
     *
     * @param domain the Order domain object
     * @return the OrderResponse DTO
     */
    override fun fromDomain(domain: Order): OrderResponse = toResponse(domain)

    /**
     * Converts Order domain aggregate to OrderResponse DTO.
     *
     * @param order the Order domain object
     * @param releases list of release snapshots for the order
     * @param shipments list of shipment snapshots for the order
     * @return the OrderResponse DTO with all order details
     */
    fun toResponse(
        order: Order,
        releases: List<ReleaseSnapshot> = emptyList(),
        shipments: List<ShipmentSnapshot> = emptyList(),
    ): OrderResponse = OrderResponse(
        id = order.id.value,
        orderKey = order.orderKey?.toString(),
        customerId = order.customerId.value,
        orderType = order.orderType.name,
        channel = order.channel.name,
        lines = order.lines.map { toLineResponse(it) },
        status = order.status.name,
        totalAmount = order.totalAmount.amount,
        currency = order.currency,
        subtotal = order.subtotal.amount,
        taxAmount = order.taxAmount.amount,
        discountAmount = order.discountAmount.amount,
        billingAddress = toAddressResponse(order.billingAddress, UUID.randomUUID()),
        notes = order.notes,
        lineCount = order.lineCount(),
        totalQuantity = order.totalQuantity(),
        releases = releases.map { toReleaseResponse(it) },
        shipments = shipments.map { toShipmentResponse(it) },
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
    )

    /**
     * Converts OrderLine domain object to OrderResponse.OrderLine DTO.
     *
     * @param line the OrderLine domain object
     * @return the OrderResponse.OrderLine DTO with line item details
     */
    fun toLineResponse(line: OrderLine): OrderResponse.OrderLine = OrderResponse.OrderLine(
        lineKey = line.id.value.toString(),
        lineNumber = line.lineNumber,
        itemId = line.itemId.toString(),
        itemName = line.itemName,
        itemDescription = line.itemDescription,
        quantity = line.quantity,
        unitPrice = line.unitPrice.amount,
        currency = line.unitPrice.currency.currencyCode,
        subtotal = line.subtotal.amount,
        taxRate = line.taxRate,
        taxAmount = line.taxAmount.amount,
        discountAmount = line.discountAmount?.amount,
        totalAmount = line.totalAmount.amount,
        fulfillmentType = line.fulfillmentType.code,
        shippingAddress = line.shippingAddress?.let { toAddressResponse(it, UUID.randomUUID()) },
        estimatedShipDate = line.estimatedShipDate,
        estimatedDeliveryDate = line.estimatedDeliveryDate,
        promisedShipDate = line.promisedShipDate,
        promisedDeliveryDate = line.promisedDeliveryDate,
        lineStatus = toLineStatusResponse(line.lineStatus),
    )

    /**
     * Converts LineStatus domain object to OrderResponse.LineStatus DTO.
     *
     * @param lineStatus the LineStatus domain object
     * @return the OrderResponse.LineStatus DTO with status details
     */
    fun toLineStatusResponse(lineStatus: LineStatus): OrderResponse.LineStatus = OrderResponse.LineStatus(
        quantity = lineStatus.quantity,
        status = lineStatus.status.name,
        statusCode = lineStatus.statusCode,
        statusDescription = lineStatus.statusDescription,
        notes = lineStatus.notes,
        updatedAt = lineStatus.updatedAt,
    )

    /**
     * Converts Address value object to AddressResponse DTO.
     *
     * @param address the Address value object
     * @param id the generated UUID for the address response
     * @return the AddressResponse DTO with address details
     */
    fun toAddressResponse(address: Address, id: UUID): AddressResponse = AddressResponse(
        id = id.toString(),
        fullName = address.fullName,
        addressLine1 = address.addressLine1,
        addressLine2 = address.addressLine2,
        city = address.city,
        stateProvince = address.stateProvince,
        postalCode = address.postalCode,
        country = address.country,
        phoneNumber = address.phoneNumber,
        email = address.email,
    )

    /**
     * Converts a list of Order domain objects to a list of OrderResponse DTOs.
     *
     * @param orders the list of Order domain objects
     * @return the list of OrderResponse DTOs
     */
    fun toResponseList(orders: List<Order>): List<OrderResponse> = orders.map { toResponse(it) }

    /**
     * Extracts the payload from ReleaseSnapshot for response.
     * The payload contains all release details in snake_case format.
     *
     * @param release the ReleaseSnapshot domain object
     * @return the payload map containing release details
     */
    fun toReleaseResponse(release: ReleaseSnapshot): Map<String, Any?> = release.payload

    /**
     * Extracts the payload from ShipmentSnapshot for response.
     * The payload contains all shipment details in snake_case format.
     *
     * @param shipment the ShipmentSnapshot domain object
     * @return the payload map containing shipment details
     */
    fun toShipmentResponse(shipment: ShipmentSnapshot): Map<String, Any?> = shipment.payload
}
