package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class OrderStatusTest :
    DescribeSpec({

        describe("OrderStatus transitions from CREATED") {
            it("should allow transition to IN_RELEASE") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.IN_RELEASE) shouldBe true
            }

            it("should allow transition to RELEASED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.RELEASED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED) shouldBe true
            }

            it("should not allow transition to SHIPPED directly") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.SHIPPED) shouldBe false
            }

            it("should not allow transition to IN_SHIPMENT directly") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.IN_SHIPMENT) shouldBe false
            }

            it("should not allow transition to DELIVERED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            }

            it("should not allow transition to CREATED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.CREATED) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.CREATED.allowedTransitions() shouldBe setOf(
                    OrderStatus.IN_RELEASE,
                    OrderStatus.RELEASED,
                    OrderStatus.CANCELLED,
                )
            }
        }

        describe("OrderStatus transitions from IN_RELEASE") {
            it("should allow transition to RELEASED") {
                OrderStatus.IN_RELEASE.canTransitionTo(OrderStatus.RELEASED) shouldBe true
            }

            it("should allow transition to IN_SHIPMENT") {
                OrderStatus.IN_RELEASE.canTransitionTo(OrderStatus.IN_SHIPMENT) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                OrderStatus.IN_RELEASE.canTransitionTo(OrderStatus.CANCELLED) shouldBe true
            }

            it("should not allow transition to SHIPPED") {
                OrderStatus.IN_RELEASE.canTransitionTo(OrderStatus.SHIPPED) shouldBe false
            }

            it("should not allow transition to DELIVERED") {
                OrderStatus.IN_RELEASE.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.IN_RELEASE.allowedTransitions() shouldBe setOf(
                    OrderStatus.RELEASED,
                    OrderStatus.IN_SHIPMENT,
                    OrderStatus.CANCELLED,
                )
            }
        }

        describe("OrderStatus transitions from RELEASED") {
            it("should allow transition to IN_SHIPMENT") {
                OrderStatus.RELEASED.canTransitionTo(OrderStatus.IN_SHIPMENT) shouldBe true
            }

            it("should allow transition to SHIPPED") {
                OrderStatus.RELEASED.canTransitionTo(OrderStatus.SHIPPED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                OrderStatus.RELEASED.canTransitionTo(OrderStatus.CANCELLED) shouldBe true
            }

            it("should not allow transition to DELIVERED") {
                OrderStatus.RELEASED.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            }

            it("should not allow transition to IN_RELEASE") {
                OrderStatus.RELEASED.canTransitionTo(OrderStatus.IN_RELEASE) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.RELEASED.allowedTransitions() shouldBe setOf(
                    OrderStatus.IN_SHIPMENT,
                    OrderStatus.SHIPPED,
                    OrderStatus.CANCELLED,
                )
            }
        }

        describe("OrderStatus transitions from IN_SHIPMENT") {
            it("should allow transition to SHIPPED") {
                OrderStatus.IN_SHIPMENT.canTransitionTo(OrderStatus.SHIPPED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                OrderStatus.IN_SHIPMENT.canTransitionTo(OrderStatus.CANCELLED) shouldBe true
            }

            it("should not allow transition to DELIVERED") {
                OrderStatus.IN_SHIPMENT.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            }

            it("should not allow transition to RELEASED") {
                OrderStatus.IN_SHIPMENT.canTransitionTo(OrderStatus.RELEASED) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.IN_SHIPMENT.allowedTransitions() shouldBe setOf(
                    OrderStatus.SHIPPED,
                    OrderStatus.CANCELLED,
                )
            }
        }

        describe("OrderStatus transitions from SHIPPED") {
            it("should allow transition to DELIVERED") {
                OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED) shouldBe true
            }

            it("should not allow transition to CANCELLED") {
                OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED) shouldBe false
            }

            it("should not allow transition to CREATED") {
                OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CREATED) shouldBe false
            }

            it("should not allow transition to SHIPPED") {
                OrderStatus.SHIPPED.canTransitionTo(OrderStatus.SHIPPED) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.SHIPPED.allowedTransitions() shouldBe setOf(OrderStatus.DELIVERED)
            }
        }

        describe("OrderStatus transitions from DELIVERED (terminal)") {
            it("should not allow any transitions") {
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CREATED) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.IN_RELEASE) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.RELEASED) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.IN_SHIPMENT) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.SHIPPED) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
                OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED) shouldBe false
            }

            it("should return empty allowed transitions") {
                OrderStatus.DELIVERED.allowedTransitions() shouldBe emptySet()
            }
        }

        describe("OrderStatus transitions from CANCELLED (terminal)") {
            it("should not allow any transitions") {
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CREATED) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.IN_RELEASE) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.RELEASED) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.IN_SHIPMENT) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.SHIPPED) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
                OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CANCELLED) shouldBe false
            }

            it("should return empty allowed transitions") {
                OrderStatus.CANCELLED.allowedTransitions() shouldBe emptySet()
            }
        }

        describe("OrderStatus isTerminal") {
            it("should identify DELIVERED as terminal") {
                OrderStatus.DELIVERED.isTerminal() shouldBe true
            }

            it("should identify CANCELLED as terminal") {
                OrderStatus.CANCELLED.isTerminal() shouldBe true
            }

            it("should identify CREATED as non-terminal") {
                OrderStatus.CREATED.isTerminal() shouldBe false
            }

            it("should identify IN_RELEASE as non-terminal") {
                OrderStatus.IN_RELEASE.isTerminal() shouldBe false
            }

            it("should identify RELEASED as non-terminal") {
                OrderStatus.RELEASED.isTerminal() shouldBe false
            }

            it("should identify IN_SHIPMENT as non-terminal") {
                OrderStatus.IN_SHIPMENT.isTerminal() shouldBe false
            }

            it("should identify SHIPPED as non-terminal") {
                OrderStatus.SHIPPED.isTerminal() shouldBe false
            }
        }

        describe("OrderStatus isPartial") {
            it("should identify IN_RELEASE as partial") {
                OrderStatus.IN_RELEASE.isPartial() shouldBe true
            }

            it("should identify IN_SHIPMENT as partial") {
                OrderStatus.IN_SHIPMENT.isPartial() shouldBe true
            }

            it("should identify CREATED as not partial") {
                OrderStatus.CREATED.isPartial() shouldBe false
            }

            it("should identify RELEASED as not partial") {
                OrderStatus.RELEASED.isPartial() shouldBe false
            }

            it("should identify SHIPPED as not partial") {
                OrderStatus.SHIPPED.isPartial() shouldBe false
            }

            it("should identify DELIVERED as not partial") {
                OrderStatus.DELIVERED.isPartial() shouldBe false
            }

            it("should identify CANCELLED as not partial") {
                OrderStatus.CANCELLED.isPartial() shouldBe false
            }
        }

        describe("OrderStatus allowsLineModification") {
            it("should allow line modification in CREATED") {
                OrderStatus.CREATED.allowsLineModification() shouldBe true
            }

            it("should not allow line modification in IN_RELEASE") {
                OrderStatus.IN_RELEASE.allowsLineModification() shouldBe false
            }

            it("should not allow line modification in RELEASED") {
                OrderStatus.RELEASED.allowsLineModification() shouldBe false
            }

            it("should not allow line modification in IN_SHIPMENT") {
                OrderStatus.IN_SHIPMENT.allowsLineModification() shouldBe false
            }

            it("should not allow line modification in SHIPPED") {
                OrderStatus.SHIPPED.allowsLineModification() shouldBe false
            }

            it("should not allow line modification in DELIVERED") {
                OrderStatus.DELIVERED.allowsLineModification() shouldBe false
            }

            it("should not allow line modification in CANCELLED") {
                OrderStatus.CANCELLED.allowsLineModification() shouldBe false
            }
        }
    })
