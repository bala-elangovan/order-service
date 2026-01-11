package io.github.balaelangovan.orders.domain.aggregate

import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal
import java.time.LocalDate

class OrderLineTest :
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

        describe("OrderLine creation with factory method") {
            it("should create order line with all required fields") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = "A great product",
                    quantity = 2,
                    unitPrice = Money.of(29.99, "USD"),
                    taxRate = BigDecimal("0.08"),
                    discountAmount = Money.of(5.00, "USD"),
                    fulfillmentType = FulfillmentType.STH,
                    shippingAddress = testAddress,
                )

                orderLine.lineNumber shouldBe 1
                orderLine.itemId shouldBe ItemId(1234567890L)
                orderLine.itemName shouldBe "Test Product"
                orderLine.itemDescription shouldBe "A great product"
                orderLine.quantity shouldBe 2
                orderLine.unitPrice shouldBe Money.of(29.99, "USD")
                orderLine.taxRate shouldBe BigDecimal("0.08")
                orderLine.discountAmount shouldBe Money.of(5.00, "USD")
                orderLine.fulfillmentType shouldBe FulfillmentType.STH
                orderLine.shippingAddress shouldBe testAddress
                orderLine.lineStatus.status shouldBe LineStatusType.CREATED
            }

            it("should create order line with optional fields as null") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.itemDescription shouldBe null
                orderLine.taxRate shouldBe null
                orderLine.discountAmount shouldBe null
                orderLine.shippingAddress shouldBe null
            }

            it("should create order line with estimated dates") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.STH,
                    shippingAddress = testAddress,
                    estimatedShipDate = LocalDate.now(),
                    estimatedDeliveryDate = LocalDate.now().plusDays(5),
                )

                orderLine.estimatedShipDate shouldBe LocalDate.now()
                orderLine.estimatedDeliveryDate shouldBe LocalDate.now().plusDays(5)
            }

            it("should create order line with promised dates") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.STH,
                    shippingAddress = testAddress,
                    promisedShipDate = LocalDate.now().plusDays(1),
                    promisedDeliveryDate = LocalDate.now().plusDays(3),
                )

                orderLine.promisedShipDate shouldBe LocalDate.now().plusDays(1)
                orderLine.promisedDeliveryDate shouldBe LocalDate.now().plusDays(3)
            }
        }

        describe("OrderLine validation") {
            it("should fail for zero quantity") {
                shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 0,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
            }

            it("should fail for negative quantity") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = -1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
                exception.message shouldContain "Quantity must be positive"
            }

            it("should fail for zero unit price") {
                shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(0, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
            }

            it("should fail for blank item name") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
                exception.message shouldContain "Item name cannot be blank"
            }

            it("should fail for negative tax rate") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = BigDecimal("-0.08"),
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
                exception.message shouldContain "Tax rate cannot be negative"
            }

            it("should fail for discount with different currency") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = Money.of(2.00, "EUR"),
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                    )
                }
                exception.message shouldContain "Discount currency must match unit price currency"
            }

            it("should fail for STH without shipping address") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.STH,
                        shippingAddress = null,
                    )
                }
                exception.message shouldContain "Shipping address is required"
            }

            it("should fail for STS without shipping address") {
                shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.STS,
                        shippingAddress = null,
                    )
                }
            }

            it("should allow BOPS without shipping address") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.shippingAddress shouldBe null
            }

            it("should fail when estimated delivery is before estimated ship date") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                        estimatedShipDate = LocalDate.now().plusDays(5),
                        estimatedDeliveryDate = LocalDate.now(),
                    )
                }
                exception.message shouldContain "Estimated delivery date cannot be before estimated ship date"
            }

            it("should fail when promised delivery is before promised ship date") {
                val exception = shouldThrow<IllegalArgumentException> {
                    OrderLine.create(
                        lineNumber = 1,
                        itemId = ItemId(1234567890L),
                        itemName = "Test Product",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = Money.of(10.00, "USD"),
                        taxRate = null,
                        discountAmount = null,
                        fulfillmentType = FulfillmentType.BOPS,
                        shippingAddress = null,
                        promisedShipDate = LocalDate.now().plusDays(5),
                        promisedDeliveryDate = LocalDate.now(),
                    )
                }
                exception.message shouldContain "Promised delivery date cannot be before promised ship date"
            }
        }

        describe("OrderLine calculated properties") {
            it("should calculate subtotal correctly") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 3,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.subtotal shouldBe Money.of(30.00, "USD")
            }

            it("should calculate tax amount correctly") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 2,
                    unitPrice = Money.of(50.00, "USD"),
                    taxRate = BigDecimal("0.10"),
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.taxAmount shouldBe Money.of(10.00, "USD") // 100 * 0.10
            }

            it("should calculate zero tax when no tax rate") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 2,
                    unitPrice = Money.of(50.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.taxAmount shouldBe Money.of(0, "USD")
            }

            it("should calculate total amount with discount") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 2,
                    unitPrice = Money.of(50.00, "USD"),
                    taxRate = BigDecimal("0.10"),
                    discountAmount = Money.of(10.00, "USD"),
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                // subtotal: 100, tax: 10, discount: 10 = 100
                orderLine.totalAmount shouldBe Money.of(100.00, "USD")
            }

            it("should calculate total amount without discount") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 2,
                    unitPrice = Money.of(50.00, "USD"),
                    taxRate = BigDecimal("0.10"),
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                // subtotal: 100, tax: 10, no discount = 110
                orderLine.totalAmount shouldBe Money.of(110.00, "USD")
            }
        }

        describe("OrderLine updateStatus") {
            it("should update status with notes") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                val updated = orderLine.updateStatus(LineStatusType.ALLOCATED, "Inventory reserved")

                updated.lineStatus.status shouldBe LineStatusType.ALLOCATED
                updated.lineStatus.notes shouldBe "Inventory reserved"
            }

            it("should get current status") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.getCurrentStatus() shouldBe LineStatusType.CREATED
            }
        }

        describe("OrderLine updatePromisedDates") {
            it("should update promised dates") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                val shipDate = LocalDate.now().plusDays(2)
                val deliveryDate = LocalDate.now().plusDays(5)
                val updated = orderLine.updatePromisedDates(shipDate, deliveryDate)

                updated.promisedShipDate shouldBe shipDate
                updated.promisedDeliveryDate shouldBe deliveryDate
            }
        }

        describe("OrderLine status checks") {
            it("should detect delivered line") {
                val orderLine = OrderLine(
                    id = LineItemId.generate(),
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                    lineStatus = LineStatus(1, LineStatusType.DELIVERED),
                    estimatedShipDate = null,
                    estimatedDeliveryDate = null,
                    promisedShipDate = null,
                    promisedDeliveryDate = null,
                )

                orderLine.isDelivered() shouldBe true
            }

            it("should detect shipped line") {
                val orderLine = OrderLine(
                    id = LineItemId.generate(),
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                    lineStatus = LineStatus(1, LineStatusType.SHIPPED),
                    estimatedShipDate = null,
                    estimatedDeliveryDate = null,
                    promisedShipDate = null,
                    promisedDeliveryDate = null,
                )

                orderLine.isShipped() shouldBe true
            }

            it("should detect shipped and invoiced as shipped") {
                val orderLine = OrderLine(
                    id = LineItemId.generate(),
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                    lineStatus = LineStatus(1, LineStatusType.SHIPPED_AND_INVOICED),
                    estimatedShipDate = null,
                    estimatedDeliveryDate = null,
                    promisedShipDate = null,
                    promisedDeliveryDate = null,
                )

                orderLine.isShipped() shouldBe true
            }

            it("should detect delivered as shipped") {
                val orderLine = OrderLine(
                    id = LineItemId.generate(),
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                    lineStatus = LineStatus(1, LineStatusType.DELIVERED),
                    estimatedShipDate = null,
                    estimatedDeliveryDate = null,
                    promisedShipDate = null,
                    promisedDeliveryDate = null,
                )

                orderLine.isShipped() shouldBe true
            }

            it("should not detect created as shipped") {
                val orderLine = OrderLine.create(
                    lineNumber = 1,
                    itemId = ItemId(1234567890L),
                    itemName = "Test Product",
                    itemDescription = null,
                    quantity = 1,
                    unitPrice = Money.of(10.00, "USD"),
                    taxRate = null,
                    discountAmount = null,
                    fulfillmentType = FulfillmentType.BOPS,
                    shippingAddress = null,
                )

                orderLine.isShipped() shouldBe false
                orderLine.isDelivered() shouldBe false
            }
        }
    })
