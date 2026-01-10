package io.github.balaelangovan.orders.domain.valueobject

import java.util.UUID

/**
 * Type-safe Line Item identifier.
 */
@JvmInline
value class LineItemId(val value: UUID) {
    companion object {
        fun generate(): LineItemId = LineItemId(UUID.randomUUID())

        fun of(value: String): LineItemId = LineItemId(UUID.fromString(value))

        fun of(value: UUID): LineItemId = LineItemId(value)
    }

    override fun toString(): String = value.toString()
}
