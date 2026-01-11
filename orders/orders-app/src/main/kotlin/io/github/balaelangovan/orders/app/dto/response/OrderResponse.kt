package io.github.balaelangovan.orders.app.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * REST response DTO representing a complete order with all related data.
 * Includes order metadata, line items, calculated totals, and addresses.
 * Uses snake_case JSON property names for API consistency.
 */
data class OrderResponse(
    @field:JsonProperty("id")
    val id: String,

    @field:JsonProperty("order_key")
    val orderKey: String?,

    @field:JsonProperty("customer_id")
    val customerId: String,

    @field:JsonProperty("order_type")
    val orderType: String,

    @field:JsonProperty("channel")
    val channel: String,

    @field:JsonProperty("lines")
    val lines: List<OrderLine>,

    @field:JsonProperty("status")
    val status: String,

    @field:JsonProperty("total_amount")
    val totalAmount: BigDecimal,

    @field:JsonProperty("currency")
    val currency: String,

    @field:JsonProperty("subtotal")
    val subtotal: BigDecimal,

    @field:JsonProperty("tax_amount")
    val taxAmount: BigDecimal,

    @field:JsonProperty("discount_amount")
    val discountAmount: BigDecimal,

    @field:JsonProperty("billing_address")
    val billingAddress: AddressResponse,

    @field:JsonProperty("notes")
    val notes: String?,

    @field:JsonProperty("line_count")
    val lineCount: Int,

    @field:JsonProperty("total_quantity")
    val totalQuantity: Int,

    @field:JsonProperty("releases")
    val releases: List<Map<String, Any?>>,

    @field:JsonProperty("shipments")
    val shipments: List<Map<String, Any?>>,

    @field:JsonProperty("created_at")
    val createdAt: LocalDateTime,

    @field:JsonProperty("updated_at")
    val updatedAt: LocalDateTime,
) {
    /**
     * REST response DTO for an order line.
     */
    data class OrderLine(
        @field:JsonProperty("line_key")
        val lineKey: String,

        @field:JsonProperty("line_number")
        val lineNumber: Int,

        @field:JsonProperty("item_id")
        val itemId: String,

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

        @field:JsonProperty("subtotal")
        val subtotal: BigDecimal,

        @field:JsonProperty("tax_rate")
        val taxRate: BigDecimal?,

        @field:JsonProperty("tax_amount")
        val taxAmount: BigDecimal,

        @field:JsonProperty("discount_amount")
        val discountAmount: BigDecimal?,

        @field:JsonProperty("total_amount")
        val totalAmount: BigDecimal,

        @field:JsonProperty("fulfillment_type")
        val fulfillmentType: String,

        @field:JsonProperty("shipping_address")
        val shippingAddress: AddressResponse?,

        @field:JsonProperty("estimated_ship_date")
        val estimatedShipDate: LocalDate?,

        @field:JsonProperty("estimated_delivery_date")
        val estimatedDeliveryDate: LocalDate?,

        @field:JsonProperty("promised_ship_date")
        val promisedShipDate: LocalDate?,

        @field:JsonProperty("promised_delivery_date")
        val promisedDeliveryDate: LocalDate?,

        @field:JsonProperty("line_status")
        val lineStatus: LineStatus,
    )

    /**
     * REST response DTO for a line status.
     */
    data class LineStatus(
        @field:JsonProperty("quantity")
        val quantity: Int,

        @field:JsonProperty("status")
        val status: String,

        @field:JsonProperty("status_code")
        val statusCode: String,

        @field:JsonProperty("status_description")
        val statusDescription: String,

        @field:JsonProperty("notes")
        val notes: String?,

        @field:JsonProperty("updated_at")
        val updatedAt: LocalDateTime,
    )
}
