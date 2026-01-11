package io.github.balaelangovan.orders.domain.service

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.aggregate.OrderLine
import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent
import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot
import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot
import io.github.balaelangovan.orders.domain.exception.ConflictException
import io.github.balaelangovan.orders.domain.exception.ResourceNotFoundException
import io.github.balaelangovan.orders.domain.mapper.OrderEventMapper
import io.github.balaelangovan.orders.domain.port.outbound.NotificationPort
import io.github.balaelangovan.orders.domain.port.outbound.OrderRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ReleaseSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.port.outbound.ShipmentSnapshotRepositoryPort
import io.github.balaelangovan.orders.domain.valueobject.Address
import io.github.balaelangovan.orders.domain.valueobject.Channel
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.domain.valueobject.ItemId
import io.github.balaelangovan.orders.domain.valueobject.LineItemId
import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.domain.valueobject.Money
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus
import io.github.balaelangovan.orders.domain.valueobject.OrderType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class OrderDomainServiceTest :
    DescribeSpec({

        val orderRepository = mockk<OrderRepositoryPort>()
        val notificationPort = mockk<NotificationPort>(relaxed = true)
        val releaseSnapshotRepository = mockk<ReleaseSnapshotRepositoryPort>()
        val shipmentSnapshotRepository = mockk<ShipmentSnapshotRepositoryPort>()
        val orderEventMapper = mockk<OrderEventMapper>()

        val service = OrderDomainService(
            orderRepository = orderRepository,
            notificationPort = notificationPort,
            releaseSnapshotRepository = releaseSnapshotRepository,
            shipmentSnapshotRepository = shipmentSnapshotRepository,
            orderEventMapper = orderEventMapper,
        )

        val testAddress = Address.of(
            fullName = "John Doe",
            addressLine1 = "123 Main St",
            addressLine2 = null,
            city = "New York",
            stateProvince = "NY",
            postalCode = "10001",
            country = "USA",
        )

        fun createTestOrderLine(): OrderLine = OrderLine.create(
            lineNumber = 1,
            itemId = ItemId(1234567890L),
            itemName = "Test Product",
            itemDescription = "A great product",
            quantity = 2,
            unitPrice = Money.of(29.99, "USD"),
            taxRate = BigDecimal("0.08"),
            discountAmount = null,
            fulfillmentType = FulfillmentType.STH,
            shippingAddress = testAddress,
        )

        fun createTestOrder(status: OrderStatus = OrderStatus.CREATED): Order {
            val order = Order.create(
                orderId = OrderId("10-20251225-0000001"),
                customerId = CustomerId("CUST-12345"),
                orderType = OrderType.STANDARD,
                channel = Channel.WEB,
                lines = listOf(createTestOrderLine()),
                billingAddress = testAddress,
            )
            return when (status) {
                OrderStatus.IN_RELEASE -> order.inRelease()
                OrderStatus.RELEASED -> order.release()
                OrderStatus.IN_SHIPMENT -> order.release().inShipment()
                OrderStatus.SHIPPED -> order.release().ship()
                OrderStatus.DELIVERED -> order.release().ship().deliver()
                OrderStatus.CANCELLED -> order.cancel()
                else -> order
            }
        }

        describe("createOrder") {
            it("should create order and send notification") {
                val order = createTestOrder()
                coEvery { orderRepository.existsByExternalOrderId(any()) } returns false
                coEvery { orderRepository.save(any()) } returns order

                val result = service.createOrder(order)

                result shouldBe order
                coVerify { orderRepository.save(order) }
                coVerify { notificationPort.notifyOrderCreated(order) }
            }

            it("should create order without external ID check") {
                val order = createTestOrder()
                coEvery { orderRepository.save(any()) } returns order

                val result = service.createOrder(order)

                result shouldBe order
            }

            it("should throw ConflictException for duplicate external order ID") {
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
                coEvery { orderRepository.existsByExternalOrderId(externalId) } returns true

                val exception = shouldThrow<ConflictException> {
                    service.createOrder(order)
                }
                exception.message shouldContain "already exists"
            }
        }

        describe("handleOrderCreatedEvent") {
            it("should convert event to domain and create order") {
                val event = OrderCreatedEvent(
                    externalOrderId = UUID.randomUUID(),
                    customerId = "CUST-12345",
                    orderType = "STANDARD",
                    channel = "WEB",
                    orderLines = listOf(
                        OrderCreatedEvent.OrderLine(
                            lineNumber = 1,
                            itemId = 1234567890L,
                            itemName = "Test Product",
                            itemDescription = null,
                            quantity = 1,
                            unitPrice = BigDecimal("10.00"),
                            currency = "USD",
                            taxRate = BigDecimal("0.08"),
                            discountAmount = null,
                            fulfillmentType = "STH",
                            shippingAddress = OrderCreatedEvent.Address(
                                fullName = "John Doe",
                                addressLine1 = "123 Main St",
                                addressLine2 = null,
                                city = "New York",
                                stateProvince = "NY",
                                postalCode = "10001",
                                country = "USA",
                                phoneNumber = "555-1234",
                                email = "test@example.com",
                            ),
                        ),
                    ),
                    billingAddress = OrderCreatedEvent.Address(
                        fullName = "John Doe",
                        addressLine1 = "123 Main St",
                        addressLine2 = null,
                        city = "New York",
                        stateProvince = "NY",
                        postalCode = "10001",
                        country = "USA",
                        phoneNumber = "555-1234",
                        email = "test@example.com",
                    ),
                    notes = null,
                    timestamp = LocalDateTime.now(),
                )
                val order = createTestOrder()

                coEvery { orderEventMapper.toDomain(event) } returns order
                coEvery { orderRepository.existsByExternalOrderId(any()) } returns false
                coEvery { orderRepository.save(any()) } returns order

                val result = service.handleOrderCreatedEvent(event)

                result shouldBe order
                coVerify { orderEventMapper.toDomain(event) }
            }
        }

        describe("handleReleaseEvent") {
            it("should save release snapshot") {
                val snapshot = ReleaseSnapshot(
                    releaseId = "REL-001",
                    orderId = "10-20251225-0000001",
                    releaseStatus = "RELEASED",
                    eventTimestamp = LocalDateTime.now(),
                )
                coJustRun { releaseSnapshotRepository.upsert(snapshot) }

                service.handleReleaseEvent(snapshot)

                coVerify { releaseSnapshotRepository.upsert(snapshot) }
            }
        }

        describe("handleShipmentEvent") {
            it("should save shipment snapshot") {
                val snapshot = ShipmentSnapshot(
                    shipmentId = "SHIP-001",
                    orderId = "10-20251225-0000001",
                    shipmentStatus = "SHIPPED",
                    trackingNumber = "1Z999AA10123456784",
                    eventTimestamp = LocalDateTime.now(),
                )
                coJustRun { shipmentSnapshotRepository.upsert(snapshot) }

                service.handleShipmentEvent(snapshot)

                coVerify { shipmentSnapshotRepository.upsert(snapshot) }
            }
        }

        describe("getReleasesForOrder") {
            it("should return releases for order") {
                val releases = listOf(
                    ReleaseSnapshot("REL-001", "ORD-001", "RELEASED", LocalDateTime.now()),
                    ReleaseSnapshot("REL-002", "ORD-001", "SHIPPED", LocalDateTime.now()),
                )
                coEvery { releaseSnapshotRepository.findByOrderId("ORD-001") } returns releases

                val result = service.getReleasesForOrder("ORD-001")

                result shouldBe releases
            }
        }

        describe("getShipmentsForOrder") {
            it("should return shipments for order") {
                val shipments = listOf(
                    ShipmentSnapshot("SHIP-001", "ORD-001", "SHIPPED", "TRACK-001", LocalDateTime.now()),
                )
                coEvery { shipmentSnapshotRepository.findByOrderId("ORD-001") } returns shipments

                val result = service.getShipmentsForOrder("ORD-001")

                result shouldBe shipments
            }
        }

        describe("getOrderById") {
            it("should return order when found") {
                val order = createTestOrder()
                coEvery { orderRepository.findById(order.id) } returns order

                val result = service.getOrderById(order.id)

                result shouldBe order
            }

            it("should throw ResourceNotFoundException when not found") {
                val orderId = OrderId("10-20251225-0000001")
                coEvery { orderRepository.findById(orderId) } returns null

                val exception = shouldThrow<ResourceNotFoundException> {
                    service.getOrderById(orderId)
                }
                exception.resource shouldBe "Order"
                exception.identifier shouldBe orderId.value
            }
        }

        describe("getOrdersByCustomerId") {
            it("should return customer orders") {
                val customerId = CustomerId("CUST-12345")
                val orders = listOf(createTestOrder())
                coEvery { orderRepository.findByCustomerId(customerId) } returns orders

                val result = service.getOrdersByCustomerId(customerId)

                result shouldBe orders
            }
        }

        describe("getAllOrders") {
            it("should return paginated orders") {
                val orders = listOf(createTestOrder())
                coEvery { orderRepository.findAll(0, 20) } returns orders

                val result = service.getAllOrders(0, 20)

                result shouldBe orders
            }
        }

        describe("updateOrder") {
            it("should update notes") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateOrder(order.id, "New notes", null)

                result.notes shouldBe "New notes"
            }

            it("should update billing address") {
                val order = createTestOrder()
                val newAddress = Address.of(
                    fullName = "Jane Doe",
                    addressLine1 = "456 Oak Ave",
                    addressLine2 = null,
                    city = "LA",
                    stateProvince = "CA",
                    postalCode = "90001",
                    country = "USA",
                )
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateOrder(order.id, null, newAddress)

                result.billingAddress shouldBe newAddress
            }

            it("should throw ResourceNotFoundException for unknown order") {
                val orderId = OrderId("10-20251225-9999999")
                coEvery { orderRepository.findById(orderId) } returns null

                shouldThrow<ResourceNotFoundException> {
                    service.updateOrder(orderId, "notes", null)
                }
            }
        }

        describe("updateStatus") {
            it("should transition to IN_RELEASE") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.IN_RELEASE)

                result.status shouldBe OrderStatus.IN_RELEASE
                coVerify { notificationPort.notifyOrderInRelease(any()) }
            }

            it("should transition to RELEASED") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.RELEASED)

                result.status shouldBe OrderStatus.RELEASED
                coVerify { notificationPort.notifyOrderReleased(any()) }
            }

            it("should transition to IN_SHIPMENT") {
                val order = createTestOrder(OrderStatus.RELEASED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.IN_SHIPMENT)

                result.status shouldBe OrderStatus.IN_SHIPMENT
                coVerify { notificationPort.notifyOrderInShipment(any()) }
            }

            it("should transition to SHIPPED") {
                val order = createTestOrder(OrderStatus.RELEASED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.SHIPPED)

                result.status shouldBe OrderStatus.SHIPPED
                coVerify { notificationPort.notifyOrderShipped(any()) }
            }

            it("should transition to DELIVERED") {
                val order = createTestOrder(OrderStatus.SHIPPED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.DELIVERED)

                result.status shouldBe OrderStatus.DELIVERED
                coVerify { notificationPort.notifyOrderDelivered(any()) }
            }

            it("should transition to CANCELLED") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateStatus(order.id, OrderStatus.CANCELLED)

                result.status shouldBe OrderStatus.CANCELLED
                coVerify { notificationPort.notifyOrderCancelled(any()) }
            }

            it("should return order unchanged for CREATED status") {
                val order = createTestOrder()
                coEvery { orderRepository.findById(order.id) } returns order

                val result = service.updateStatus(order.id, OrderStatus.CREATED)

                result shouldBe order
            }
        }

        describe("inReleaseOrder") {
            it("should transition to IN_RELEASE and notify") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.inReleaseOrder(order.id)

                result.status shouldBe OrderStatus.IN_RELEASE
                coVerify { notificationPort.notifyOrderInRelease(any()) }
            }
        }

        describe("releaseOrder") {
            it("should transition to RELEASED and notify") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.releaseOrder(order.id)

                result.status shouldBe OrderStatus.RELEASED
                coVerify { notificationPort.notifyOrderReleased(any()) }
            }
        }

        describe("inShipmentOrder") {
            it("should transition to IN_SHIPMENT and notify") {
                val order = createTestOrder(OrderStatus.RELEASED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.inShipmentOrder(order.id)

                result.status shouldBe OrderStatus.IN_SHIPMENT
                coVerify { notificationPort.notifyOrderInShipment(any()) }
            }
        }

        describe("shipOrder") {
            it("should ship order and notify") {
                val order = createTestOrder(OrderStatus.RELEASED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.shipOrder(order.id)

                result.status shouldBe OrderStatus.SHIPPED
                coVerify { notificationPort.notifyOrderShipped(any()) }
            }
        }

        describe("deliverOrder") {
            it("should deliver order and notify") {
                val order = createTestOrder(OrderStatus.SHIPPED)
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.deliverOrder(order.id)

                result.status shouldBe OrderStatus.DELIVERED
                coVerify { notificationPort.notifyOrderDelivered(any()) }
            }
        }

        describe("cancelOrder") {
            it("should cancel order and notify") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.cancelOrder(order.id)

                result.status shouldBe OrderStatus.CANCELLED
                coVerify { notificationPort.notifyOrderCancelled(any()) }
            }
        }

        describe("updateLineStatus") {
            it("should update line status") {
                val order = createTestOrder()
                val lineId = order.lines.first().id
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                val result = service.updateLineStatus(order.id, lineId, LineStatusType.ALLOCATED, "Inventory reserved")

                result.lines.first().lineStatus.status shouldBe LineStatusType.ALLOCATED
            }
        }

        describe("deleteOrder") {
            it("should soft-delete order by cancelling") {
                val order = createTestOrder()
                val orderSlot = slot<Order>()
                coEvery { orderRepository.findById(order.id) } returns order
                coEvery { orderRepository.save(capture(orderSlot)) } answers { orderSlot.captured }

                service.deleteOrder(order.id)

                orderSlot.captured.status shouldBe OrderStatus.CANCELLED
            }
        }

        describe("createOrder with customerId and lines (unsupported)") {
            it("should throw UnsupportedOperationException") {
                val customerId = CustomerId("CUST-12345")
                val lines = listOf(createTestOrderLine())

                shouldThrow<UnsupportedOperationException> {
                    service.createOrder(customerId, lines)
                }
            }
        }
    })
