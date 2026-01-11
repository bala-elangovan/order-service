package io.github.balaelangovan.orders.domain.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain event representing an order created in the checkout service.
 * This event is consumed by the order-management-service via Kafka.
 *
 * Contains complete order information for order creation.
 * Shipping addresses are required at line level to support mixed fulfillment.
 */
data class OrderCreatedEvent(
    @field:JsonProperty("external_order_id")
    val externalOrderId: UUID,
    @field:JsonProperty("customer_id")
    val customerId: String,
    @field:JsonProperty("order_type")
    val orderType: String = "STANDARD",
    @field:JsonProperty("channel")
    val channel: String,
    @field:JsonProperty("order_lines")
    val orderLines: List<OrderLine>,
    @field:JsonProperty("billing_address")
    val billingAddress: Address,
    @field:JsonProperty("notes")
    val notes: String?,
    @field:JsonProperty("status_tracking")
    val statusTracking: StatusTracking? = null,
    @field:JsonProperty("timestamp")
    val timestamp: LocalDateTime,
) {
    /**
     * Order line information within the event.
     * Each line must have its own shipping address (required).
     * Fulfillment type determines how the line will be fulfilled.
     */
    data class OrderLine(
        @field:JsonProperty("line_number")
        val lineNumber: Int,
        @field:JsonProperty("item_id")
        val itemId: Long,
        @field:JsonProperty("item_name")
        val itemName: String,
        @field:JsonProperty("item_description")
        val itemDescription: String?,
        @field:JsonProperty("quantity")
        val quantity: Int,
        @field:JsonProperty("unit_price")
        val unitPrice: BigDecimal,
        @field:JsonProperty("currency")
        val currency: String,
        @field:JsonProperty("tax_rate")
        val taxRate: BigDecimal,
        @field:JsonProperty("discount_amount")
        val discountAmount: BigDecimal?,
        @field:JsonProperty("fulfillment_type")
        val fulfillmentType: String = "STH",
        @field:JsonProperty("shipping_address")
        val shippingAddress: Address,
        @field:JsonProperty("estimated_ship_date")
        val estimatedShipDate: LocalDate? = null,
        @field:JsonProperty("estimated_delivery_date")
        val estimatedDeliveryDate: LocalDate? = null,
    )

    /**
     * Address information within the event.
     */
    data class Address(
        @field:JsonProperty("full_name")
        val fullName: String,
        @field:JsonProperty("address_line1")
        val addressLine1: String,
        @field:JsonProperty("address_line2")
        val addressLine2: String?,
        @field:JsonProperty("city")
        val city: String,
        @field:JsonProperty("state_province")
        val stateProvince: String,
        @field:JsonProperty("postal_code")
        val postalCode: String,
        @field:JsonProperty("country")
        val country: String,
        @field:JsonProperty("phone_number")
        val phoneNumber: String,
        @field:JsonProperty("email")
        val email: String,
    )

    /**
     * Snapshot of order status tracking history.
     */
    data class StatusTracking(
        @field:JsonProperty("current_status")
        val currentStatus: String,
        @field:JsonProperty("status_history")
        val statusHistory: List<StatusChange>,
    )

    /**
     * Individual status change entry.
     */
    data class StatusChange(
        @field:JsonProperty("status")
        val status: String,
        @field:JsonProperty("timestamp")
        val timestamp: LocalDateTime,
        @field:JsonProperty("notes")
        val notes: String?,
    )
}
