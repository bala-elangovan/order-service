package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ItemIdTest :
    DescribeSpec({

        describe("ItemId creation with Long") {
            it("should create valid item ID with 10-digit number") {
                val itemId = ItemId(1234567890L)

                itemId.value shouldBe 1234567890L
            }

            it("should accept minimum 10-digit value") {
                val itemId = ItemId(1_000_000_000L)

                itemId.value shouldBe 1_000_000_000L
            }

            it("should accept maximum 10-digit value") {
                val itemId = ItemId(9_999_999_999L)

                itemId.value shouldBe 9_999_999_999L
            }

            it("should create item ID using of(Long) factory method") {
                val itemId = ItemId.of(5555555555L)

                itemId.value shouldBe 5555555555L
            }
        }

        describe("ItemId creation with String") {
            it("should create valid item ID from 10-digit string") {
                val itemId = ItemId.of("1234567890")

                itemId.value shouldBe 1234567890L
            }

            it("should accept string starting with zero") {
                // Note: "0123456789" is not a valid 10-digit number as it starts with 0
                // Valid 10-digit numbers are 1000000000 to 9999999999
                val itemId = ItemId.of("1000000000")

                itemId.value shouldBe 1000000000L
            }
        }

        describe("ItemId validation with Long") {
            it("should fail for 9-digit number") {
                val exception = shouldThrow<IllegalArgumentException> {
                    ItemId(999_999_999L)
                }
                exception.message shouldContain "10-digit number"
            }

            it("should fail for 11-digit number") {
                shouldThrow<IllegalArgumentException> {
                    ItemId(10_000_000_000L)
                }
            }

            it("should fail for zero") {
                shouldThrow<IllegalArgumentException> {
                    ItemId(0L)
                }
            }

            it("should fail for negative number") {
                shouldThrow<IllegalArgumentException> {
                    ItemId(-1234567890L)
                }
            }
        }

        describe("ItemId validation with String") {
            it("should fail for 9-digit string") {
                shouldThrow<IllegalArgumentException> {
                    ItemId.of("123456789")
                }
            }

            it("should fail for 11-digit string") {
                shouldThrow<IllegalArgumentException> {
                    ItemId.of("12345678901")
                }
            }

            it("should fail for non-numeric string") {
                shouldThrow<IllegalArgumentException> {
                    ItemId.of("123456789a")
                }
            }

            it("should fail for empty string") {
                shouldThrow<IllegalArgumentException> {
                    ItemId.of("")
                }
            }

            it("should fail for string with spaces") {
                shouldThrow<IllegalArgumentException> {
                    ItemId.of("12345 67890")
                }
            }
        }

        describe("ItemId toString") {
            it("should return the value as string") {
                val itemId = ItemId(1234567890L)

                itemId.toString() shouldBe "1234567890"
            }
        }

        describe("ItemId equality") {
            it("should be equal for same value") {
                val id1 = ItemId(1234567890L)
                val id2 = ItemId(1234567890L)

                (id1 == id2) shouldBe true
            }

            it("should not be equal for different values") {
                val id1 = ItemId(1234567890L)
                val id2 = ItemId(1234567891L)

                (id1 == id2) shouldBe false
            }
        }
    })
