package io.github.balaelangovan.orders.app.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid

/**
 * REST request DTO for updating an order.
 * Only notes and billing address can be updated.
 * All fields are optional - only provided fields will be updated.
 */
data class UpdateOrderRequest(
    @field:JsonProperty("notes")
    val notes: String? = null,

    @field:Valid
    @field:JsonProperty("billing_address")
    val billingAddress: AddressRequest? = null,
)
