package io.github.balaelangovan.orders.domain.port.outbound

import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot

/**
 * Outbound port for persisting release snapshots.
 * Provides upsert semantics for release events from the Release Service.
 */
interface ReleaseSnapshotRepositoryPort {

    /**
     * Saves or updates a release snapshot using upsert semantics.
     * If a snapshot with the same releaseId exists, it will be updated.
     *
     * @param releaseSnapshot the release snapshot to save
     * @return the saved release snapshot
     */
    suspend fun upsert(releaseSnapshot: ReleaseSnapshot): ReleaseSnapshot

    /**
     * Finds a release snapshot by its release ID.
     *
     * @param releaseId the release identifier
     * @return the release snapshot or null if not found
     */
    suspend fun findByReleaseId(releaseId: String): ReleaseSnapshot?

    /**
     * Finds all release snapshots for an order.
     *
     * @param orderId the order identifier
     * @return list of release snapshots for the order
     */
    suspend fun findByOrderId(orderId: String): List<ReleaseSnapshot>
}
