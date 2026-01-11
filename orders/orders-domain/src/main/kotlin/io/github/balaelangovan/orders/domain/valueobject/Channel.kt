package io.github.balaelangovan.orders.domain.valueobject

/**
 * Represents the channel through which an order was placed.
 * Each channel has a unique numeric prefix for order ID generation.
 *
 * Order ID Format: {channel}-{YYYYMMDD}-{sequence}
 * Example: 10-20251225-0000001 (Web order on Dec 25, 2025)
 */
enum class Channel(val prefix: Int, val description: String) {
    WEB(10, "Web - Desktop Browser"),
    MOBILE(20, "Mobile - iOS/Android App"),
    API(30, "API - Third-party Integration"),
    POS(40, "POS - Point of Sale"),
    CALL_CENTER(50, "Call Center - Phone Orders"),
    ;

    companion object {
        fun fromPrefix(prefix: Int): Channel = entries.find { it.prefix == prefix }
            ?: throw IllegalArgumentException("Unknown channel prefix: $prefix")

        fun fromNameOrDefault(name: String?, default: Channel = API): Channel =
            name?.let { entries.find { entry -> entry.name == it } } ?: default
    }
}
