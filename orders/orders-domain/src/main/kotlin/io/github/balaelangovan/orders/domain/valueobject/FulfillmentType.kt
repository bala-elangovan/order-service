package io.github.balaelangovan.orders.domain.valueobject

/**
 * Represents the fulfillment method for an order line.
 * Determines how the item will be delivered to the customer.
 *
 * Note: These are customer-facing fulfillment options.
 * Internal fulfillment decisions (which warehouse, which store ships) are made by
 * downstream services (Allocation, Release, Shipment) and not exposed to guests.
 */
enum class FulfillmentType(val code: String, val description: String) {
    STH("STH", "Ship to Home - Standard delivery to customer address"),
    BOPS("BOPS", "Buy Online Pick in Store - Customer picks up at store"),
    STS("STS", "Ship to Store - Ship to store for customer pickup"),
    ;

    companion object {
        fun fromCode(code: String): FulfillmentType = entries.find { it.code == code }
            ?: throw IllegalArgumentException("Unknown fulfillment type code: $code")

        fun fromCodeOrNameOrDefault(value: String?, default: FulfillmentType = STH): FulfillmentType = value?.let { v ->
            entries.find { it.code == v } ?: entries.find { it.name == v }
        } ?: default
    }
}
