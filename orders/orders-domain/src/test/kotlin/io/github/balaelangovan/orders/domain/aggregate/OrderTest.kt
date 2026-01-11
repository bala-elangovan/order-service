package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.exception.InvalidStateTransitionException
import io.github.balaelangovan.orders.domain.exception.ValidationException
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.Channel
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus
import io.github.balaelangovan.orders.domain.valueobject.OrderType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.util.UUID

class OrderTest :
    DescribeSpec({

        val testAddress = Address.of(
            fullName = "John Doe",
            addressLine1 = "123 Main St",
            addressLine2 = null,
            city = "New York",
            stateProvince = "NY",
            postalCode = "10001",
            country = "USA",
        )

        fun createTestOrderLine(lineNumber: Int = 1, quantity: Int = 2, unitPrice: Double = 29.99): OrderLine =
            OrderLine.create(
                lineNumber = lineNumber,
                itemId = ItemId(1234567890L),
                itemName = "Test Product",
                itemDescription = "A great product",
                quantity = quantity,
                unitPrice = Money.of(unitPrice, "USD"),
                taxRate = BigDecimal("0.08"),
                discountAmount = null,
                fulfillmentType = FulfillmentType.STH,
                shippingAddress = testAddress,
            )

        describe("Order creation with factory method") {
            it("should create order with CREATED status") {
                val orderId = OrderId("10-20251225-0000001")
                val customerId = CustomerId("CUST-12345")
                val lines = listOf(createTestOrderLine())

                val order = Order.create(
                    orderId = orderId,
                    customerId = customerId,
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                order.id shouldBe orderId
                order.customerId shouldBe customerId
                order.orderType shouldBe OrderType.STANDARD
                order.channel shouldBe Channel.WEB
                order.status shouldBe OrderStatus.CREATED
                order.billingAddress shouldBe testAddress
                order.lines.size shouldBe 1
                order.notes shouldBe null
                order.orderKey shouldBe null
            }

            it("should create order with notes") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                    notes = "Please ship quickly",
                )

                order.notes shouldBe "Please ship quickly"
            }

            it("should create order with external order ID") {
                val externalId = UUID.randomUUID()
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                    externalOrderId = externalId,
                )

                order.externalOrderId shouldBe externalId
            }

            it("should create guest order") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("GUEST-12345"),
                    orderType = OrderType.GUEST,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.orderType shouldBe OrderType.GUEST
                order.isGuestOrder() shouldBe true
            }

            it("should create return order") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.RETURN,
                    channel = Channel.API,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.orderType shouldBe OrderType.RETURN
                order.isReturnOrder() shouldBe true
            }
        }

        describe("Order validation") {
            it("should fail for empty lines list") {
                val exception = shouldThrow<IllegalArgumentException> {
                    Order.create(
                        orderId = OrderId("10-20251225-0000001"),
                        customerId = CustomerId("CUST-12345"),
                        orderType = OrderType.STANDARD,
                        channel = Channel.WEB,
                        lines = emptyList(),
                        billingAddress = testAddress,
                    )
                }
                exception.message shouldContain "Order must have at least one line item"
            }

            it("should fail for mixed currencies") {
                val line1 = createTestOrderLine(lineNumber = 1)
                val line2 = OrderLine.create(
                    lineNumber = 2,
                    itemId = ItemId(1234567891L),
                    itemName = "Euro Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "EUR"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                val exception = shouldThrow<IllegalArgumentException> {
                    Order.create(
                        orderId = OrderId("10-20251225-0000001"),
                        customerId = CustomerId("CUST-12345"),
                        orderType = OrderType.STANDARD,
                        channel = Channel.WEB,
                        lines = listOf(line1, line2),
                        billingAddress = testAddress,
                    )
                }
                exception.message shouldContain "different currencies"
            }
        }

        describe("Order calculated properties") {
            it("should calculate subtotal correctly") {
                val lines = listOf(
                    createTestOrderLine(lineNumber = 1, quantity = 2, unitPrice = 10.00),
                    createTestOrderLine(lineNumber = 2, quantity = 3, unitPrice = 20.00),
                )

                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                // (2 * 10) + (3 * 20) = 20 + 60 = 80
                order.subtotal shouldBe Money.of(80.00, "USD")
            }

            it("should calculate tax amount correctly") {
                val lines = listOf(
                    createTestOrderLine(lineNumber = 1, quantity = 1, unitPrice = 100.00),
                )

                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                // 100 * 0.08 = 8.00
                order.taxAmount shouldBe Money.of(8.00, "USD")
            }

            it("should calculate total amount correctly") {
                val lines = listOf(
                    createTestOrderLine(lineNumber = 1, quantity = 1, unitPrice = 100.00),
                )

                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                // subtotal: 100 + tax: 8 - discount: 0 = 108
                order.totalAmount shouldBe Money.of(108.00, "USD")
            }

            it("should get currency from first line") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.currency shouldBe "USD"
            }

            it("should count lines correctly") {
                val lines = listOf(
                    createTestOrderLine(lineNumber = 1),
                    createTestOrderLine(lineNumber = 2),
                    createTestOrderLine(lineNumber = 3),
                )

                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                order.lineCount() shouldBe 3
            }

            it("should calculate total quantity correctly") {
                val lines = listOf(
                    createTestOrderLine(lineNumber = 1, quantity = 2),
                    createTestOrderLine(lineNumber = 2, quantity = 5),
                )

                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                order.totalQuantity() shouldBe 7
            }
        }

        describe("Order line management") {
            it("should add line to order") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(lineNumber = 1)),
                    billingAddress = testAddress,
                )

                val newLine = createTestOrderLine(lineNumber = 2)
                val updatedOrder = order.addLine(newLine)

                updatedOrder.lines.size shouldBe 2
                updatedOrder.updatedAt shouldNotBe order.updatedAt
            }

            it("should fail to add line with different currency") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(lineNumber = 1)),
                    billingAddress = testAddress,
                )

                val eurLine = OrderLine.create(
                    lineNumber = 2,
                    itemId = ItemId(1234567891L),
                    itemName = "Euro Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "EUR"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                val exception = shouldThrow<ValidationException> {
                    order.addLine(eurLine)
                }
                exception.message shouldContain "does not match order currency"
            }

            it("should fail to add line to non-created order") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(lineNumber = 1)),
                    billingAddress = testAddress,
                )
                order = order.release()

                val exception = shouldThrow<ValidationException> {
                    order.addLine(createTestOrderLine(lineNumber = 2))
                }
                exception.message shouldContain "Cannot add line items to non-created order"
            }

            it("should remove line from order") {
                val line1 = createTestOrderLine(lineNumber = 1)
                val line2 = createTestOrderLine(lineNumber = 2)
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(line1, line2),
                    billingAddress = testAddress,
                )

                val updatedOrder = order.removeLine(line1.id)

                updatedOrder.lines.size shouldBe 1
            }

            it("should fail to remove last line") {
                val line = createTestOrderLine()
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(line),
                    billingAddress = testAddress,
                )

                val exception = shouldThrow<ValidationException> {
                    order.removeLine(line.id)
                }
                exception.message shouldContain "Cannot remove last line item"
            }

            it("should fail to remove line from non-created order") {
                val line1 = createTestOrderLine(lineNumber = 1)
                val line2 = createTestOrderLine(lineNumber = 2)
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(line1, line2),
                    billingAddress = testAddress,
                )
                order = order.release()

                shouldThrow<ValidationException> {
                    order.removeLine(line1.id)
                }
            }

            it("should find line by id") {
                val line = createTestOrderLine()
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(line),
                    billingAddress = testAddress,
                )

                order.getLine(line.id) shouldBe line
            }

            it("should return null for unknown line id") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.getLine(io.github.balaelangovan.orders.domain.valueobject.LineItemId.generate()) shouldBe null
            }

            it("should check if order contains item") {
                val itemId = ItemId(1234567890L)
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.containsItem(itemId) shouldBe true
                order.containsItem(ItemId(9999999999L)) shouldBe false
            }
        }

        describe("Order status transitions") {
            it("should transition to IN_RELEASE from CREATED") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                val inReleaseOrder = order.inRelease()

                inReleaseOrder.status shouldBe OrderStatus.IN_RELEASE
            }

            it("should transition to RELEASED from CREATED") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                val releasedOrder = order.release()

                releasedOrder.status shouldBe OrderStatus.RELEASED
            }

            it("should transition to RELEASED from IN_RELEASE") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).inRelease()

                val releasedOrder = order.release()

                releasedOrder.status shouldBe OrderStatus.RELEASED
            }

            it("should transition to IN_SHIPMENT from RELEASED") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).release()

                val inShipmentOrder = order.inShipment()

                inShipmentOrder.status shouldBe OrderStatus.IN_SHIPMENT
            }

            it("should ship order from RELEASED status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).release()

                val shippedOrder = order.ship()

                shippedOrder.status shouldBe OrderStatus.SHIPPED
            }

            it("should ship order from IN_SHIPMENT status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).release().inShipment()

                val shippedOrder = order.ship()

                shippedOrder.status shouldBe OrderStatus.SHIPPED
            }

            it("should deliver order from SHIPPED status") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release().ship()

                val deliveredOrder = order.deliver()

                deliveredOrder.status shouldBe OrderStatus.DELIVERED
            }

            it("should cancel order from CREATED status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                val cancelledOrder = order.cancel()

                cancelledOrder.status shouldBe OrderStatus.CANCELLED
            }

            it("should cancel order from IN_RELEASE status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).inRelease()

                val cancelledOrder = order.cancel()

                cancelledOrder.status shouldBe OrderStatus.CANCELLED
            }

            it("should cancel order from IN_SHIPMENT status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).release().inShipment()

                val cancelledOrder = order.cancel()

                cancelledOrder.status shouldBe OrderStatus.CANCELLED
            }

            it("should fail to ship directly from CREATED status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                shouldThrow<InvalidStateTransitionException> {
                    order.ship()
                }
            }

            it("should fail to ship from DELIVERED status") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release().ship().deliver()

                val exception = shouldThrow<InvalidStateTransitionException> {
                    order.ship()
                }
                exception.message shouldContain "Cannot transition from DELIVERED to SHIPPED"
            }

            it("should fail to cancel from DELIVERED status") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release().ship().deliver()

                shouldThrow<InvalidStateTransitionException> {
                    order.cancel()
                }
            }

            it("should fail to deliver from CREATED status") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                shouldThrow<InvalidStateTransitionException> {
                    order.deliver()
                }
            }

            it("should identify partial fulfillment state") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).inRelease()

                order.isPartialFulfillment() shouldBe true
            }

            it("should identify non-partial fulfillment state") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                ).release()

                order.isPartialFulfillment() shouldBe false
            }

            it("should check if order can be cancelled") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.canCancel() shouldBe true
                order.release().ship().canCancel() shouldBe false
            }
        }

        describe("Order update operations") {
            it("should update notes") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                val updatedOrder = order.updateNotes("New notes")

                updatedOrder.notes shouldBe "New notes"
            }

            it("should clear notes") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                    notes = "Initial notes",
                )

                val updatedOrder = order.updateNotes(null)

                updatedOrder.notes shouldBe null
            }

            it("should update billing address") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                val newAddress = Address.of(
                    fullName = "Jane Doe",
                    addressLine1 = "456 Oak Ave",
                    addressLine2 = null,
                    city = "Los Angeles",
                    stateProvince = "CA",
                    postalCode = "90001",
                    country = "USA",
                )

                val updatedOrder = order.updateBillingAddress(newAddress)

                updatedOrder.billingAddress shouldBe newAddress
            }

            it("should fail to update billing address on terminal order") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release().ship().deliver()

                val exception = shouldThrow<ValidationException> {
                    order.updateBillingAddress(testAddress)
                }
                exception.message shouldContain "Cannot update billing address on terminal order"
            }

            it("should update line status") {
                val line = createTestOrderLine()
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(line),
                    billingAddress = testAddress,
                )

                val updatedOrder = order.updateLineStatus(line.id, LineStatusType.ALLOCATED, "Inventory allocated")

                updatedOrder.lines.first().lineStatus.status shouldBe LineStatusType.ALLOCATED
            }
        }

        describe("Order status checks") {
            it("should identify modifiable order") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )

                order.isModifiable() shouldBe true
            }

            it("should identify non-modifiable order") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release()

                order.isModifiable() shouldBe false
            }

            it("should identify terminal order") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.release().ship().deliver()

                order.isTerminal() shouldBe true
            }

            it("should identify cancelled as terminal") {
                var order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine()),
                    billingAddress = testAddress,
                )
                order = order.cancel()

                order.isTerminal() shouldBe true
            }
        }

        describe("Order shipping calculations") {
            it("should have free shipping for orders over 100") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 5, unitPrice = 30.00)), // $150 + tax
                    billingAddress = testAddress,
                )

                order.calculateShippingCost() shouldBe Money.of(0, "USD")
            }

            it("should have $5 shipping for 5+ items under $100") {
                val lines = (1..6).map { createTestOrderLine(lineNumber = it, quantity = 1, unitPrice = 5.00) }
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = lines,
                    billingAddress = testAddress,
                )

                order.calculateShippingCost() shouldBe Money.of(5, "USD")
            }

            it("should have $10 shipping for less than 5 items under $100") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 1, unitPrice = 10.00)),
                    billingAddress = testAddress,
                )

                order.calculateShippingCost() shouldBe Money.of(10, "USD")
            }
        }

        describe("Order volume discount calculations") {
            it("should have 10% discount for orders over 200") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 10, unitPrice = 20.00)), // $200 subtotal
                    billingAddress = testAddress,
                )

                // Total = 200 + 16 (tax) = 216, 10% of 216 = 21.60
                order.calculateVolumeDiscount() shouldBe Money.of(21.60, "USD")
            }

            it("should have 5% discount for orders over 100") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 4, unitPrice = 30.00)), // $120 + tax
                    billingAddress = testAddress,
                )

                // Total = 120 + 9.60 (tax) = 129.60, 5% of 129.60 = 6.48
                order.calculateVolumeDiscount() shouldBe Money.of(6.48, "USD")
            }

            it("should have no discount for orders under 100") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 1, unitPrice = 50.00)), // $50 + tax
                    billingAddress = testAddress,
                )

                order.calculateVolumeDiscount() shouldBe Money.of(0, "USD")
            }
        }

        describe("Order final amount calculation") {
            it("should calculate final amount with discount and shipping") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 1, unitPrice = 50.00)),
                    billingAddress = testAddress,
                )

                // Total: 54.00, no volume discount, shipping: $10
                val finalAmount = order.calculateFinalAmount()
                finalAmount shouldBe Money.of(64.00, "USD")
            }
        }

        describe("Order inventory fulfillment check") {
            it("should check if order can be fulfilled from inventory") {
                val itemId = ItemId(1234567890L)
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 5)),
                    billingAddress = testAddress,
                )

                val inventory = mapOf(itemId to 10)
                order.canFulfillFrom(inventory) shouldBe true
            }

            it("should detect insufficient inventory") {
                val itemId = ItemId(1234567890L)
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 10)),
                    billingAddress = testAddress,
                )

                val inventory = mapOf(itemId to 5)
                order.canFulfillFrom(inventory) shouldBe false
            }

            it("should detect missing inventory for item") {
                val order = Order.create(
                    orderId = OrderId("10-20251225-0000001"),
                    customerId = CustomerId("CUST-12345"),
                    orderType = OrderType.STANDARD,
                    channel = Channel.WEB,
                    lines = listOf(createTestOrderLine(quantity = 1)),
                    billingAddress = testAddress,
                )

                val inventory = emptyMap<ItemId, Int>()
                order.canFulfillFrom(inventory) shouldBe false
            }
        }
    })
