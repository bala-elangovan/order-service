package io.github.balaelangovan.orders.infra.entity

import io.github.balaelangovan.orders.domain.valueobject.FulfillmentType
import io.github.balaelangovan.orders.infra.annotation.TimestampId
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for OrderLine.
 * ANEMIC - no business logic, just data holder.
 *
 * Each line can have its own fulfillment type and shipping address
 * to support mixed fulfillment scenarios.
 *
 * Date fields:
 * - ESD/EDD: Estimated dates from checkout/promise system
 * - PSD/PDD: Promised dates from allocation service
 */
@Entity
@Table(name = "order_lines")
data class OrderLineEntity(
    @Id
    @TimestampId
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Column(name = "line_key", nullable = false, unique = true, columnDefinition = "UUID")
    val lineKey: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_key", referencedColumnName = "order_key", nullable = false)
    var order: OrderEntity? = null,

    @Column(name = "line_number", nullable = false)
    val lineNumber: Int = 1,

    @Column(name = "item_id", nullable = false)
    val itemId: Long = 0L,

    @Column(name = "item_name", nullable = false, length = 500)
    val itemName: String = "",

    @Column(name = "item_description", columnDefinition = "TEXT")
    val itemDescription: String? = null,

    @Column(name = "quantity", nullable = false)
    val quantity: Int = 1,

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    val unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_rate", precision = 5, scale = 4)
    val taxRate: BigDecimal? = null,

    @Column(name = "discount_amount", precision = 19, scale = 2)
    val discountAmount: BigDecimal? = null,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String = "USD",

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 20)
    val fulfillmentType: FulfillmentType = FulfillmentType.STH,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_key", referencedColumnName = "address_key")
    var shippingAddress: AddressEntity? = null,

    // Estimated dates from checkout/promise system
    @Column(name = "estimated_ship_date")
    val estimatedShipDate: LocalDate? = null,

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDate? = null,

    // Promised dates from allocation service (actual dates)
    @Column(name = "promised_ship_date")
    val promisedShipDate: LocalDate? = null,

    @Column(name = "promised_delivery_date")
    val promisedDeliveryDate: LocalDate? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToOne(mappedBy = "line", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var lineStatus: OrderLineStatusEntity? = null,
)
