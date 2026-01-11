package io.github.balaelangovan.orders.app.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * REST response DTO for an address.
 */
data class AddressResponse(
    @field:JsonProperty("id")
    val id: String,

    @field:JsonProperty("full_name")
    val fullName: String,

    @field:JsonProperty("address_line_1")
    val addressLine1: String,

    @field:JsonProperty("address_line_2")
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
    val phoneNumber: String?,

    @field:JsonProperty("email")
    val email: String?,
)
