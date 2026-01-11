package io.github.balaelangovan.orders.infra.adapter.outbound

import io.github.balaelangovan.orders.domain.aggregate.Order
import io.github.balaelangovan.orders.domain.port.outbound.NotificationPort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Simple notification adapter that logs notifications.
 * In a real system, this would send emails, SMS, or publish events.
 */
@Component
class LoggingNotificationAdapter(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : NotificationPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun notifyOrderCreated(order: Order): Unit = withContext(ioDispatcher) {
        logger.info(
            "NOTIFICATION: Order created - ID: {}, Customer: {}, Total: {}",
            order.id,
            order.customerId,
            order.totalAmount,
        )
    }

    override suspend fun notifyOrderShipped(order: Order): Unit = withContext(ioDispatcher) {
        logger.info(
            "NOTIFICATION: Order shipped - ID: {}, Customer: {}",
            order.id,
            order.customerId,
        )
    }

    override suspend fun notifyOrderDelivered(order: Order): Unit = withContext(ioDispatcher) {
        logger.info(
            "NOTIFICATION: Order delivered - ID: {}, Customer: {}",
            order.id,
            order.customerId,
        )
    }

    override suspend fun notifyOrderCancelled(order: Order): Unit = withContext(ioDispatcher) {
        logger.info(
            "NOTIFICATION: Order cancelled - ID: {}, Customer: {}",
            order.id,
            order.customerId,
        )
    }
}
