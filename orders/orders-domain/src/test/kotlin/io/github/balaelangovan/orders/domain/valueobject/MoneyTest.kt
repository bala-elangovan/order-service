package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.util.Currency

class MoneyTest :
    DescribeSpec({

        describe("Money creation") {
            it("should create money with BigDecimal amount") {
                val money = Money.of(BigDecimal("10.50"), "USD")

                money.amount shouldBe BigDecimal("10.50")
                money.currency shouldBe Currency.getInstance("USD")
            }

            it("should create money with Double amount") {
                val money = Money.of(10.50, "USD")

                money.amount shouldBe BigDecimal("10.50")
                money.currency shouldBe Currency.getInstance("USD")
            }

            it("should create money with Int amount") {
                val money = Money.of(10, "USD")

                money.amount shouldBe BigDecimal("10.00")
                money.currency shouldBe Currency.getInstance("USD")
            }

            it("should round to 2 decimal places") {
                val money = Money.of(BigDecimal("10.555"), "USD")

                money.amount shouldBe BigDecimal("10.56")
            }

            it("should fail for more than 2 decimal places without rounding") {
                shouldThrow<IllegalArgumentException> {
                    Money(BigDecimal("10.555"), Currency.getInstance("USD"))
                }
            }

            it("should have ZERO constant") {
                Money.ZERO.amount shouldBe BigDecimal("0.00")
                Money.ZERO.currency shouldBe Currency.getInstance("USD")
            }
        }

        describe("Money arithmetic operations") {
            val usd10 = Money.of(10, "USD")
            val usd5 = Money.of(5, "USD")
            val eur10 = Money.of(10, "EUR")

            describe("addition") {
                it("should add two money values with same currency") {
                    val result = usd10 + usd5

                    result.amount shouldBe BigDecimal("15.00")
                    result.currency shouldBe Currency.getInstance("USD")
                }

                it("should fail when adding different currencies") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        usd10 + eur10
                    }
                    exception.message shouldContain "different currencies"
                }
            }

            describe("subtraction") {
                it("should subtract two money values with same currency") {
                    val result = usd10 - usd5

                    result.amount shouldBe BigDecimal("5.00")
                    result.currency shouldBe Currency.getInstance("USD")
                }

                it("should allow negative result") {
                    val result = usd5 - usd10

                    result.amount shouldBe BigDecimal("-5.00")
                }

                it("should fail when subtracting different currencies") {
                    val exception = shouldThrow<IllegalArgumentException> {
                        usd10 - eur10
                    }
                    exception.message shouldContain "different currencies"
                }
            }

            describe("multiplication") {
                it("should multiply by integer") {
                    val result = usd10 * 3

                    result.amount shouldBe BigDecimal("30.00")
                    result.currency shouldBe Currency.getInstance("USD")
                }

                it("should multiply by BigDecimal with valid scale") {
                    val result = usd10 * BigDecimal("2")

                    result.amount shouldBe BigDecimal("20.00")
                    result.currency shouldBe Currency.getInstance("USD")
                }

                it("should fail when multiplication results in more than 2 decimal places") {
                    shouldThrow<IllegalArgumentException> {
                        usd10 * BigDecimal("1.5") // 10.00 * 1.5 = 15.000 (scale 3)
                    }
                }
            }

            describe("division") {
                it("should divide by integer") {
                    val result = usd10 / 2

                    result.amount shouldBe BigDecimal("5.00")
                    result.currency shouldBe Currency.getInstance("USD")
                }

                it("should round result to 2 decimal places") {
                    val money = Money.of(10, "USD")
                    val result = money / 3

                    result.amount shouldBe BigDecimal("3.33")
                }

                it("should fail when dividing by zero") {
                    shouldThrow<IllegalArgumentException> {
                        usd10 / 0
                    }
                }
            }
        }

        describe("Money comparison methods") {
            val positive = Money.of(10, "USD")
            val zero = Money.of(0, "USD")
            val negative = Money.of(-5, "USD")
            val eur10 = Money.of(10, "EUR")

            it("should detect positive amounts") {
                positive.isPositive() shouldBe true
                zero.isPositive() shouldBe false
                negative.isPositive() shouldBe false
            }

            it("should detect zero amounts") {
                zero.isZero() shouldBe true
                positive.isZero() shouldBe false
                negative.isZero() shouldBe false
            }

            it("should detect negative amounts") {
                negative.isNegative() shouldBe true
                zero.isNegative() shouldBe false
                positive.isNegative() shouldBe false
            }

            it("should compare greater than") {
                positive.isGreaterThan(zero) shouldBe true
                zero.isGreaterThan(positive) shouldBe false
                positive.isGreaterThan(positive) shouldBe false
            }

            it("should compare less than") {
                zero.isLessThan(positive) shouldBe true
                positive.isLessThan(zero) shouldBe false
                positive.isLessThan(positive) shouldBe false
            }

            it("should fail comparison with different currencies") {
                shouldThrow<IllegalArgumentException> {
                    positive.isGreaterThan(eur10)
                }
                shouldThrow<IllegalArgumentException> {
                    positive.isLessThan(eur10)
                }
            }
        }

        describe("Money toString") {
            it("should format as currency code and amount") {
                val money = Money.of(25.99, "USD")

                money.toString() shouldBe "USD 25.99"
            }
        }
    })
