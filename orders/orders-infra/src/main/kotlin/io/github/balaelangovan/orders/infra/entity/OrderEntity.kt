package io.github.balaelangovan.orders.infra.entity

import io.github.balaelangovan.orders.domain.valueobject.Channel
import io.github.balaelangovan.orders.domain.valueobject.OrderStatus
import io.github.balaelangovan.orders.domain.valueobject.OrderType
import io.github.balaelangovan.orders.infra.annotation.TimestampId
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedEntityGraphs
import jakarta.persistence.NamedSubgraph
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for Order.
 * ANEMIC - no business logic, just data holder.
 *
 * Supports multiple order types (STANDARD, GUEST, RETURN, etc.)
 * Shipping addresses are now at the line level to support mixed fulfillment.
 *
 * Entity Graphs defined for efficient fetching:
 * - order-with-lines: Fetches order with lines and their statuses (for most read operations)
 * - order-with-snapshots: Fetches order with shipment and release snapshots
 * - order-full: Fetches order with all associations (lines, statuses, snapshots)
 */
@Entity
@Table(name = "orders")
@NamedEntityGraphs(
    NamedEntityGraph(
        name = "order-with-lines",
        attributeNodes = [
            NamedAttributeNode(value = "lines", subgraph = "lines-with-status"),
        ],
        subgraphs = [
            NamedSubgraph(
                name = "lines-with-status",
                attributeNodes = [NamedAttributeNode("lineStatus")],
            ),
        ],
    ),
    NamedEntityGraph(
        name = "order-with-snapshots",
        attributeNodes = [
            NamedAttributeNode("shipmentSnapshots"),
            NamedAttributeNode("releaseSnapshots"),
        ],
    ),
    NamedEntityGraph(
        name = "order-full",
        attributeNodes = [
            NamedAttributeNode(value = "lines", subgraph = "lines-with-status"),
            NamedAttributeNode("shipmentSnapshots"),
            NamedAttributeNode("releaseSnapshots"),
        ],
        subgraphs = [
            NamedSubgraph(
                name = "lines-with-status",
                attributeNodes = [NamedAttributeNode("lineStatus")],
            ),
        ],
    ),
)
data class OrderEntity(
    @Id
    @TimestampId
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Column(name = "order_key", nullable = false, unique = true, columnDefinition = "UUID")
    val orderKey: UUID = UUID.randomUUID(),

    @Column(name = "order_id", nullable = false, unique = true, length = 20)
    val orderId: String = "",

    @Column(name = "external_order_id", unique = true, columnDefinition = "UUID")
    val externalOrderId: UUID? = null,

    @Column(name = "customer_id", nullable = false, length = 100)
    val customerId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    val orderType: OrderType = OrderType.STANDARD,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    val channel: Channel = Channel.WEB,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    val status: OrderStatus = OrderStatus.CREATED,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_key", referencedColumnName = "address_key", nullable = false)
    var billingAddress: AddressEntity? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val shipmentSnapshots: MutableList<ShipmentSnapshotEntity> = mutableListOf(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val releaseSnapshots: MutableList<ReleaseSnapshotEntity> = mutableListOf(),

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    val subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String = "USD",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val lines: MutableList<OrderLineEntity> = mutableListOf(),
)
