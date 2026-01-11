package io.github.balaelangovan.orders.infra.entity

import io.github.balaelangovan.orders.infra.annotation.TimestampId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

/**
 * JPA entity for storing release snapshot data using a hybrid approach.
 * Key fields are indexed for efficient queries, while the full event
 * payload is stored as JSONB for flexibility and future-proofing.
 *
 * Updates are handled via upsert on release_id (unique constraint).
 * One order can have multiple releases (1:N relationship).
 */
@Entity
@Table(name = "release_snapshots")
data class ReleaseSnapshotEntity(
    @Id
    @TimestampId
    @Column(name = "id", nullable = false)
    val id: Long = 0L,

    @Column(name = "release_key", nullable = false, unique = true, columnDefinition = "UUID")
    val releaseKey: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_key", referencedColumnName = "order_key", nullable = false)
    val order: OrderEntity? = null,

    @Column(name = "release_id", nullable = false, unique = true, length = 100)
    val releaseId: String = "",

    @Column(name = "release_status", nullable = false, length = 50)
    val releaseStatus: String = "",

    @Column(name = "event_timestamp", nullable = false)
    val eventTimestamp: LocalDateTime = LocalDateTime.now(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    val payload: Map<String, Any?> = emptyMap(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
