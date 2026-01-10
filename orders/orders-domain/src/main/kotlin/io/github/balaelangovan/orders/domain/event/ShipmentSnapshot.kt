package io.github.balaelangovan.orders.domain.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Snapshot of shipment information received from Shipment Service.
 * Each event represents a single shipment action.
 * Order service stores these events and aggregates when querying.
 * Updates are handled via upsert on shipmentId.
 *
 * Uses a hybrid storage approach:
 * - Key fields (shipment_id, order_id, shipment_status, tracking_number) are indexed for queries
 * - Full event payload is stored as JSONB for flexibility and future-proofing
 *
 * Payload structure inspired by Sterling OMS Shipment entity:
 * - shipment_key: Internal system key for the shipment
 * - shipment_no: Sequential shipment number
 * - release_key: Link to parent release
 * - release_no: Release number this shipment fulfills
 * - ship_node: Origin fulfillment node
 * - ship_node_type: DC, STORE, VENDOR
 * - status: Current shipment status code
 * - status_date: Timestamp of last status change
 * - carrier: Shipping carrier (UPS, FedEx, USPS, etc.)
 * - scac: Standard Carrier Alpha Code
 * - service_type: Ground, Express, Overnight, SameDay
 * - bill_of_lading: BOL number
 * - ship_date: Actual ship date
 * - expected_delivery_date: Expected delivery date
 * - actual_delivery_date: Actual delivery timestamp
 * - containers: Array of shipment containers with tracking
 * - shipment_lines: Array of shipment lines with item details
 */
data class ShipmentSnapshot(
    @field:JsonProperty("shipment_id")
    val shipmentId: String,
    @field:JsonProperty("order_id")
    val orderId: String,
    @field:JsonProperty("shipment_status")
    val shipmentStatus: String,
    @field:JsonProperty("tracking_number")
    val trackingNumber: String?,
    @field:JsonProperty("event_timestamp")
    val eventTimestamp: LocalDateTime,
    @field:JsonProperty("payload")
    val payload: Map<String, Any?> = emptyMap(),
)
