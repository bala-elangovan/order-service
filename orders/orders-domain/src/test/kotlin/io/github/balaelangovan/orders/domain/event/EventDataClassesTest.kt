package io.github.balaelangovan.orders.domain.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class EventDataClassesTest :
    DescribeSpec({

        describe("OrderCreatedEvent") {
            val address = OrderCreatedEvent.Address(
                fullName = "John Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4",
                city = "New York",
                stateProvince = "NY",
                postalCode = "10001",
                country = "USA",
                phoneNumber = "555-1234",
                email = "john@example.com",
            )

            val orderLine = OrderCreatedEvent.OrderLine(
                lineNumber = 1,
                itemId = 1234567890L,
                itemName = "Test Product",
                itemDescription = "A great product",
                quantity = 2,
                unitPrice = BigDecimal("29.99"),
                currency = "USD",
                taxRate = BigDecimal("0.08"),
                discountAmount = BigDecimal("5.00"),
                fulfillmentType = "STH",
                shippingAddress = address,
                estimatedShipDate = LocalDate.of(2025, 1, 15),
                estimatedDeliveryDate = LocalDate.of(2025, 1, 20),
            )

            val statusChange = OrderCreatedEvent.StatusChange(
                status = "CREATED",
                timestamp = LocalDateTime.of(2025, 1, 10, 10, 0, 0),
                notes = "Order created",
            )

            val statusTracking = OrderCreatedEvent.StatusTracking(
                currentStatus = "CREATED",
                statusHistory = listOf(statusChange),
            )

            val externalOrderId = UUID.randomUUID()
            val timestamp = LocalDateTime.now()
            val event = OrderCreatedEvent(
                externalOrderId = externalOrderId,
                customerId = "CUST-12345",
                orderType = "STANDARD",
                channel = "WEB",
                orderLines = listOf(orderLine),
                billingAddress = address,
                notes = "Test notes",
                statusTracking = statusTracking,
                timestamp = timestamp,
            )

            describe("Address") {
                it("should have correct properties") {
                    address.fullName shouldBe "John Doe"
                    address.addressLine1 shouldBe "123 Main St"
                    address.addressLine2 shouldBe "Apt 4"
                    address.city shouldBe "New York"
                    address.stateProvince shouldBe "NY"
                    address.postalCode shouldBe "10001"
                    address.country shouldBe "USA"
                    address.phoneNumber shouldBe "555-1234"
                    address.email shouldBe "john@example.com"
                }

                it("should support equality") {
                    val sameAddress = address.copy()
                    address shouldBe sameAddress
                }

                it("should support copy") {
                    val modified = address.copy(city = "Los Angeles")
                    modified.city shouldBe "Los Angeles"
                    modified.fullName shouldBe address.fullName
                }

                it("should have proper hashCode") {
                    val sameAddress = address.copy()
                    address.hashCode() shouldBe sameAddress.hashCode()
                }
            }

            describe("OrderLine") {
                it("should have correct properties") {
                    orderLine.lineNumber shouldBe 1
                    orderLine.itemId shouldBe 1234567890L
                    orderLine.itemName shouldBe "Test Product"
                    orderLine.itemDescription shouldBe "A great product"
                    orderLine.quantity shouldBe 2
                    orderLine.unitPrice shouldBe BigDecimal("29.99")
                    orderLine.currency shouldBe "USD"
                    orderLine.taxRate shouldBe BigDecimal("0.08")
                    orderLine.discountAmount shouldBe BigDecimal("5.00")
                    orderLine.fulfillmentType shouldBe "STH"
                    orderLine.shippingAddress shouldBe address
                    orderLine.estimatedShipDate shouldBe LocalDate.of(2025, 1, 15)
                    orderLine.estimatedDeliveryDate shouldBe LocalDate.of(2025, 1, 20)
                }

                it("should use default fulfillment type") {
                    val lineWithDefaults = OrderCreatedEvent.OrderLine(
                        lineNumber = 1,
                        itemId = 1L,
                        itemName = "Item",
                        itemDescription = null,
                        quantity = 1,
                        unitPrice = BigDecimal("10.00"),
                        currency = "USD",
                        taxRate = BigDecimal.ZERO,
                        discountAmount = null,
                        shippingAddress = address,
                    )
                    lineWithDefaults.fulfillmentType shouldBe "STH"
                    lineWithDefaults.estimatedShipDate shouldBe null
                    lineWithDefaults.estimatedDeliveryDate shouldBe null
                }

                it("should support copy") {
                    val modified = orderLine.copy(quantity = 5)
                    modified.quantity shouldBe 5
                    modified.itemId shouldBe orderLine.itemId
                }
            }

            describe("StatusChange") {
                it("should have correct properties") {
                    statusChange.status shouldBe "CREATED"
                    statusChange.timestamp shouldBe LocalDateTime.of(2025, 1, 10, 10, 0, 0)
                    statusChange.notes shouldBe "Order created"
                }

                it("should support null notes") {
                    val changeWithoutNotes = OrderCreatedEvent.StatusChange(
                        status = "PROCESSING",
                        timestamp = LocalDateTime.now(),
                        notes = null,
                    )
                    changeWithoutNotes.notes shouldBe null
                }
            }

            describe("StatusTracking") {
                it("should have correct properties") {
                    statusTracking.currentStatus shouldBe "CREATED"
                    statusTracking.statusHistory.size shouldBe 1
                    statusTracking.statusHistory[0] shouldBe statusChange
                }
            }

            describe("OrderCreatedEvent properties") {
                it("should have correct properties") {
                    event.externalOrderId shouldBe externalOrderId
                    event.customerId shouldBe "CUST-12345"
                    event.orderType shouldBe "STANDARD"
                    event.channel shouldBe "WEB"
                    event.orderLines.size shouldBe 1
                    event.billingAddress shouldBe address
                    event.notes shouldBe "Test notes"
                    event.statusTracking shouldBe statusTracking
                    event.timestamp shouldBe timestamp
                }

                it("should use default order type") {
                    val eventWithDefaults = OrderCreatedEvent(
                        externalOrderId = UUID.randomUUID(),
                        customerId = "CUST-1",
                        channel = "MOBILE",
                        orderLines = listOf(orderLine),
                        billingAddress = address,
                        notes = null,
                        timestamp = LocalDateTime.now(),
                    )
                    eventWithDefaults.orderType shouldBe "STANDARD"
                    eventWithDefaults.statusTracking shouldBe null
                }

                it("should support copy") {
                    val modified = event.copy(customerId = "CUST-99999")
                    modified.customerId shouldBe "CUST-99999"
                    modified.channel shouldBe event.channel
                }
            }
        }

        describe("ReleaseSnapshot") {
            val timestamp = LocalDateTime.now()
            val payload = mapOf(
                "release_key" to "REL-KEY-001",
                "release_no" to "0001",
                "ship_node" to "DC-001",
            )

            val snapshot = ReleaseSnapshot(
                releaseId = "REL-001",
                orderId = "10-20251225-0000001",
                releaseStatus = "RELEASED",
                eventTimestamp = timestamp,
                payload = payload,
            )

            it("should have correct properties") {
                snapshot.releaseId shouldBe "REL-001"
                snapshot.orderId shouldBe "10-20251225-0000001"
                snapshot.releaseStatus shouldBe "RELEASED"
                snapshot.eventTimestamp shouldBe timestamp
                snapshot.payload shouldBe payload
            }

            it("should use empty map as default payload") {
                val snapshotWithoutPayload = ReleaseSnapshot(
                    releaseId = "REL-002",
                    orderId = "ORD-002",
                    releaseStatus = "CREATED",
                    eventTimestamp = LocalDateTime.now(),
                )
                snapshotWithoutPayload.payload shouldBe emptyMap()
            }

            it("should support equality") {
                val sameSnapshot = snapshot.copy()
                snapshot shouldBe sameSnapshot
            }

            it("should not be equal for different release IDs") {
                val differentSnapshot = snapshot.copy(releaseId = "REL-999")
                snapshot shouldNotBe differentSnapshot
            }

            it("should support copy") {
                val modified = snapshot.copy(releaseStatus = "SHIPPED")
                modified.releaseStatus shouldBe "SHIPPED"
                modified.releaseId shouldBe snapshot.releaseId
            }

            it("should have consistent hashCode") {
                val sameSnapshot = snapshot.copy()
                snapshot.hashCode() shouldBe sameSnapshot.hashCode()
            }
        }

        describe("ShipmentSnapshot") {
            val timestamp = LocalDateTime.now()
            val payload = mapOf(
                "shipment_key" to "SHIP-KEY-001",
                "carrier" to "UPS",
                "service_type" to "GROUND",
            )

            val snapshot = ShipmentSnapshot(
                shipmentId = "SHIP-001",
                orderId = "10-20251225-0000001",
                shipmentStatus = "SHIPPED",
                trackingNumber = "1Z999AA10123456784",
                eventTimestamp = timestamp,
                payload = payload,
            )

            it("should have correct properties") {
                snapshot.shipmentId shouldBe "SHIP-001"
                snapshot.orderId shouldBe "10-20251225-0000001"
                snapshot.shipmentStatus shouldBe "SHIPPED"
                snapshot.trackingNumber shouldBe "1Z999AA10123456784"
                snapshot.eventTimestamp shouldBe timestamp
                snapshot.payload shouldBe payload
            }

            it("should allow null tracking number") {
                val snapshotWithoutTracking = ShipmentSnapshot(
                    shipmentId = "SHIP-002",
                    orderId = "ORD-002",
                    shipmentStatus = "CREATED",
                    trackingNumber = null,
                    eventTimestamp = LocalDateTime.now(),
                )
                snapshotWithoutTracking.trackingNumber shouldBe null
            }

            it("should use empty map as default payload") {
                val snapshotWithoutPayload = ShipmentSnapshot(
                    shipmentId = "SHIP-003",
                    orderId = "ORD-003",
                    shipmentStatus = "CREATED",
                    trackingNumber = "TRACK-003",
                    eventTimestamp = LocalDateTime.now(),
                )
                snapshotWithoutPayload.payload shouldBe emptyMap()
            }

            it("should support equality") {
                val sameSnapshot = snapshot.copy()
                snapshot shouldBe sameSnapshot
            }

            it("should not be equal for different shipment IDs") {
                val differentSnapshot = snapshot.copy(shipmentId = "SHIP-999")
                snapshot shouldNotBe differentSnapshot
            }

            it("should support copy") {
                val modified = snapshot.copy(shipmentStatus = "DELIVERED")
                modified.shipmentStatus shouldBe "DELIVERED"
                modified.shipmentId shouldBe snapshot.shipmentId
            }

            it("should have consistent hashCode") {
                val sameSnapshot = snapshot.copy()
                snapshot.hashCode() shouldBe sameSnapshot.hashCode()
            }
        }
    })
