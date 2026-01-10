package io.github.balaelangovan.orders.domain.valueobject

/**
 * Type-safe Customer identifier.
 */
@JvmInline
value class CustomerId(val value: String) {
    init {
        require(value.isNotBlank()) { "Customer ID cannot be blank" }
    }

    companion object {
        fun of(value: String): CustomerId = CustomerId(value)
    }

    override fun toString(): String = value
}
