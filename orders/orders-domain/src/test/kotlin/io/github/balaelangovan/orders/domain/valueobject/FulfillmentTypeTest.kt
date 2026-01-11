package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class FulfillmentTypeTest :
    DescribeSpec({

        describe("FulfillmentType properties") {
            it("should have correct codes") {
                FulfillmentType.STH.code shouldBe "STH"
                FulfillmentType.BOPS.code shouldBe "BOPS"
                FulfillmentType.STS.code shouldBe "STS"
            }

            it("should have descriptions") {
                FulfillmentType.STH.description shouldBe "Ship to Home - Standard delivery to customer address"
                FulfillmentType.BOPS.description shouldBe "Buy Online Pick in Store - Customer picks up at store"
                FulfillmentType.STS.description shouldBe "Ship to Store - Ship to store for customer pickup"
            }
        }

        describe("FulfillmentType fromCode") {
            it("should find fulfillment type by code") {
                FulfillmentType.fromCode("STH") shouldBe FulfillmentType.STH
                FulfillmentType.fromCode("BOPS") shouldBe FulfillmentType.BOPS
                FulfillmentType.fromCode("STS") shouldBe FulfillmentType.STS
            }

            it("should throw for unknown code") {
                val exception = shouldThrow<IllegalArgumentException> {
                    FulfillmentType.fromCode("UNKNOWN")
                }
                exception.message shouldContain "Unknown fulfillment type code"
            }
        }

        describe("FulfillmentType fromCodeOrNameOrDefault") {
            it("should find fulfillment type by code") {
                FulfillmentType.fromCodeOrNameOrDefault("STH") shouldBe FulfillmentType.STH
                FulfillmentType.fromCodeOrNameOrDefault("BOPS") shouldBe FulfillmentType.BOPS
                FulfillmentType.fromCodeOrNameOrDefault("STS") shouldBe FulfillmentType.STS
            }

            it("should find fulfillment type by name (same as code for this enum)") {
                FulfillmentType.fromCodeOrNameOrDefault("STH") shouldBe FulfillmentType.STH
            }

            it("should return default for null value") {
                FulfillmentType.fromCodeOrNameOrDefault(null) shouldBe FulfillmentType.STH
            }

            it("should return default for unknown value") {
                FulfillmentType.fromCodeOrNameOrDefault("UNKNOWN") shouldBe FulfillmentType.STH
            }

            it("should use custom default") {
                FulfillmentType.fromCodeOrNameOrDefault("UNKNOWN", FulfillmentType.BOPS) shouldBe FulfillmentType.BOPS
            }
        }
    })
