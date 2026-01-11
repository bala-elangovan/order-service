package io.github.balaelangovan.orders.domain.valueobject

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class OrderStatusTest :
    DescribeSpec({

        describe("OrderStatus transitions from CREATED") {
            it("should allow transition to SHIPPED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.SHIPPED) shouldBe true
            }

            it("should allow transition to CANCELLED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED) shouldBe true
            }

            it("should not allow transition to DELIVERED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED) shouldBe false
            }

            it("should not allow transition to CREATED") {
                OrderStatus.CREATED.canTransitionTo(OrderStatus.CREATED) shouldBe false
            }

            it("should return correct allowed transitions") {
                OrderStatus.CREATED.allowedTransitions() shouldBe setOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
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

            it("should identify SHIPPED as non-terminal") {
                OrderStatus.SHIPPED.isTerminal() shouldBe false
            }
        }
    })
