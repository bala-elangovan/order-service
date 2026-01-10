package io.github.balaelangovan.orders.domain.valueobject

/**
 * Order a lifecycle status enum with state transition validation.
 */
enum class OrderStatus {
    CREATED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    ;

    /**
     * Validates if transition to a new status is allowed based on the current status.
     */
    fun canTransitionTo(newStatus: OrderStatus): Boolean = when (this) {
        CREATED -> newStatus in setOf(SHIPPED, CANCELLED)
        SHIPPED -> newStatus in setOf(DELIVERED)
        DELIVERED -> false // Terminal state
        CANCELLED -> false // Terminal state
    }

    /**
     * Returns allowed next states from current status.
     */
    fun allowedTransitions(): Set<OrderStatus> = when (this) {
        CREATED -> setOf(SHIPPED, CANCELLED)
        SHIPPED -> setOf(DELIVERED)
        DELIVERED -> emptySet()
        CANCELLED -> emptySet()
    }

    fun isTerminal(): Boolean = this == DELIVERED || this == CANCELLED
}
