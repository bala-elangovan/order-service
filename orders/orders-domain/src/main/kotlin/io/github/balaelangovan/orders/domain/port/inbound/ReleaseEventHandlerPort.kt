package io.github.balaelangovan.orders.domain.port.inbound

import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot

/**
 * Inbound port for handling release events from the Release Service.
 * Receives release snapshots and persists them for order tracking.
 */
fun interface ReleaseEventHandlerPort {

    /**
     * Handles a release snapshot event.
     * Uses upsert semantics - creates new or updates existing based on releaseId.
     *
     * @param releaseSnapshot the release snapshot data from the event
     */
    suspend fun handleReleaseEvent(releaseSnapshot: ReleaseSnapshot)
}
