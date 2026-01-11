package io.github.balaelangovan.orders.app.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * REST request DTO for an address.
 * Common class used by CreateOrderRequest and UpdateOrderRequest.
 */
data class AddressRequest(
    @field:NotBlank(message = "Full name is required")
    @field:JsonProperty("full_name")
    val fullName: String,

    @field:NotBlank(message = "Address line 1 is required")
    @field:JsonProperty("address_line_1")
    val addressLine1: String,

    @field:JsonProperty("address_line_2")
    val addressLine2: String? = null,

    @field:NotBlank(message = "City is required")
    @field:JsonProperty("city")
    val city: String,

    @field:NotBlank(message = "State/Province is required")
    @field:JsonProperty("state_province")
    val stateProvince: String,

    @field:NotBlank(message = "Postal code is required")
    @field:JsonProperty("postal_code")
    val postalCode: String,

    @field:NotBlank(message = "Country is required")
    @field:JsonProperty("country")
    val country: String,

    @field:JsonProperty("phone_number")
    val phoneNumber: String? = null,

    @field:Email(message = "Valid email is required")
    @field:JsonProperty("email")
    val email: String? = null,
)
