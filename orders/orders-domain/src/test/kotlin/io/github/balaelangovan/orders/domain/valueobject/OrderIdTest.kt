package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class OrderIdTest :
    DescribeSpec({

        describe("OrderId creation") {
            it("should create valid order ID with correct format") {
                val orderId = OrderId("10-20251225-0000001")

                orderId.value shouldBe "10-20251225-0000001"
            }

            it("should create order ID using of() factory method") {
                val orderId = OrderId.of("20-20251231-9999999")

                orderId.value shouldBe "20-20251231-9999999"
            }

            it("should accept all valid channel prefixes") {
                val webOrder = OrderId("10-20251225-0000001")
                val mobileOrder = OrderId("20-20251225-0000001")
                val apiOrder = OrderId("30-20251225-0000001")
                val posOrder = OrderId("40-20251225-0000001")
                val callCenterOrder = OrderId("50-20251225-0000001")

                webOrder.value shouldBe "10-20251225-0000001"
                mobileOrder.value shouldBe "20-20251225-0000001"
                apiOrder.value shouldBe "30-20251225-0000001"
                posOrder.value shouldBe "40-20251225-0000001"
                callCenterOrder.value shouldBe "50-20251225-0000001"
            }
        }

        describe("OrderId validation") {
            it("should fail for empty string") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderId("")
                }
                exception.message shouldContain "Invalid order ID format"
            }

            it("should fail for missing channel prefix") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("20251225-0000001")
                }
            }

            it("should fail for wrong date format") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("10-2025125-0000001") // 7 digits instead of 8
                }
            }

            it("should fail for wrong sequence format") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("10-20251225-000001") // 6 digits instead of 7
                }
            }

            it("should fail for letters in the format") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("AB-20251225-0000001")
                }
            }

            it("should fail for wrong separator") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("10_20251225_0000001")
                }
            }

            it("should fail for single digit channel") {
                shouldThrow<IllegalArgumentException> {
                    OrderId("1-20251225-0000001")
                }
            }
        }

        describe("OrderId toString") {
            it("should return the value") {
                val orderId = OrderId("10-20251225-0000001")

                orderId.toString() shouldBe "10-20251225-0000001"
            }
        }

        describe("OrderId equality") {
            it("should be equal for same value") {
                val id1 = OrderId("10-20251225-0000001")
                val id2 = OrderId("10-20251225-0000001")

                (id1 == id2) shouldBe true
            }

            it("should not be equal for different values") {
                val id1 = OrderId("10-20251225-0000001")
                val id2 = OrderId("10-20251225-0000002")

                (id1 == id2) shouldBe false
            }
        }
    })
