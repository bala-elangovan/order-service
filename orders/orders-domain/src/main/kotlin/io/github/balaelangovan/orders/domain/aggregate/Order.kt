package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.exception.InvalidStateTransitionException
import io.github.balaelangovan.orders.domain.exception.ValidationException
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.Channel
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus
import io.github.balaelangovan.orders.domain.valueobject.OrderType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Order aggregate root representing a customer order with rich domain behavior.
 * This is the primary aggregate in the order domain, encapsulating all order-related
 * business rules, including status transitions, line item management, and monetary calculations.
 * Supports multiple order types (STANDARD, GUEST, RETURN) and line-level shipping addresses
 * for mixed fulfillment scenarios where items can ship to different destinations.
 */
data class Order(
    val id: OrderId,
    val orderKey: UUID?,
    val externalOrderId: UUID?,
    val customerId: CustomerId,
    val orderType: OrderType,
    val channel: Channel,
    val lines: List<OrderLine>,
    val status: OrderStatus,
    val billingAddress: Address,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        /**
         * Factory method to create a new order with CREATED status.
         * Validates that at least one line item exists and all business rules are satisfied.
         *
         * @param orderId unique identifier for the order
         * @param externalOrderId external order ID from upstream system (optional)
         * @param customerId identifier of the customer placing the order
         * @param orderType type of order (STANDARD, GUEST, RETURN, etc.)
         * @param channel originating channel (WEB, MOBILE, API, POS)
         * @param lines list of order line items (must not be empty)
         * @param billingAddress billing address for the order
         * @param notes optional notes or special instructions
         * @return newly created Order with CREATED status
         * @throws IllegalArgumentException if lines is empty
         * @throws ValidationException if validation fails
         */
        fun create(
            orderId: OrderId,
            customerId: CustomerId,
            orderType: OrderType,
            channel: Channel,
            lines: List<OrderLine>,
            billingAddress: Address,
            notes: String? = null,
            externalOrderId: UUID? = null,
        ): Order {
            require(lines.isNotEmpty()) { "Order must have at least one line item" }

            val order = Order(
                id = orderId,
                orderKey = null,
                externalOrderId = externalOrderId,
                customerId = customerId,
                orderType = orderType,
                channel = channel,
                lines = lines,
                status = OrderStatus.CREATED,
                billingAddress = billingAddress,
                notes = notes,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

            order.validate()
            return order
        }
    }

    init {
        validate()
    }

    val subtotal: Money
        get() {
            if (lines.isEmpty()) return Money.ZERO
            val currency = lines.first().unitPrice.currency
            return lines.map { it.subtotal }.fold(Money.of(0, currency.currencyCode)) { acc, money -> acc + money }
        }

    val taxAmount: Money
        get() {
            if (lines.isEmpty()) return Money.ZERO
            val currency = lines.first().unitPrice.currency
            return lines.map { it.taxAmount }.fold(Money.of(0, currency.currencyCode)) { acc, money -> acc + money }
        }

    val discountAmount: Money
        get() {
            if (lines.isEmpty()) return Money.ZERO
            val currency = lines.first().unitPrice.currency
            return lines.mapNotNull { it.discountAmount }.fold(Money.of(0, currency.currencyCode)) { acc, money ->
                acc + money
            }
        }

    val totalAmount: Money
        get() = subtotal + taxAmount - discountAmount

    val currency: String
        get() = lines.firstOrNull()?.unitPrice?.currency?.currencyCode ?: "USD"

    /**
     * Validates order business rules including line count, total amount, and currency consistency.
     *
     * @throws ValidationException if any validation rule fails
     */
    fun validate() {
        if (lines.isEmpty()) {
            throw ValidationException("Order must have at least one line item", ErrorCode.INVALID_ORDER)
        }

        if (!totalAmount.isPositive() && totalAmount.amount != BigDecimal.ZERO) {
            throw ValidationException("Order total cannot be negative", ErrorCode.INVALID_ORDER)
        }

        val currencies = lines.map { it.unitPrice.currency }.toSet()
        if (currencies.size > 1) {
            throw ValidationException("All order line items must have the same currency", ErrorCode.INVALID_ORDER)
        }
    }

    /**
     * Adds a new line item to the order. Only allowed when the order is in CREATED status.
     *
     * @param line the order line to add
     * @return new Order instance with the added line
     * @throws ValidationException if order is not in CREATED status or currency mismatch
     */
    fun addLine(line: OrderLine): Order {
        requireCreatedStatus("Cannot add line items to non-created order")
        if (lines.isNotEmpty()) {
            val orderCurrency = lines.first().unitPrice.currency
            if (line.unitPrice.currency != orderCurrency) {
                throw ValidationException(
                    "Line item currency ${line.unitPrice.currency} does not match order currency $orderCurrency",
                    ErrorCode.INVALID_ORDER,
                )
            }
        }

        return copy(lines = lines + line, updatedAt = LocalDateTime.now())
    }

    /**
     * Removes a line item from the order. Cannot remove the last line item.
     *
     * @param lineId identifier of the line to remove
     * @return new Order instance without the specified line
     * @throws ValidationException if the order is not in CREATED status or removing last line
     */
    fun removeLine(lineId: LineItemId): Order {
        requireCreatedStatus("Cannot remove line items from non-created order")
        val newLines = lines.filterNot { it.id == lineId }
        if (newLines.isEmpty()) {
            throw ValidationException(
                "Cannot remove last line item from order. Cancel the order instead.",
                ErrorCode.INVALID_ORDER,
            )
        }
        return copy(lines = newLines, updatedAt = LocalDateTime.now())
    }

    /**
     * Updates the order notes.
     *
     * @param newNotes the new notes value (can be null to clear)
     * @return new Order instance with updated notes
     */
    fun updateNotes(newNotes: String?): Order = copy(notes = newNotes, updatedAt = LocalDateTime.now())

    /**
     * Updates the billing address. Not allowed on terminal orders.
     *
     * @param newBillingAddress the new billing address
     * @return new Order instance with updated billing address
     * @throws ValidationException if the order is in terminal status
     */
    fun updateBillingAddress(newBillingAddress: Address): Order {
        requireModifiableStatus("Cannot update billing address")
        return copy(billingAddress = newBillingAddress, updatedAt = LocalDateTime.now())
    }

    /**
     * Updates the status of a specific line item.
     *
     * @param lineId identifier of the line to update
     * @param newStatus the new status to set
     * @param notes optional notes for the status change
     * @return new Order instance with updated line status
     */
    fun updateLineStatus(lineId: LineItemId, newStatus: LineStatusType, notes: String? = null): Order {
        val updatedLines = lines.map { line ->
            if (line.id == lineId) {
                line.updateStatus(newStatus, notes)
            } else {
                line
            }
        }
        return copy(lines = updatedLines, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to SHIPPED status.
     *
     * @return new Order instance with SHIPPED status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun ship(): Order {
        validateTransition(OrderStatus.SHIPPED)
        return copy(status = OrderStatus.SHIPPED, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to DELIVERED status.
     *
     * @return new Order instance with DELIVERED status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun deliver(): Order {
        validateTransition(OrderStatus.DELIVERED)
        return copy(status = OrderStatus.DELIVERED, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to CANCELLED status.
     *
     * @return new Order instance with CANCELLED status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun cancel(): Order {
        validateTransition(OrderStatus.CANCELLED)
        return copy(status = OrderStatus.CANCELLED, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to IN_RELEASE status (partial release).
     * Indicates some but not all lines have been released for fulfillment.
     *
     * @return new Order instance with IN_RELEASE status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun inRelease(): Order {
        validateTransition(OrderStatus.IN_RELEASE)
        return copy(status = OrderStatus.IN_RELEASE, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to RELEASED status (full release).
     * Indicates all lines have been released for fulfillment.
     *
     * @return new Order instance with RELEASED status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun release(): Order {
        validateTransition(OrderStatus.RELEASED)
        return copy(status = OrderStatus.RELEASED, updatedAt = LocalDateTime.now())
    }

    /**
     * Transitions the order to IN_SHIPMENT status (partial shipment).
     * Indicates some but not all lines have been shipped.
     *
     * @return new Order instance with IN_SHIPMENT status
     * @throws InvalidStateTransitionException if transition is not allowed from current status
     */
    fun inShipment(): Order {
        validateTransition(OrderStatus.IN_SHIPMENT)
        return copy(status = OrderStatus.IN_SHIPMENT, updatedAt = LocalDateTime.now())
    }

    /**
     * @return number of line items in the order
     */
    fun lineCount(): Int = lines.size

    /**
     * @return sum of quantities across all line items
     */
    fun totalQuantity(): Int = lines.sumOf { it.quantity }

    /**
     * Checks if the order contains a specific item.
     *
     * @param itemId the item identifier to search for
     * @return true if any line contains the specified item
     */
    fun containsItem(itemId: ItemId): Boolean = lines.any { it.itemId == itemId }

    /**
     * Retrieves a specific line item by its identifier.
     *
     * @param lineId the line identifier to find
     * @return the matching OrderLine or null if not found
     */
    fun getLine(lineId: LineItemId): OrderLine? = lines.find { it.id == lineId }

    /**
     * @return true if order is in CREATED status and can be modified
     */
    fun isModifiable(): Boolean = status.allowsLineModification()

    /**
     * @return true if order is in a terminal status (DELIVERED or CANCELLED)
     */
    fun isTerminal(): Boolean = status.isTerminal()

    /**
     * @return true if order is in a partial fulfillment state (IN_RELEASE or IN_SHIPMENT)
     */
    fun isPartialFulfillment(): Boolean = status.isPartial()

    /**
     * @return true if the order can be cancelled from current status
     */
    fun canCancel(): Boolean = status.canTransitionTo(OrderStatus.CANCELLED)

    /**
     * @return true if this is a guest checkout order
     */
    fun isGuestOrder(): Boolean = orderType == OrderType.GUEST

    /**
     * @return true if this is a return order
     */
    fun isReturnOrder(): Boolean = orderType == OrderType.RETURN

    /**
     * Calculates shipping cost based on order total and line count.
     * Free shipping for orders over $100; $5 for 5+ items; otherwise $10.
     *
     * @return shipping cost as Money in the order's currency
     */
    fun calculateShippingCost(): Money {
        if (totalAmount.amount >= BigDecimal("100.00")) {
            return Money.of(0, currency)
        }

        return if (lineCount() > 5) {
            Money.of(5, currency)
        } else {
            Money.of(10, currency)
        }
    }

    /**
     * Calculates volume discount based on order total.
     * 10% discount for orders over $200; 5% for orders over $100.
     *
     * @return discount amount as Money in the order's currency
     */
    fun calculateVolumeDiscount(): Money {
        val amount = totalAmount.amount

        val discountPercentage = when {
            amount >= BigDecimal("200.00") -> BigDecimal("0.10")
            amount >= BigDecimal("100.00") -> BigDecimal("0.05")
            else -> BigDecimal.ZERO
        }

        val discountValue = amount.multiply(discountPercentage)
            .setScale(2, java.math.RoundingMode.HALF_UP)

        return Money.of(discountValue, currency)
    }

    /**
     * Validates whether all items in the order can be fulfilled from available inventory.
     *
     * @param inventory map of item IDs to available quantities
     * @return true if all line items have sufficient inventory
     */
    fun canFulfillFrom(inventory: Map<ItemId, Int>): Boolean = lines.all { line ->
        val availableQuantity = inventory[line.itemId] ?: 0
        availableQuantity >= line.quantity
    }

    /**
     * Calculates the final order amount including shipping and after applying volume discounts.
     *
     * @return final amount as Money (order total - volume discount + shipping)
     */
    fun calculateFinalAmount(): Money {
        val volumeDiscount = calculateVolumeDiscount()
        val shipping = calculateShippingCost()
        return totalAmount - volumeDiscount + shipping
    }

    /**
     * Validates that a status transition is allowed from current status to new status.
     *
     * @param newStatus the target status to transition to
     * @throws InvalidStateTransitionException if the transition is not allowed
     */
    private fun validateTransition(newStatus: OrderStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw InvalidStateTransitionException(
                "Cannot transition from $status to $newStatus. Allowed transitions: ${status.allowedTransitions()}",
            )
        }
    }

    /**
     * Validates that the order is in CREATED status.
     *
     * @param message the error message to use if validation fails
     * @throws ValidationException if the order is not in CREATED status
     */
    private fun requireCreatedStatus(message: String) {
        if (status != OrderStatus.CREATED) {
            throw ValidationException("$message (current status: $status)", ErrorCode.INVALID_ORDER)
        }
    }

    /**
     * Validates that the order is not in a terminal status.
     *
     * @param message the error message to use if validation fails
     * @throws ValidationException if the order is in a terminal status
     */
    private fun requireModifiableStatus(message: String) {
        if (status.isTerminal()) {
            throw ValidationException("$message on terminal order (current status: $status)", ErrorCode.INVALID_ORDER)
        }
    }
}
