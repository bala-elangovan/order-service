package io.github.balaelangovan.orders.domain.valueobject

/**
 * Type-safe Item identifier.
 * Must be a 10-digit numeric value.
 */
@JvmInline
value class ItemId(val value: Long) {
    init {
        require(value in 1_000_000_000..9_999_999_999) {
            "Item ID must be a 10-digit number, got: $value"
        }
    }

    companion object {
        fun of(value: Long): ItemId = ItemId(value)

        fun of(value: String): ItemId {
            require(value.matches(Regex("^\\d{10}$"))) {
                "Item ID must be a 10-digit numeric string, got: $value"
            }
            return ItemId(value.toLong())
        }
    }

    override fun toString(): String = value.toString()
}
