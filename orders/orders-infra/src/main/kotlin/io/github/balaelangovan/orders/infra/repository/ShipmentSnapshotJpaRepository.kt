package io.github.balaelangovan.orders.infra.repository

import io.github.balaelangovan.orders.infra.entity.ShipmentSnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * JPA repository for ShipmentSnapshot entities.
 * Supports upsert semantics via findByShipmentId (shipment_id has a unique constraint).
 *
 * Uses hybrid storage approach - key fields are indexed columns,
 * full payload is stored as JSONB. For queries on payload fields,
 * use native queries with JSONB operators.
 */
@Repository
interface ShipmentSnapshotJpaRepository : JpaRepository<ShipmentSnapshotEntity, Long> {

    /**
     * Find by shipment ID for upsert operations.
     * Used when receiving shipment events from Shipment Service.
     */
    fun findByShipmentId(shipmentId: String): Optional<ShipmentSnapshotEntity>

    /**
     * Find all shipments for a specific order by order ID.
     */
    fun findByOrderOrderId(orderId: String): List<ShipmentSnapshotEntity>

    /**
     * Find shipments by tracking number.
     */
    fun findByTrackingNumber(trackingNumber: String): Optional<ShipmentSnapshotEntity>
}
