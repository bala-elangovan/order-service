package io.github.balaelangovan.orders.app.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

/**
 * REST request DTO for creating a new order.
 * Requires customer ID, channel, at least one line item, and billing address.
 * Supports multiple order types (STANDARD, GUEST, RETURN) and line-level
 * shipping addresses for mixed fulfillment scenarios.
 */
data class CreateOrderRequest(
    @field:NotBlank(message = "Customer ID is required")
    @field:JsonProperty("customer_id")
    val customerId: String,

    @field:JsonProperty("order_type")
    val orderType: String = "STANDARD",

    @field:NotBlank(message = "Channel is required")
    @field:JsonProperty("channel")
    val channel: String,

    @field:NotEmpty(message = "Order must have at least one line item")
    @field:Valid
    @field:JsonProperty("lines")
    val lines: List<OrderLine>,

    @field:Valid
    @field:JsonProperty("billing_address")
    val billingAddress: AddressRequest,

    @field:JsonProperty("notes")
    val notes: String? = null,
) {
    /**
     * REST request DTO for an order line.
     * Each line must have its own shipping address (required).
     * Fulfillment type determines how the line will be fulfilled.
     */
    data class OrderLine(
        @field:NotBlank(message = "Item ID is required")
        @field:JsonProperty("item_id")
        val itemId: String,

        @field:NotBlank(message = "Item name is required")
        @field:JsonProperty("item_name")
        val itemName: String,

        @field:JsonProperty("item_description")
        val itemDescription: String? = null,

        @field:Min(value = 1, message = "Quantity must be at least 1")
        @field:JsonProperty("quantity")
        val quantity: Int,

        @field:DecimalMin(value = "0.01", message = "Unit price must be positive")
        @field:JsonProperty("unit_price")
        val unitPrice: BigDecimal,

        @field:NotBlank(message = "Currency is required")
        @field:JsonProperty("currency")
        val currency: String,

        @field:JsonProperty("tax_rate")
        val taxRate: BigDecimal? = null,

        @field:JsonProperty("discount_amount")
        val discountAmount: BigDecimal? = null,

        @field:JsonProperty("fulfillment_type")
        val fulfillmentType: String = "STH",

        @field:Valid
        @field:NotNull(message = "Shipping address is required for each line")
        @field:JsonProperty("shipping_address")
        var shippingAddress: AddressRequest,

        @field:JsonProperty("estimated_ship_date")
        val estimatedShipDate: LocalDate? = null,

        @field:JsonProperty("estimated_delivery_date")
        val estimatedDeliveryDate: LocalDate? = null,

        @field:JsonProperty("promised_ship_date")
        val promisedShipDate: LocalDate? = null,

        @field:JsonProperty("promised_delivery_date")
        val promisedDeliveryDate: LocalDate? = null,
    )
}
