package io.github.balaelangovan.orders.domain.valueobject

/**
 * Immutable Address value object.
 */
data class Address(
    val fullName: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val stateProvince: String,
    val postalCode: String,
    val country: String,
    val phoneNumber: String?,
    val email: String?,
) {
    init {
        require(fullName.isNotBlank()) { "Full name cannot be blank" }
        require(addressLine1.isNotBlank()) { "Address line 1 cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(stateProvince.isNotBlank()) { "State/Province cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }

        email?.let {
            require(it.contains("@")) { "Invalid email format" }
        }
    }

    companion object {
        fun of(
            fullName: String,
            addressLine1: String,
            addressLine2: String?,
            city: String,
            stateProvince: String,
            postalCode: String,
            country: String,
            phoneNumber: String? = null,
            email: String? = null,
        ): Address = Address(
            fullName, addressLine1, addressLine2,
            city, stateProvince, postalCode, country,
            phoneNumber, email,
        )
    }
}
