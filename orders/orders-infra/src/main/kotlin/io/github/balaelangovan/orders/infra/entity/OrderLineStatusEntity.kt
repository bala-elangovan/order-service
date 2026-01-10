package io.github.balaelangovan.orders.infra.entity

import io.github.balaelangovan.orders.domain.valueobject.LineStatusType
import io.github.balaelangovan.orders.infra.annotation.TimestampId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for OrderLineStatus.
 * ANEMIC - no business logic, just data holder.
 * Tracks the status of order line items with status code and description.
 */
@Entity
@Table(name = "order_line_status")
data class OrderLineStatusEntity(
    @Id
    @TimestampId
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Column(name = "line_status_key", nullable = false, unique = true, columnDefinition = "UUID")
    val lineStatusKey: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_key", referencedColumnName = "line_key", nullable = false)
    var line: OrderLineEntity? = null,

    @Column(name = "quantity", nullable = false)
    val quantity: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    val status: LineStatusType = LineStatusType.CREATED,

    @Column(name = "status_code", nullable = false, length = 10)
    val statusCode: String = LineStatusType.CREATED.code,

    @Column(name = "status_description", nullable = false, length = 200)
    val statusDescription: String = LineStatusType.CREATED.description,

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
