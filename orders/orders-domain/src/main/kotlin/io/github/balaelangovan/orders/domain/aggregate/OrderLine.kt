package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.Money
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

/**
 * OrderLine entity representing a line item within an order aggregate.
 * Cannot exist independently and must be part of an Order. Each line tracks its own
 * fulfillment type and shipping address to support mixed fulfillment scenarios.
 * Date fields include both estimated dates (ESD/EDD from the checkout/ promise system)
 * and promised dates (PSD/PDD from the allocation service after inventory confirmation).
 */
data class OrderLine(
    val id: LineItemId,
    val lineNumber: Int,
    val itemId: ItemId,
    val itemName: String,
    val itemDescription: String?,
    val quantity: Int,
    val unitPrice: Money,
    val taxRate: BigDecimal?,
    val discountAmount: Money?,
    val fulfillmentType: FulfillmentType,
    val shippingAddress: Address?,
    val lineStatus: LineStatus,
    val estimatedShipDate: LocalDate?,
    val estimatedDeliveryDate: LocalDate?,
    val promisedShipDate: LocalDate?,
    val promisedDeliveryDate: LocalDate?,
) {
    init {
        require(quantity > 0) { "Quantity must be positive, got: $quantity" }
        require(unitPrice.isPositive()) { "Unit price must be positive" }
        require(itemName.isNotBlank()) { "Item name cannot be blank" }

        taxRate?.let {
            require(it >= BigDecimal.ZERO) { "Tax rate cannot be negative" }
        }

        discountAmount?.let {
            require(it.currency == unitPrice.currency) {
                "Discount currency must match unit price currency"
            }
        }

        if (fulfillmentType in setOf(FulfillmentType.STH, FulfillmentType.STS)) {
            requireNotNull(shippingAddress) {
                "Shipping address is required for fulfillment type: $fulfillmentType"
            }
        }

        if (estimatedShipDate != null && estimatedDeliveryDate != null) {
            require(!estimatedDeliveryDate.isBefore(estimatedShipDate)) {
                "Estimated delivery date cannot be before estimated ship date"
            }
        }

        if (promisedShipDate != null && promisedDeliveryDate != null) {
            require(!promisedDeliveryDate.isBefore(promisedShipDate)) {
                "Promised delivery date cannot be before promised ship date"
            }
        }
    }

    /** Calculated subtotal: unitPrice * quantity */
    val subtotal: Money
        get() = unitPrice * quantity

    /** Calculated tax amount based on subtotal and taxRate */
    val taxAmount: Money
        get() {
            val rate = taxRate ?: BigDecimal.ZERO
            val taxValue = subtotal.amount.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP)
            return Money.of(taxValue, unitPrice.currency.currencyCode)
        }

    /** Total line amount: subtotal + tax - discount */
    val totalAmount: Money
        get() {
            var total = subtotal + taxAmount
            discountAmount?.let {
                total -= it
            }
            return total
        }

    /**
     * Updates the line status with optional notes.
     *
     * @param newStatus the new status to set
     * @param notes optional notes explaining the status change
     * @return new OrderLine instance with updated status
     */
    fun updateStatus(newStatus: LineStatusType, notes: String? = null): OrderLine {
        val updatedStatus = lineStatus.updateStatus(newStatus, notes)
        return copy(lineStatus = updatedStatus)
    }

    /**
     * Updates the promised dates received from allocation service.
     *
     * @param promisedShipDate the confirmed ship date
     * @param promisedDeliveryDate the confirmed delivery date
     * @return new OrderLine instance with updated dates
     */
    fun updatePromisedDates(promisedShipDate: LocalDate, promisedDeliveryDate: LocalDate): OrderLine = copy(
        promisedShipDate = promisedShipDate,
        promisedDeliveryDate = promisedDeliveryDate,
    )

    /**
     * @return the current status of this line item
     */
    fun getCurrentStatus(): LineStatusType = lineStatus.status

    /**
     * @return true if a line has reached DELIVERED status
     */
    fun isDelivered(): Boolean = lineStatus.status == LineStatusType.DELIVERED

    /**
     * @return true if a line has been shipped (includes SHIPPED, SHIPPED_AND_INVOICED, DELIVERED)
     */
    fun isShipped(): Boolean = lineStatus.status in setOf(
        LineStatusType.SHIPPED,
        LineStatusType.SHIPPED_AND_INVOICED,
        LineStatusType.DELIVERED,
    )

    companion object {
        /**
         * Factory method to create a new OrderLine with CREATED status.
         *
         * @param lineNumber sequential line number within the order
         * @param itemId identifier of the item being ordered
         * @param itemName display name of the item
         * @param itemDescription optional description of the item
         * @param quantity number of units ordered (must be positive)
         * @param unitPrice price per unit
         * @param taxRate tax rate as decimal (e.g., 0.08 for 8%)
         * @param discountAmount optional discount applied to this line
         * @param fulfillmentType how this line will be fulfilled (STH, BOPS, STS)
         * @param shippingAddress shipping destination (required for STH/STS)
         * @param estimatedShipDate estimated ship date from checkout
         * @param estimatedDeliveryDate estimated delivery date from checkout
         * @param promisedShipDate confirmed ship date from allocation
         * @param promisedDeliveryDate confirmed delivery date from allocation
         * @return new OrderLine with generated ID and CREATED status
         */
        fun create(
            lineNumber: Int,
            itemId: ItemId,
            itemName: String,
            itemDescription: String?,
            quantity: Int,
            unitPrice: Money,
            taxRate: BigDecimal?,
            discountAmount: Money?,
            fulfillmentType: FulfillmentType,
            shippingAddress: Address?,
            estimatedShipDate: LocalDate? = null,
            estimatedDeliveryDate: LocalDate? = null,
            promisedShipDate: LocalDate? = null,
            promisedDeliveryDate: LocalDate? = null,
        ): OrderLine {
            val lineStatus = LineStatus.create(quantity)

            return OrderLine(
                id = LineItemId.generate(),
                lineNumber = lineNumber,
                itemId = itemId,
                itemName = itemName,
                itemDescription = itemDescription,
                quantity = quantity,
                unitPrice = unitPrice,
                taxRate = taxRate,
                discountAmount = discountAmount,
                fulfillmentType = fulfillmentType,
                shippingAddress = shippingAddress,
                lineStatus = lineStatus,
                estimatedShipDate = estimatedShipDate,
                estimatedDeliveryDate = estimatedDeliveryDate,
                promisedShipDate = promisedShipDate,
                promisedDeliveryDate = promisedDeliveryDate,
            )
        }
    }
}
