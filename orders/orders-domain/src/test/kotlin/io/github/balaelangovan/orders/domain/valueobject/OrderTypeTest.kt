package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class OrderTypeTest :
    DescribeSpec({

        describe("OrderType properties") {
            it("should have correct codes") {
                OrderType.STANDARD.code shouldBe "STD"
                OrderType.GUEST.code shouldBe "GUEST"
                OrderType.RETURN.code shouldBe "RET"
                OrderType.EXCHANGE.code shouldBe "EXCH"
                OrderType.STORE.code shouldBe "STORE"
                OrderType.SUBSCRIPTION.code shouldBe "SUB"
            }

            it("should have descriptions") {
                OrderType.STANDARD.description shouldBe "Standard customer order"
                OrderType.GUEST.description shouldBe "Guest checkout order - no registered account"
                OrderType.RETURN.description shouldBe "Return order - customer returning items"
                OrderType.EXCHANGE.description shouldBe "Exchange order - swap for different item"
                OrderType.STORE.description shouldBe "In-store order - placed at physical store"
                OrderType.SUBSCRIPTION.description shouldBe "Subscription order - recurring order"
            }
        }

        describe("OrderType fromCode") {
            it("should find order type by code") {
                OrderType.fromCode("STD") shouldBe OrderType.STANDARD
                OrderType.fromCode("GUEST") shouldBe OrderType.GUEST
                OrderType.fromCode("RET") shouldBe OrderType.RETURN
                OrderType.fromCode("EXCH") shouldBe OrderType.EXCHANGE
                OrderType.fromCode("STORE") shouldBe OrderType.STORE
                OrderType.fromCode("SUB") shouldBe OrderType.SUBSCRIPTION
            }

            it("should throw for unknown code") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderType.fromCode("UNKNOWN")
                }
                exception.message shouldContain "Unknown order type code"
            }
        }

        describe("OrderType fromNameOrDefault") {
            it("should find order type by name") {
                OrderType.fromNameOrDefault("STANDARD") shouldBe OrderType.STANDARD
                OrderType.fromNameOrDefault("GUEST") shouldBe OrderType.GUEST
                OrderType.fromNameOrDefault("RETURN") shouldBe OrderType.RETURN
                OrderType.fromNameOrDefault("EXCHANGE") shouldBe OrderType.EXCHANGE
                OrderType.fromNameOrDefault("STORE") shouldBe OrderType.STORE
                OrderType.fromNameOrDefault("SUBSCRIPTION") shouldBe OrderType.SUBSCRIPTION
            }

            it("should return default for null name") {
                OrderType.fromNameOrDefault(null) shouldBe OrderType.STANDARD
            }

            it("should return default for unknown name") {
                OrderType.fromNameOrDefault("UNKNOWN") shouldBe OrderType.STANDARD
            }

            it("should use custom default") {
                OrderType.fromNameOrDefault("UNKNOWN", OrderType.GUEST) shouldBe OrderType.GUEST
            }

            it("should be case sensitive") {
                // lowercase doesn't match, returns default
                OrderType.fromNameOrDefault("standard") shouldBe OrderType.STANDARD
            }
        }
    })
