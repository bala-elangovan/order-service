package io.github.balaelangovan.orders.domain.valueobject

/**
 * Order lifecycle status enum with state transition validation.
 * Supports partial fulfillment states using "IN_*" naming convention.
 */
enum class OrderStatus {
    CREATED,
    IN_RELEASE,
    RELEASED,
    IN_SHIPMENT,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    ;

    /**
     * Validates if transition to a new status is allowed based on the current status.
     */
    fun canTransitionTo(newStatus: OrderStatus): Boolean = when (this) {
        CREATED -> newStatus in setOf(IN_RELEASE, RELEASED, CANCELLED)
        IN_RELEASE -> newStatus in setOf(RELEASED, IN_SHIPMENT, CANCELLED)
        RELEASED -> newStatus in setOf(IN_SHIPMENT, SHIPPED, CANCELLED)
        IN_SHIPMENT -> newStatus in setOf(SHIPPED, CANCELLED)
        SHIPPED -> newStatus == DELIVERED
        DELIVERED -> false // Terminal state
        CANCELLED -> false // Terminal state
    }

    /**
     * Returns allowed next states from current status.
     */
    fun allowedTransitions(): Set<OrderStatus> = when (this) {
        CREATED -> setOf(IN_RELEASE, RELEASED, CANCELLED)
        IN_RELEASE -> setOf(RELEASED, IN_SHIPMENT, CANCELLED)
        RELEASED -> setOf(IN_SHIPMENT, SHIPPED, CANCELLED)
        IN_SHIPMENT -> setOf(SHIPPED, CANCELLED)
        SHIPPED -> setOf(DELIVERED)
        DELIVERED -> emptySet()
        CANCELLED -> emptySet()
    }

    /**
     * Returns true if this is a terminal state (no further transitions allowed).
     */
    fun isTerminal(): Boolean = this == DELIVERED || this == CANCELLED

    /**
     * Returns true if this is a partial/in-progress fulfillment state.
     */
    fun isPartial(): Boolean = this == IN_RELEASE || this == IN_SHIPMENT

    /**
     * Returns true if order lines can still be modified.
     * Only CREATED status allows line modifications.
     */
    fun allowsLineModification(): Boolean = this == CREATED
}
