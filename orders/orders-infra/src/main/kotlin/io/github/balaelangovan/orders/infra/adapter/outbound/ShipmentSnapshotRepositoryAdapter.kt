package io.github.balaelangovan.orders.infra.adapter.outbound

import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot
import io.github.balaelangovan.orders.domain.port.outbound.ShipmentSnapshotRepositoryPort
import io.github.balaelangovan.orders.infra.entity.ShipmentSnapshotEntity
import io.github.balaelangovan.orders.infra.repository.OrderJpaRepository
import io.github.balaelangovan.orders.infra.repository.ShipmentSnapshotJpaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Persistence adapter for shipment snapshots.
 * Implements upsert semantics - creates new or updates existing based on shipmentId.
 */
@Component
@Transactional
class ShipmentSnapshotRepositoryAdapter(
    private val shipmentSnapshotRepository: ShipmentSnapshotJpaRepository,
    private val orderRepository: OrderJpaRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ShipmentSnapshotRepositoryPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun upsert(shipmentSnapshot: ShipmentSnapshot): ShipmentSnapshot = withContext(ioDispatcher) {
        logger.debug(
            "Upserting shipment snapshot: shipmentId={}, orderId={}",
            shipmentSnapshot.shipmentId,
            shipmentSnapshot.orderId,
        )

        val orderEntity = orderRepository.findByOrderId(shipmentSnapshot.orderId).orElse(null)
            ?: throw IllegalStateException("Order not found: ${shipmentSnapshot.orderId}")

        val existingEntity = shipmentSnapshotRepository.findByShipmentId(shipmentSnapshot.shipmentId).orElse(null)

        val entityToSave = existingEntity?.copy(
            shipmentStatus = shipmentSnapshot.shipmentStatus,
            trackingNumber = shipmentSnapshot.trackingNumber,
            eventTimestamp = shipmentSnapshot.eventTimestamp,
            payload = shipmentSnapshot.payload,
            updatedAt = LocalDateTime.now(),
        )
            ?: ShipmentSnapshotEntity(
                order = orderEntity,
                shipmentId = shipmentSnapshot.shipmentId,
                shipmentStatus = shipmentSnapshot.shipmentStatus,
                trackingNumber = shipmentSnapshot.trackingNumber,
                eventTimestamp = shipmentSnapshot.eventTimestamp,
                payload = shipmentSnapshot.payload,
            )

        val savedEntity = shipmentSnapshotRepository.save(entityToSave)
        logger.debug("Shipment snapshot saved: shipmentId={}", savedEntity.shipmentId)

        entityToSnapshot(savedEntity)
    }

    override suspend fun findByShipmentId(shipmentId: String): ShipmentSnapshot? = withContext(ioDispatcher) {
        logger.debug("Finding shipment snapshot by shipmentId: {}", shipmentId)
        shipmentSnapshotRepository.findByShipmentId(shipmentId).orElse(null)?.let { entityToSnapshot(it) }
    }

    override suspend fun findByOrderId(orderId: String): List<ShipmentSnapshot> = withContext(ioDispatcher) {
        logger.debug("Finding shipment snapshots by orderId: {}", orderId)
        shipmentSnapshotRepository.findByOrderOrderId(orderId).map { entityToSnapshot(it) }
    }

    override suspend fun findByTrackingNumber(trackingNumber: String): ShipmentSnapshot? = withContext(ioDispatcher) {
        logger.debug("Finding shipment snapshot by trackingNumber: {}", trackingNumber)
        shipmentSnapshotRepository.findByTrackingNumber(trackingNumber).orElse(null)?.let { entityToSnapshot(it) }
    }

    private fun entityToSnapshot(entity: ShipmentSnapshotEntity): ShipmentSnapshot = ShipmentSnapshot(
        shipmentId = entity.shipmentId,
        orderId = entity.order?.orderId ?: "",
        shipmentStatus = entity.shipmentStatus,
        trackingNumber = entity.trackingNumber,
        eventTimestamp = entity.eventTimestamp,
        payload = entity.payload,
    )
}
