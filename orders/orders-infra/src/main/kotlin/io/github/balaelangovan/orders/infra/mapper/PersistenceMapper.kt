package io.github.balaelangovan.orders.infra.mapper

import io.github.balaelangovan.orders.domain.aggregate.LineStatus
import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.mapper.DomainMapper
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.infra.entity.AddressEntity
import io.github.balaelangovan.orders.infra.entity.OrderEntity
import io.github.balaelangovan.orders.infra.entity.OrderLineEntity
import io.github.balaelangovan.orders.infra.entity.OrderLineStatusEntity
import io.github.balaelangovan.orders.infra.entity.ReleaseSnapshotEntity
import io.github.balaelangovan.orders.infra.entity.ShipmentSnapshotEntity
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Anti-Corruption Layer for bidirectional conversion between domain objects and JPA entities.
 * ID strategy: `id` (Long) for PK, `*_key` (UUID) for FK references, `order_id` (String) for business ID.
 */
@Component
class PersistenceMapper {

    inner class AddressMapper : DomainMapper<Address, AddressEntity> {
        override fun toDomain(source: AddressEntity): Address = entityToAddress(source)
        override fun fromDomain(domain: Address): AddressEntity = addressToEntity(domain)
    }

    val addressMapper = AddressMapper()

    fun addressToEntity(address: Address): AddressEntity = AddressEntity(
        id = 0L,
        addressKey = UUID.randomUUID(),
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

    fun entityToAddress(entity: AddressEntity): Address = Address.Companion.of(
        fullName = entity.fullName,
        addressLine1 = entity.addressLine1,
        addressLine2 = entity.addressLine2,
        city = entity.city,
        stateProvince = entity.stateProvince,
        postalCode = entity.postalCode,
        country = entity.country,
        phoneNumber = entity.phoneNumber,
        email = entity.email,
    )

    /**
     * Converts domain Order to OrderEntity.
     * Billing address is at order level, shipping addresses are at line level.
     */
    fun orderToEntity(
        order: Order,
        billingAddressEntity: AddressEntity,
        lineShippingAddresses: Map<LineItemId, AddressEntity>,
    ): OrderEntity {
        val orderEntity = OrderEntity(
            id = 0L,
            orderKey = UUID.randomUUID(),
            orderId = order.id.value,
            externalOrderId = order.externalOrderId,
            customerId = order.customerId.value,
            orderType = order.orderType,
            channel = order.channel,
            status = order.status,
            billingAddress = billingAddressEntity,
            shipmentSnapshots = mutableListOf(),
            releaseSnapshots = mutableListOf(),
            notes = order.notes,
            subtotal = order.subtotal.amount,
            taxAmount = order.taxAmount.amount,
            discountAmount = order.discountAmount.amount,
            totalAmount = order.totalAmount.amount,
            currency = order.currency,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt,
            lines = mutableListOf(),
        )

        order.lines.forEach { domainLine ->
            val shippingAddressEntity = lineShippingAddresses[domainLine.id]
            val lineEntity = orderLineToEntity(domainLine, shippingAddressEntity)
            lineEntity.order = orderEntity
            orderEntity.lines.add(lineEntity)

            val statusEntity = lineStatusToEntity(domainLine.lineStatus)
            statusEntity.line = lineEntity
            lineEntity.lineStatus = statusEntity
        }

        return orderEntity
    }

    fun entityToOrder(entity: OrderEntity, billingAddress: Address): Order {
        val lines = entity.lines.map { lineEntity ->
            entityToOrderLine(lineEntity)
        }

        return Order(
            id = OrderId.Companion.of(entity.orderId),
            orderKey = entity.orderKey,
            externalOrderId = entity.externalOrderId,
            customerId = CustomerId.Companion.of(entity.customerId),
            orderType = entity.orderType,
            channel = entity.channel,
            lines = lines,
            status = entity.status,
            billingAddress = billingAddress,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }

    private fun orderLineToEntity(line: OrderLine, shippingAddressEntity: AddressEntity?): OrderLineEntity =
        OrderLineEntity(
            id = 0L,
            lineKey = line.id.value,
            order = null,
            lineNumber = line.lineNumber,
            itemId = line.itemId.value,
            itemName = line.itemName,
            itemDescription = line.itemDescription,
            quantity = line.quantity,
            unitPrice = line.unitPrice.amount,
            taxRate = line.taxRate,
            discountAmount = line.discountAmount?.amount,
            currency = line.unitPrice.currency.currencyCode,
            fulfillmentType = line.fulfillmentType,
            shippingAddress = shippingAddressEntity,
            estimatedShipDate = line.estimatedShipDate,
            estimatedDeliveryDate = line.estimatedDeliveryDate,
            promisedShipDate = line.promisedShipDate,
            promisedDeliveryDate = line.promisedDeliveryDate,
            lineStatus = null,
        )

    private fun entityToOrderLine(entity: OrderLineEntity): OrderLine {
        val unitPrice = Money.Companion.of(entity.unitPrice, entity.currency)
        val discountAmount = entity.discountAmount?.let { Money.Companion.of(it, entity.currency) }

        val lineStatus = entity.lineStatus?.let { entityToLineStatus(it) }
            ?: LineStatus.create(entity.quantity)

        val shippingAddress = entity.shippingAddress?.let { entityToAddress(it) }

        return OrderLine(
            id = LineItemId.Companion.of(entity.lineKey),
            lineNumber = entity.lineNumber,
            itemId = ItemId.Companion.of(entity.itemId),
            itemName = entity.itemName,
            itemDescription = entity.itemDescription,
            quantity = entity.quantity,
            unitPrice = unitPrice,
            taxRate = entity.taxRate,
            discountAmount = discountAmount,
            fulfillmentType = entity.fulfillmentType,
            shippingAddress = shippingAddress,
            lineStatus = lineStatus,
            estimatedShipDate = entity.estimatedShipDate,
            estimatedDeliveryDate = entity.estimatedDeliveryDate,
            promisedShipDate = entity.promisedShipDate,
            promisedDeliveryDate = entity.promisedDeliveryDate,
        )
    }

    private fun lineStatusToEntity(status: LineStatus): OrderLineStatusEntity = OrderLineStatusEntity(
        id = 0L,
        lineStatusKey = UUID.randomUUID(),
        line = null,
        quantity = status.quantity,
        status = status.status,
        statusCode = status.statusCode,
        statusDescription = status.statusDescription,
        notes = status.notes,
        updatedAt = status.updatedAt,
    )

    private fun entityToLineStatus(entity: OrderLineStatusEntity): LineStatus = LineStatus(
        quantity = entity.quantity,
        status = entity.status,
        statusCode = entity.statusCode,
        statusDescription = entity.statusDescription,
        notes = entity.notes,
        updatedAt = entity.updatedAt,
    )

    /**
     * Converts domain ShipmentSnapshot to entity using hybrid approach.
     * Key fields are stored as columns, full payload stored as JSONB.
     */
    fun shipmentSnapshotToEntity(
        snapshot: io.github.balaelangovan.orders.domain.event.ShipmentSnapshot,
    ): ShipmentSnapshotEntity = ShipmentSnapshotEntity(
        id = 0L,
        shipmentKey = UUID.randomUUID(),
        shipmentId = snapshot.shipmentId,
        shipmentStatus = snapshot.shipmentStatus,
        trackingNumber = snapshot.trackingNumber,
        eventTimestamp = snapshot.eventTimestamp,
        payload = snapshot.payload,
    )

    /**
     * Converts entity to domain ShipmentSnapshot.
     * Reconstructs a domain object from indexed fields and payload.
     */
    fun entityToShipmentSnapshot(
        entity: ShipmentSnapshotEntity,
    ): io.github.balaelangovan.orders.domain.event.ShipmentSnapshot =
        io.github.balaelangovan.orders.domain.event.ShipmentSnapshot(
            shipmentId = entity.shipmentId,
            orderId = entity.order?.orderId ?: "",
            shipmentStatus = entity.shipmentStatus,
            trackingNumber = entity.trackingNumber,
            eventTimestamp = entity.eventTimestamp,
            payload = entity.payload,
        )

    /**
     * Converts domain ReleaseSnapshot to entity using hybrid approach.
     * Key fields are stored as columns, full payload is stored as JSONB.
     */
    fun releaseSnapshotToEntity(
        snapshot: io.github.balaelangovan.orders.domain.event.ReleaseSnapshot,
    ): ReleaseSnapshotEntity = ReleaseSnapshotEntity(
        id = 0L,
        releaseKey = UUID.randomUUID(),
        releaseId = snapshot.releaseId,
        releaseStatus = snapshot.releaseStatus,
        eventTimestamp = snapshot.eventTimestamp,
        payload = snapshot.payload,
    )

    /**
     * Converts entity to domain ReleaseSnapshot.
     * Reconstructs domain object from indexed fields and payload.
     */
    fun entityToReleaseSnapshot(
        entity: ReleaseSnapshotEntity,
    ): io.github.balaelangovan.orders.domain.event.ReleaseSnapshot =
        io.github.balaelangovan.orders.domain.event.ReleaseSnapshot(
            releaseId = entity.releaseId,
            orderId = entity.order?.orderId ?: "",
            releaseStatus = entity.releaseStatus,
            eventTimestamp = entity.eventTimestamp,
            payload = entity.payload,
        )
}
