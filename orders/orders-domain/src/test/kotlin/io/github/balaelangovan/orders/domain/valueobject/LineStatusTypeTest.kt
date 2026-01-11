package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class LineStatusTypeTest :
    DescribeSpec({

        describe("LineStatusType properties") {
            it("should have correct codes") {
                LineStatusType.CREATED.code shouldBe "100"
                LineStatusType.ALLOCATED.code shouldBe "200"
                LineStatusType.RELEASED.code shouldBe "300"
                LineStatusType.SHIPPED.code shouldBe "400"
                LineStatusType.SHIPPED_AND_INVOICED.code shouldBe "410"
                LineStatusType.DELIVERED.code shouldBe "500"
                LineStatusType.RETURN_INITIATED.code shouldBe "600"
                LineStatusType.RETURN_COMPLETED.code shouldBe "700"
                LineStatusType.CANCELLED.code shouldBe "900"
            }

            it("should have descriptions") {
                LineStatusType.CREATED.description shouldBe "Order line created"
                LineStatusType.DELIVERED.description shouldBe "Delivered to customer"
            }
        }

        describe("LineStatusType fromCode") {
            it("should find status by code") {
                LineStatusType.fromCode("100") shouldBe LineStatusType.CREATED
                LineStatusType.fromCode("400") shouldBe LineStatusType.SHIPPED
                LineStatusType.fromCode("900") shouldBe LineStatusType.CANCELLED
            }

            it("should throw for unknown code") {
                val exception = shouldThrow<IllegalArgumentException> {
                    LineStatusType.fromCode("999")
                }
                exception.message shouldContain "Unknown status code"
            }
        }

        describe("LineStatusType transitions from CREATED") {
            it("should allow transition to ALLOCATED") {
                LineStatusType.CREATED.canTransitionTo(LineStatusType.ALLOCATED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                LineStatusType.CREATED.canTransitionTo(LineStatusType.CANCELLED) shouldBe true
            }

            it("should not allow transition to SHIPPED") {
                LineStatusType.CREATED.canTransitionTo(LineStatusType.SHIPPED) shouldBe false
            }
        }

        describe("LineStatusType transitions from ALLOCATED") {
            it("should allow transition to RELEASED") {
                LineStatusType.ALLOCATED.canTransitionTo(LineStatusType.RELEASED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                LineStatusType.ALLOCATED.canTransitionTo(LineStatusType.CANCELLED) shouldBe true
            }

            it("should not allow transition to SHIPPED") {
                LineStatusType.ALLOCATED.canTransitionTo(LineStatusType.SHIPPED) shouldBe false
            }
        }

        describe("LineStatusType transitions from RELEASED") {
            it("should allow transition to SHIPPED") {
                LineStatusType.RELEASED.canTransitionTo(LineStatusType.SHIPPED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                LineStatusType.RELEASED.canTransitionTo(LineStatusType.CANCELLED) shouldBe true
            }

            it("should not allow transition to DELIVERED") {
                LineStatusType.RELEASED.canTransitionTo(LineStatusType.DELIVERED) shouldBe false
            }
        }

        describe("LineStatusType transitions from SHIPPED") {
            it("should allow transition to SHIPPED_AND_INVOICED") {
                LineStatusType.SHIPPED.canTransitionTo(LineStatusType.SHIPPED_AND_INVOICED) shouldBe true
            }

            it("should allow transition to DELIVERED") {
                LineStatusType.SHIPPED.canTransitionTo(LineStatusType.DELIVERED) shouldBe true
            }

            it("should allow transition to RETURN_INITIATED") {
                LineStatusType.SHIPPED.canTransitionTo(LineStatusType.RETURN_INITIATED) shouldBe true
            }

            it("should not allow transition to CANCELLED") {
                LineStatusType.SHIPPED.canTransitionTo(LineStatusType.CANCELLED) shouldBe false
            }
        }

        describe("LineStatusType transitions from SHIPPED_AND_INVOICED") {
            it("should allow transition to DELIVERED") {
                LineStatusType.SHIPPED_AND_INVOICED.canTransitionTo(LineStatusType.DELIVERED) shouldBe true
            }

            it("should allow transition to RETURN_INITIATED") {
                LineStatusType.SHIPPED_AND_INVOICED.canTransitionTo(LineStatusType.RETURN_INITIATED) shouldBe true
            }

            it("should not allow transition to CANCELLED") {
                LineStatusType.SHIPPED_AND_INVOICED.canTransitionTo(LineStatusType.CANCELLED) shouldBe false
            }
        }

        describe("LineStatusType transitions from DELIVERED") {
            it("should allow transition to RETURN_INITIATED") {
                LineStatusType.DELIVERED.canTransitionTo(LineStatusType.RETURN_INITIATED) shouldBe true
            }

            it("should not allow transition to CANCELLED") {
                LineStatusType.DELIVERED.canTransitionTo(LineStatusType.CANCELLED) shouldBe false
            }
        }

        describe("LineStatusType transitions from RETURN_INITIATED") {
            it("should allow transition to RETURN_COMPLETED") {
                LineStatusType.RETURN_INITIATED.canTransitionTo(LineStatusType.RETURN_COMPLETED) shouldBe true
            }

            it("should not allow transition to CANCELLED") {
                LineStatusType.RETURN_INITIATED.canTransitionTo(LineStatusType.CANCELLED) shouldBe false
            }
        }

        describe("LineStatusType terminal states") {
            it("RETURN_COMPLETED should not allow any transitions") {
                LineStatusType.RETURN_COMPLETED.canTransitionTo(LineStatusType.CREATED) shouldBe false
                LineStatusType.RETURN_COMPLETED.canTransitionTo(LineStatusType.CANCELLED) shouldBe false
            }

            it("CANCELLED should not allow any transitions") {
                LineStatusType.CANCELLED.canTransitionTo(LineStatusType.CREATED) shouldBe false
                LineStatusType.CANCELLED.canTransitionTo(LineStatusType.RETURN_COMPLETED) shouldBe false
            }
        }

        describe("LineStatusType isTerminal") {
            it("should identify DELIVERED as terminal") {
                LineStatusType.DELIVERED.isTerminal() shouldBe true
            }

            it("should identify RETURN_COMPLETED as terminal") {
                LineStatusType.RETURN_COMPLETED.isTerminal() shouldBe true
            }

            it("should identify CANCELLED as terminal") {
                LineStatusType.CANCELLED.isTerminal() shouldBe true
            }

            it("should identify CREATED as non-terminal") {
                LineStatusType.CREATED.isTerminal() shouldBe false
            }

            it("should identify SHIPPED as non-terminal") {
                LineStatusType.SHIPPED.isTerminal() shouldBe false
            }
        }
    })
