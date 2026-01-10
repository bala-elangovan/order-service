package io.github.balaelangovan.orders.domain.port.outbound

import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot

/**
 * Outbound port for persisting shipment snapshots.
 * Provides upsert semantics for shipment events from the Shipment Service.
 */
interface ShipmentSnapshotRepositoryPort {

    /**
     * Saves or updates a shipment snapshot using upsert semantics.
     * If a snapshot with the same shipmentId exists, it will be updated.
     *
     * @param shipmentSnapshot the shipment snapshot to save
     * @return the saved shipment snapshot
     */
    suspend fun upsert(shipmentSnapshot: ShipmentSnapshot): ShipmentSnapshot

    /**
     * Finds a shipment snapshot by its shipment ID.
     *
     * @param shipmentId the shipment identifier
     * @return the shipment snapshot or null if not found
     */
    suspend fun findByShipmentId(shipmentId: String): ShipmentSnapshot?

    /**
     * Finds all shipment snapshots for an order.
     *
     * @param orderId the order identifier
     * @return list of shipment snapshots for the order
     */
    suspend fun findByOrderId(orderId: String): List<ShipmentSnapshot>

    /**
     * Finds a shipment snapshot by tracking number.
     *
     * @param trackingNumber the tracking number
     * @return the shipment snapshot or null if not found
     */
    suspend fun findByTrackingNumber(trackingNumber: String): ShipmentSnapshot?
}
