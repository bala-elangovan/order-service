package io.github.balaelangovan.orders.domain.valueobject

/**
 * Type-safe Business Order ID using Kotlin inline value class for zero runtime overhead.
 * Format: {channel}-{YYYYMMDD}-{sequence}
 * Example: 10-20251225-0000001 (Web order on Dec 25, 2025)
 *
 * Channel prefixes:
 * - 10: WEB
 * - 20: MOBILE
 * - 30: API
 * - 40: POS
 * - 50: CALL_CENTER
 */
@JvmInline
value class OrderId(val value: String) {
    init {
        require(value.matches(Regex("\\d{2}-\\d{8}-\\d{7}"))) {
            "Invalid order ID format. Expected: channel-YYYYMMDD-sequence (e.g., 10-20251225-0000001)"
        }
    }

    companion object {
        fun of(value: String): OrderId = OrderId(value)
    }

    override fun toString(): String = value
}
