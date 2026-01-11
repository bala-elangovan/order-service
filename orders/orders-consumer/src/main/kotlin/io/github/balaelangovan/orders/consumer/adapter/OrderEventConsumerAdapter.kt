package io.github.balaelangovan.orders.consumer.adapter

import io.github.balaelangovan.orders.domain.event.OrderCreatedEvent
import io.github.balaelangovan.orders.domain.exception.DomainException
import io.github.balaelangovan.orders.domain.exception.ErrorCode
import io.github.balaelangovan.orders.domain.exception.ProcessingException
import io.github.balaelangovan.orders.domain.port.inbound.OrderEventHandlerPort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * Kafka event consumer adapter for order creation events.
 * Consumes OrderCreatedEvent from checkout service and delegates to the event handler port.
 *
 * This adapter implements the event-driven architecture pattern:
 * - Listens to checkout-order-create topic
 * - Delegates to OrderEventHandlerPort for processing
 * - Provides manual acknowledgment for reliability
 */
@Component
class OrderEventConsumerAdapter(private val orderEventHandler: OrderEventHandlerPort) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Consumes order created events from Kafka.
     *
     * Flow:
     * 1. Receive event from Kafka
     * 2. Delegate to event handler port
     * 3. Acknowledge a message (manual commit)
     *
     * Error handling:
     * - On success: Message is acknowledged
     * - On failure: Exception thrown, a message not acknowledged, will be reprocessed
     */
    @KafkaListener(
        topics = ["\${kafka.topics.order-created}"],
        groupId = "\${spring.kafka.consumer.group-id}",
    )
    fun consumeOrderCreatedEvent(event: OrderCreatedEvent, acknowledgment: Acknowledgment) = runBlocking {
        try {
            logger.info(
                "Received OrderCreatedEvent: externalOrderId={}, customerId={}, channel={}, lineCount={}",
                event.externalOrderId,
                event.customerId,
                event.channel,
                event.orderLines.size,
            )

            val savedOrder = orderEventHandler.handleOrderCreatedEvent(event)

            acknowledgment.acknowledge()

            logger.info(
                "Successfully processed OrderCreatedEvent: externalOrderId={}, savedOrderId={}",
                event.externalOrderId,
                savedOrder.id.value,
            )
        } catch (e: DomainException) {
            logger.error(
                "Failed to process OrderCreatedEvent: externalOrderId={}, errorCode={}, error={}",
                event.externalOrderId,
                e.code,
                e.message,
                e,
            )
            throw e
        } catch (e: Exception) {
            logger.error(
                "Failed to process OrderCreatedEvent: externalOrderId={}, error={}",
                event.externalOrderId,
                e.message,
                e,
            )
            throw ProcessingException(
                "Failed to process order created event: ${event.externalOrderId}",
                ErrorCode.EVENT_PROCESSING_FAILED,
                e,
            )
        }
    }
}
