package io.github.balaelangovan.orders.domain.valueobject

/**
 * Status for order line items tracking the fulfillment lifecycle.
 * Supports various order types: guest orders, return orders, standard orders.
 */
enum class LineStatusType(val code: String, val description: String) {
    CREATED("100", "Order line created"),
    ALLOCATED("200", "Inventory allocated"),
    RELEASED("300", "Released for fulfillment"),
    SHIPPED("400", "Shipped from warehouse"),
    SHIPPED_AND_INVOICED("410", "Shipped and invoiced"),
    DELIVERED("500", "Delivered to customer"),
    RETURN_INITIATED("600", "Return initiated by customer"),
    RETURN_COMPLETED("700", "Return completed and processed"),
    CANCELLED("900", "Order line cancelled"),
    ;

    fun canTransitionTo(newStatus: LineStatusType): Boolean = when (this) {
        CREATED -> newStatus in setOf(ALLOCATED, CANCELLED)
        ALLOCATED -> newStatus in setOf(RELEASED, CANCELLED)
        RELEASED -> newStatus in setOf(SHIPPED, CANCELLED)
        SHIPPED -> newStatus in setOf(SHIPPED_AND_INVOICED, DELIVERED, RETURN_INITIATED)
        SHIPPED_AND_INVOICED -> newStatus in setOf(DELIVERED, RETURN_INITIATED)
        DELIVERED -> newStatus == RETURN_INITIATED
        RETURN_INITIATED -> newStatus == RETURN_COMPLETED
        RETURN_COMPLETED, CANCELLED -> false // Terminal states
    }

    fun isTerminal(): Boolean = this in setOf(DELIVERED, RETURN_COMPLETED, CANCELLED)

    companion object {
        fun fromCode(code: String): LineStatusType = entries.find { it.code == code }
            ?: throw IllegalArgumentException("Unknown status code: $code")
    }
}
