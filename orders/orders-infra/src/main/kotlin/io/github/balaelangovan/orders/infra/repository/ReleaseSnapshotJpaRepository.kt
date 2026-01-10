package io.github.balaelangovan.orders.infra.repository

import io.github.balaelangovan.orders.infra.entity.ReleaseSnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * JPA repository for ReleaseSnapshot entities.
 * Supports upsert semantics via findByReleaseId (release_id has a unique constraint).
 *
 * Uses hybrid storage approach - key fields are indexed columns,
 * full payload is stored as JSONB. For queries on payload fields,
 * use native queries with JSONB operators.
 */
@Repository
interface ReleaseSnapshotJpaRepository : JpaRepository<ReleaseSnapshotEntity, Long> {

    /**
     * Find by release ID for upsert operations.
     * Used when receiving release events from Release Service.
     */
    fun findByReleaseId(releaseId: String): Optional<ReleaseSnapshotEntity>

    /**
     * Find all releases for a specific order by order ID.
     */
    fun findByOrderOrderId(orderId: String): List<ReleaseSnapshotEntity>
}
