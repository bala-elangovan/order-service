package io.github.balaelangovan.orders.domain.port.inbound

import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot

/**
 * Inbound port for handling shipment events from the Shipment Service.
 * Receives shipment snapshots and persists them for order tracking.
 */
fun interface ShipmentEventHandlerPort {

    /**
     * Handles a shipment snapshot event.
     * Uses upsert semantics - creates new or updates existing based on shipmentId.
     *
     * @param shipmentSnapshot the shipment snapshot data from the event
     */
    suspend fun handleShipmentEvent(shipmentSnapshot: ShipmentSnapshot)
}
