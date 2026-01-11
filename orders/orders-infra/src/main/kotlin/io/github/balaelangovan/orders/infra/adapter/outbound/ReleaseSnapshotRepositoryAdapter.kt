package io.github.balaelangovan.orders.infra.adapter.outbound

import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot
import io.github.balaelangovan.orders.domain.port.outbound.ReleaseSnapshotRepositoryPort
import io.github.balaelangovan.orders.infra.entity.ReleaseSnapshotEntity
import io.github.balaelangovan.orders.infra.repository.OrderJpaRepository
import io.github.balaelangovan.orders.infra.repository.ReleaseSnapshotJpaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Persistence adapter for release snapshots.
 * Implements upsert semantics - creates new or updates existing based on releaseId.
 */
@Component
@Transactional
class ReleaseSnapshotRepositoryAdapter(
    private val releaseSnapshotRepository: ReleaseSnapshotJpaRepository,
    private val orderRepository: OrderJpaRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ReleaseSnapshotRepositoryPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun upsert(releaseSnapshot: ReleaseSnapshot): ReleaseSnapshot = withContext(ioDispatcher) {
        logger.debug(
            "Upserting release snapshot: releaseId={}, orderId={}",
            releaseSnapshot.releaseId,
            releaseSnapshot.orderId,
        )

        val orderEntity = orderRepository.findByOrderId(releaseSnapshot.orderId).orElse(null)
            ?: throw IllegalStateException("Order not found: ${releaseSnapshot.orderId}")

        val existingEntity = releaseSnapshotRepository.findByReleaseId(releaseSnapshot.releaseId).orElse(null)

        val entityToSave = if (existingEntity != null) {
            existingEntity.copy(
                releaseStatus = releaseSnapshot.releaseStatus,
                eventTimestamp = releaseSnapshot.eventTimestamp,
                payload = releaseSnapshot.payload,
                updatedAt = LocalDateTime.now(),
            )
        } else {
            ReleaseSnapshotEntity(
                order = orderEntity,
                releaseId = releaseSnapshot.releaseId,
                releaseStatus = releaseSnapshot.releaseStatus,
                eventTimestamp = releaseSnapshot.eventTimestamp,
                payload = releaseSnapshot.payload,
            )
        }

        val savedEntity = releaseSnapshotRepository.save(entityToSave)
        logger.debug("Release snapshot saved: releaseId={}", savedEntity.releaseId)

        entityToSnapshot(savedEntity)
    }

    override suspend fun findByReleaseId(releaseId: String): ReleaseSnapshot? = withContext(ioDispatcher) {
        logger.debug("Finding release snapshot by releaseId: {}", releaseId)
        releaseSnapshotRepository.findByReleaseId(releaseId).orElse(null)?.let { entityToSnapshot(it) }
    }

    override suspend fun findByOrderId(orderId: String): List<ReleaseSnapshot> = withContext(ioDispatcher) {
        logger.debug("Finding release snapshots by orderId: {}", orderId)
        releaseSnapshotRepository.findByOrderOrderId(orderId).map { entityToSnapshot(it) }
    }

    private fun entityToSnapshot(entity: ReleaseSnapshotEntity): ReleaseSnapshot = ReleaseSnapshot(
        releaseId = entity.releaseId,
        orderId = entity.order?.orderId ?: "",
        releaseStatus = entity.releaseStatus,
        eventTimestamp = entity.eventTimestamp,
        payload = entity.payload,
    )
}
