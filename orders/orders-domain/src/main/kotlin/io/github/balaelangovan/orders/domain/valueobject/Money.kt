package io.github.balaelangovan.orders.domain.valueobject

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

/**
 * Immutable Money value object representing an amount and currency.
 * Uses Kotlin operator overloading for arithmetic operations.
 */
data class Money(val amount: BigDecimal, val currency: Currency) {
    init {
        require(amount.scale() <= 2) {
            "Money amount cannot have more than 2 decimal places"
        }
    }

    companion object {
        val ZERO = Money(BigDecimal.ZERO.setScale(2), Currency.getInstance("USD"))

        fun of(amount: BigDecimal, currencyCode: String): Money = Money(
            amount = amount.setScale(2, RoundingMode.HALF_UP),
            currency = Currency.getInstance(currencyCode),
        )

        fun of(amount: Double, currencyCode: String): Money = Money(
            amount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
            currency = Currency.getInstance(currencyCode),
        )

        fun of(amount: Int, currencyCode: String): Money = Money(
            amount = BigDecimal.valueOf(amount.toLong()).setScale(2, RoundingMode.HALF_UP),
            currency = Currency.getInstance(currencyCode),
        )
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return Money(amount - other.amount, currency)
    }

    operator fun times(factor: Int): Money = Money(amount * BigDecimal.valueOf(factor.toLong()), currency)

    operator fun times(factor: BigDecimal): Money = Money(amount * factor, currency)

    operator fun div(divisor: Int): Money {
        require(divisor != 0) { "Cannot divide by zero" }
        return Money(
            amount.divide(BigDecimal.valueOf(divisor.toLong()), 2, RoundingMode.HALF_UP),
            currency,
        )
    }

    fun isPositive(): Boolean = amount > BigDecimal.ZERO

    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    fun isNegative(): Boolean = amount < BigDecimal.ZERO

    fun isGreaterThan(other: Money): Boolean {
        requireSameCurrency(other)
        return amount > other.amount
    }

    fun isLessThan(other: Money): Boolean {
        requireSameCurrency(other)
        return amount < other.amount
    }

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Cannot perform operation on different currencies: $currency and ${other.currency}"
        }
    }

    override fun toString(): String = "${currency.currencyCode} $amount"
}
