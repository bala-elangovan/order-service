package io.github.balaelangovan.orders.domain.valueobject

/**
 * Represents the type of order.
 * Supports various order types for different business scenarios.
 */
enum class OrderType(val code: String, val description: String) {
    STANDARD("STD", "Standard customer order"),
    GUEST("GUEST", "Guest checkout order - no registered account"),
    RETURN("RET", "Return order - customer returning items"),
    EXCHANGE("EXCH", "Exchange order - swap for different item"),
    STORE("STORE", "In-store order - placed at physical store"),
    SUBSCRIPTION("SUB", "Subscription order - recurring order"),
    ;

    companion object {
        fun fromCode(code: String): OrderType = entries.find { it.code == code }
            ?: throw IllegalArgumentException("Unknown order type code: $code")

        fun fromNameOrDefault(name: String?, default: OrderType = STANDARD): OrderType =
            name?.let { entries.find { entry -> entry.name == it } } ?: default
    }
}
