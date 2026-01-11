package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID

class LineItemIdTest :
    DescribeSpec({

        describe("LineItemId creation") {
            it("should generate unique line item IDs") {
                val id1 = LineItemId.generate()
                val id2 = LineItemId.generate()

                id1 shouldNotBe id2
            }

            it("should create from UUID") {
                val uuid = UUID.randomUUID()
                val lineItemId = LineItemId.of(uuid)

                lineItemId.value shouldBe uuid
            }

            it("should create from string") {
                val uuidString = "550e8400-e29b-41d4-a716-446655440000"
                val lineItemId = LineItemId.of(uuidString)

                lineItemId.value shouldBe UUID.fromString(uuidString)
            }

            it("should create directly with UUID") {
                val uuid = UUID.randomUUID()
                val lineItemId = LineItemId(uuid)

                lineItemId.value shouldBe uuid
            }
        }

        describe("LineItemId validation") {
            it("should fail for invalid UUID string") {
                shouldThrow<IllegalArgumentException> {
                    LineItemId.of("not-a-valid-uuid")
                }
            }

            it("should fail for empty string") {
                shouldThrow<IllegalArgumentException> {
                    LineItemId.of("")
                }
            }
        }

        describe("LineItemId toString") {
            it("should return the UUID string representation") {
                val uuidString = "550e8400-e29b-41d4-a716-446655440000"
                val lineItemId = LineItemId.of(uuidString)

                lineItemId.toString() shouldBe uuidString
            }
        }

        describe("LineItemId equality") {
            it("should be equal for same UUID") {
                val uuid = UUID.randomUUID()
                val id1 = LineItemId(uuid)
                val id2 = LineItemId(uuid)

                (id1 == id2) shouldBe true
            }

            it("should not be equal for different UUIDs") {
                val id1 = LineItemId.generate()
                val id2 = LineItemId.generate()

                (id1 == id2) shouldBe false
            }
        }
    })
