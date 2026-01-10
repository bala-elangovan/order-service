package io.github.balaelangovan.orders.consumer.adapter

import io.github.balaelangovan.orders.domain.event.ReleaseSnapshot
import io.github.balaelangovan.orders.domain.exception.DomainException
import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.exception.ProcessingException
import io.github.balaelangovan.orders.domain.port.inbound.ReleaseEventHandlerPort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * Kafka event consumer adapter for release events from the Release Service.
 * Consumes ReleaseSnapshot events and delegates to the event handler port.
 *
 * This adapter implements the event-driven architecture pattern:
 * - Listens to release-events topic
 * - Delegates to ReleaseEventHandlerPort for processing
 * - Provides manual acknowledgment for reliability
 */
@Component
class ReleaseEventConsumerAdapter(private val releaseEventHandler: ReleaseEventHandlerPort) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Consumes release events from Kafka.
     *
     * Flow:
     * 1. Receive event from Kafka
     * 2. Delegate to event handler port for upsert
     * 3. Acknowledge a message (manual commit)
     *
     * Error handling:
     * - On success: Message is acknowledged
     * - On failure: Exception thrown, a message not acknowledged, will be reprocessed
     */
    @KafkaListener(
        topics = ["\${kafka.topics.release-events}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "releaseKafkaListenerContainerFactory",
    )
    fun consumeReleaseEvent(event: ReleaseSnapshot, acknowledgment: Acknowledgment) = runBlocking {
        try {
            logger.info(
                "Received ReleaseSnapshot: releaseId={}, orderId={}, status={}",
                event.releaseId,
                event.orderId,
                event.releaseStatus,
            )

            releaseEventHandler.handleReleaseEvent(event)

            acknowledgment.acknowledge()

            logger.info(
                "Successfully processed ReleaseSnapshot: releaseId={}, orderId={}",
                event.releaseId,
                event.orderId,
            )
        } catch (e: DomainException) {
            logger.error(
                "Failed to process ReleaseSnapshot: releaseId={}, orderId={}, errorCode={}, error={}",
                event.releaseId,
                event.orderId,
                e.code,
                e.message,
                e,
            )
            throw e
        } catch (e: Exception) {
            logger.error(
                "Failed to process ReleaseSnapshot: releaseId={}, orderId={}, error={}",
                event.releaseId,
                event.orderId,
                e.message,
                e,
            )
            throw ProcessingException(
                "Failed to process release event: ${event.releaseId}",
                ErrorCode.EVENT_PROCESSING_FAILED,
                e,
            )
        }
    }
}
