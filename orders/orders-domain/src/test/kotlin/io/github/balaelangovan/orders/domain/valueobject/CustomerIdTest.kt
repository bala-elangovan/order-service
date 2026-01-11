package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class CustomerIdTest :
    DescribeSpec({

        describe("CustomerId creation") {
            it("should create valid customer ID") {
                val customerId = CustomerId("CUST-12345")

                customerId.value shouldBe "CUST-12345"
            }

            it("should create customer ID using of() factory method") {
                val customerId = CustomerId.of("customer-abc-123")

                customerId.value shouldBe "customer-abc-123"
            }

            it("should accept numeric customer IDs") {
                val customerId = CustomerId("123456789")

                customerId.value shouldBe "123456789"
            }

            it("should accept UUID-style customer IDs") {
                val customerId = CustomerId("550e8400-e29b-41d4-a716-446655440000")

                customerId.value shouldBe "550e8400-e29b-41d4-a716-446655440000"
            }
        }

        describe("CustomerId validation") {
            it("should fail for empty string") {
                val exception = shouldThrow<IllegalArgumentException> {
                    CustomerId("")
                }
                exception.message shouldContain "cannot be blank"
            }

            it("should fail for blank string with spaces") {
                shouldThrow<IllegalArgumentException> {
                    CustomerId("   ")
                }
            }

            it("should fail for blank string with tabs") {
                shouldThrow<IllegalArgumentException> {
                    CustomerId("\t\t")
                }
            }
        }

        describe("CustomerId toString") {
            it("should return the value") {
                val customerId = CustomerId("CUST-12345")

                customerId.toString() shouldBe "CUST-12345"
            }
        }

        describe("CustomerId equality") {
            it("should be equal for same value") {
                val id1 = CustomerId("CUST-12345")
                val id2 = CustomerId("CUST-12345")

                (id1 == id2) shouldBe true
            }

            it("should not be equal for different values") {
                val id1 = CustomerId("CUST-12345")
                val id2 = CustomerId("CUST-67890")

                (id1 == id2) shouldBe false
            }
        }
    })
