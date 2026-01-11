package io.github.balaelangovan.orders.consumer.adapter

import io.github.balaelangovan.orders.domain.event.ShipmentSnapshot
import io.github.balaelangovan.orders.domain.exception.DomainException
import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.exception.ProcessingException
import io.github.balaelangovan.orders.domain.port.inbound.ShipmentEventHandlerPort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * Kafka event consumer adapter for shipment events from the Shipment Service.
 * Consumes ShipmentSnapshot events and delegates to the event handler port.
 *
 * This adapter implements the event-driven architecture pattern:
 * - Listens to shipment-events topic
 * - Delegates to ShipmentEventHandlerPort for processing
 * - Provides manual acknowledgment for reliability
 */
@Component
class ShipmentEventConsumerAdapter(private val shipmentEventHandler: ShipmentEventHandlerPort) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Consumes shipment events from Kafka.
     *
     * Flow:
     * 1. Receive event from Kafka
     * 2. Delegate to event handler port for upsert
     * 3. Acknowledge message (manual commit)
     *
     * Error handling:
     * - On success: Message is acknowledged
     * - On failure: Exception thrown, message not acknowledged, will be reprocessed
     */
    @KafkaListener(
        topics = ["\${kafka.topics.shipment-events}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "shipmentKafkaListenerContainerFactory",
    )
    fun consumeShipmentEvent(event: ShipmentSnapshot, acknowledgment: Acknowledgment) = runBlocking {
        try {
            logger.info(
                "Received ShipmentSnapshot: shipmentId={}, orderId={}, status={}, trackingNumber={}",
                event.shipmentId,
                event.orderId,
                event.shipmentStatus,
                event.trackingNumber,
            )

            shipmentEventHandler.handleShipmentEvent(event)

            acknowledgment.acknowledge()

            logger.info(
                "Successfully processed ShipmentSnapshot: shipmentId={}, orderId={}",
                event.shipmentId,
                event.orderId,
            )
        } catch (e: DomainException) {
            logger.error(
                "Failed to process ShipmentSnapshot: shipmentId={}, orderId={}, errorCode={}, error={}",
                event.shipmentId,
                event.orderId,
                e.code,
                e.message,
                e,
            )
            throw e
        } catch (e: Exception) {
            logger.error(
                "Failed to process ShipmentSnapshot: shipmentId={}, orderId={}, error={}",
                event.shipmentId,
                event.orderId,
                e.message,
                e,
            )
            throw ProcessingException(
                "Failed to process shipment event: ${event.shipmentId}",
                ErrorCode.EVENT_PROCESSING_FAILED,
                e,
            )
        }
    }
}
