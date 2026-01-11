package io.github.balaelangovan.orders.domain.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Snapshot of release information received from Release Service.
 * Each event represents a single release action.
 * Order service stores these events and aggregates when querying.
 * Updates are handled via upsert on releaseId.
 *
 * Uses a hybrid storage approach:
 * - Key fields (release_id, order_id, release_status) are indexed for queries
 * - Full event payload is stored as JSONB for flexibility and future-proofing
 *
 * Payload structure inspired by Sterling OMS Release entity:
 * - release_key: Internal system key for the release
 * - release_no: Sequential release number within the order (e.g., "0001")
 * - ship_node: Fulfillment node identifier (DC, store, vendor)
 * - ship_node_type: DC, STORE, VENDOR
 * - status: Current release status code
 * - status_date: Timestamp of last status change
 * - requested_ship_date: Customer requested ship date
 * - promised_ship_date: Promised ship date to customer
 * - expected_ship_date: Expected ship date based on node capacity
 * - expected_delivery_date: Expected delivery date
 * - carrier: Carrier assigned for shipping
 * - scac: Standard Carrier Alpha Code
 * - order_lines: Array of release lines with item details
 */
data class ReleaseSnapshot(
    @field:JsonProperty("release_id")
    val releaseId: String,
    @field:JsonProperty("order_id")
    val orderId: String,
    @field:JsonProperty("release_status")
    val releaseStatus: String,
    @field:JsonProperty("event_timestamp")
    val eventTimestamp: LocalDateTime,
    @field:JsonProperty("payload")
    val payload: Map<String, Any?> = emptyMap(),
)
