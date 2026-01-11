package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.exception.InvalidStateTransitionException
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import java.time.LocalDateTime

/**
 * LineStatus entity representing the status of items within an order line.
 * Part of the OrderLine aggregate - cannot exist independently.
 *
 * Tracks quantity and status for fulfillment lifecycle.
 */
data class LineStatus(
    val quantity: Int,
    val status: LineStatusType,
    val statusCode: String = status.code,
    val statusDescription: String = status.description,
    val notes: String? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        require(quantity > 0) { "Quantity must be positive, got: $quantity" }
    }

    /**
     * Updates the status with validation.
     *
     * @param newStatus the new status to transition to
     * @param notes optional notes explaining the status change
     * @return new LineStatus instance with updated status
     * @throws InvalidStateTransitionException if the transition is not allowed
     */
    fun updateStatus(newStatus: LineStatusType, notes: String? = null): LineStatus {
        if (!status.canTransitionTo(newStatus)) {
            throw InvalidStateTransitionException(
                "Cannot transition line status from $status to $newStatus",
            )
        }

        return copy(
            status = newStatus,
            statusCode = newStatus.code,
            statusDescription = newStatus.description,
            notes = notes,
            updatedAt = LocalDateTime.now(),
        )
    }

    /**
     * Checks if the line status is in a terminal state.
     *
     * @return true if status cannot transition further
     */
    fun isTerminal(): Boolean = status.isTerminal()

    companion object {
        /**
         * Creates a new LineStatus in the CREATED state.
         *
         * @param quantity the quantity of items in this status
         * @return new LineStatus with CREATED status
         */
        fun create(quantity: Int): LineStatus = LineStatus(
            quantity = quantity,
            status = LineStatusType.CREATED,
        )
    }
}
