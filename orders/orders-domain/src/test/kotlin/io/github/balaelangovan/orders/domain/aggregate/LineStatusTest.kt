package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.exception.InvalidStateTransitionException
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class LineStatusTest :
    DescribeSpec({

        describe("LineStatus creation") {
            it("should create line status with create factory method") {
                val lineStatus = LineStatus.create(5)

                lineStatus.quantity shouldBe 5
                lineStatus.status shouldBe LineStatusType.CREATED
                lineStatus.statusCode shouldBe "100"
                lineStatus.statusDescription shouldBe "Order line created"
                lineStatus.notes shouldBe null
            }

            it("should create line status with direct constructor") {
                val lineStatus = LineStatus(
                    quantity = 3,
                    status = LineStatusType.ALLOCATED,
                    notes = "Inventory reserved",
                )

                lineStatus.quantity shouldBe 3
                lineStatus.status shouldBe LineStatusType.ALLOCATED
                lineStatus.statusCode shouldBe "200"
                lineStatus.notes shouldBe "Inventory reserved"
            }
        }

        describe("LineStatus validation") {
            it("should fail for zero quantity") {
                shouldThrow<IllegalArgumentException> {
                    LineStatus.create(0)
                }
            }

            it("should fail for negative quantity") {
                val exception = shouldThrow<IllegalArgumentException> {
                    LineStatus.create(-1)
                }
                exception.message shouldContain "Quantity must be positive"
            }
        }

        describe("LineStatus updateStatus") {
            it("should transition from CREATED to ALLOCATED") {
                val lineStatus = LineStatus.create(2)

                val updated = lineStatus.updateStatus(LineStatusType.ALLOCATED, "Inventory reserved")

                updated.status shouldBe LineStatusType.ALLOCATED
                updated.statusCode shouldBe "200"
                updated.statusDescription shouldBe "Inventory allocated"
                updated.notes shouldBe "Inventory reserved"
                updated.quantity shouldBe 2
                updated.updatedAt shouldNotBe lineStatus.updatedAt
            }

            it("should transition from CREATED to CANCELLED") {
                val lineStatus = LineStatus.create(2)

                val updated = lineStatus.updateStatus(LineStatusType.CANCELLED)

                updated.status shouldBe LineStatusType.CANCELLED
                updated.notes shouldBe null
            }

            it("should transition from ALLOCATED to RELEASED") {
                val lineStatus = LineStatus(quantity = 2, status = LineStatusType.ALLOCATED)

                val updated = lineStatus.updateStatus(LineStatusType.RELEASED)

                updated.status shouldBe LineStatusType.RELEASED
            }

            it("should transition from RELEASED to SHIPPED") {
                val lineStatus = LineStatus(quantity = 2, status = LineStatusType.RELEASED)

                val updated = lineStatus.updateStatus(LineStatusType.SHIPPED)

                updated.status shouldBe LineStatusType.SHIPPED
            }

            it("should transition from SHIPPED to DELIVERED") {
                val lineStatus = LineStatus(quantity = 2, status = LineStatusType.SHIPPED)

                val updated = lineStatus.updateStatus(LineStatusType.DELIVERED)

                updated.status shouldBe LineStatusType.DELIVERED
            }

            it("should fail for invalid transition from CREATED to SHIPPED") {
                val lineStatus = LineStatus.create(2)

                val exception = shouldThrow<InvalidStateTransitionException> {
                    lineStatus.updateStatus(LineStatusType.SHIPPED)
                }
                exception.message shouldContain "Cannot transition line status from CREATED to SHIPPED"
            }

            it("should fail for transition from terminal state CANCELLED") {
                val lineStatus = LineStatus(quantity = 2, status = LineStatusType.CANCELLED)

                shouldThrow<InvalidStateTransitionException> {
                    lineStatus.updateStatus(LineStatusType.CREATED)
                }
            }

            it("should fail for transition from terminal state RETURN_COMPLETED") {
                val lineStatus = LineStatus(quantity = 2, status = LineStatusType.RETURN_COMPLETED)

                shouldThrow<InvalidStateTransitionException> {
                    lineStatus.updateStatus(LineStatusType.CANCELLED)
                }
            }
        }

        describe("LineStatus isTerminal") {
            it("should return true for DELIVERED") {
                val lineStatus = LineStatus(quantity = 1, status = LineStatusType.DELIVERED)

                lineStatus.isTerminal() shouldBe true
            }

            it("should return true for CANCELLED") {
                val lineStatus = LineStatus(quantity = 1, status = LineStatusType.CANCELLED)

                lineStatus.isTerminal() shouldBe true
            }

            it("should return true for RETURN_COMPLETED") {
                val lineStatus = LineStatus(quantity = 1, status = LineStatusType.RETURN_COMPLETED)

                lineStatus.isTerminal() shouldBe true
            }

            it("should return false for CREATED") {
                val lineStatus = LineStatus.create(1)

                lineStatus.isTerminal() shouldBe false
            }

            it("should return false for SHIPPED") {
                val lineStatus = LineStatus(quantity = 1, status = LineStatusType.SHIPPED)

                lineStatus.isTerminal() shouldBe false
            }
        }
    })
