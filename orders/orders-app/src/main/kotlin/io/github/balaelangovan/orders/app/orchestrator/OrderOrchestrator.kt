package io.github.balaelangovan.orders.app.orchestrator

import io.github.balaelangovan.orders.app.dto.request.CreateOrderRequest
import io.github.balaelangovan.orders.app.dto.request.UpdateOrderRequest
import io.github.balaelangovan.orders.app.dto.response.OrderResponse
import io.github.balaelangovan.orders.app.mapper.RequestMapper
import io.github.balaelangovan.orders.app.mapper.ResponseMapper
import io.github.balaelangovan.orders.domain.service.OrderDomainService
import io.github.balaelangovan.orders.domain.valueobject.CustomerId
import io.github.balaelangovan.orders.domain.valueobject.OrderId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Application orchestrator handling DTO mapping between REST layer and domain.
 * This is a thin layer that:
 * 1. Converts request DTOs to domain objects
 * 2. Delegates business operations to OrderDomainService
 * 3. Converts domain objects back to response DTOs
 *
 * All business logic is in the domain service, not here.
 */
@Service
@Transactional
class OrderOrchestrator(
    private val domainService: OrderDomainService,
    private val requestMapper: RequestMapper,
    private val responseMapper: ResponseMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates a new order from request DTO.
     *
     * @param request the order creation request
     * @return the created order as response DTO
     */
    suspend fun createOrder(request: CreateOrderRequest): OrderResponse {
        logger.info("Creating order for customer: {}", request.customerId)

        val order = requestMapper.toDomain(request)
        val savedOrder = domainService.createOrder(order)

        return responseMapper.toResponse(savedOrder)
    }

    /**
     * Retrieves an order by ID with its releases and shipments.
     *
     * @param id the order identifier string
     * @return the order as response DTO with releases and shipments
     */
    @Transactional(readOnly = true)
    suspend fun getOrderById(id: String): OrderResponse {
        logger.debug("Getting order: {}", id)

        val orderId = OrderId.of(id)
        val order = domainService.getOrderById(orderId)
        val releases = domainService.getReleasesForOrder(id)
        val shipments = domainService.getShipmentsForOrder(id)

        return responseMapper.toResponse(order, releases, shipments)
    }

    /**
     * Retrieves orders with optional customer filter and pagination.
     *
     * @param customerId optional customer identifier to filter by
     * @param page zero-based page number
     * @param size number of orders per page
     * @return list of orders as response DTOs
     */
    @Transactional(readOnly = true)
    suspend fun getOrders(customerId: String?, page: Int, size: Int): List<OrderResponse> {
        logger.debug("Getting orders - customerId: {}, page: {}, size: {}", customerId, page, size)

        val orders = if (customerId != null) {
            domainService.getOrdersByCustomerId(CustomerId.of(customerId))
        } else {
            domainService.getAllOrders(page, size)
        }

        return responseMapper.toResponseList(orders)
    }

    /**
     * Updates order notes and/or billing address.
     *
     * @param id the order identifier string
     * @param request the update request containing optional notes and billing address
     * @return the updated order as response DTO
     */
    suspend fun updateOrder(id: String, request: UpdateOrderRequest): OrderResponse {
        logger.info("Updating order: {}", id)

        val orderId = OrderId.of(id)
        val billingAddress = request.billingAddress?.let { requestMapper.toAddress(it) }
        val order = domainService.updateOrder(orderId, request.notes, billingAddress)

        return responseMapper.toResponse(order)
    }

    /**
     * Cancels an order.
     *
     * @param id the order identifier string
     * @return the canceled order as response DTO
     */
    suspend fun cancelOrder(id: String): OrderResponse {
        logger.info("Cancelling order: {}", id)

        val orderId = OrderId.of(id)
        val order = domainService.cancelOrder(orderId)

        return responseMapper.toResponse(order)
    }

    /**
     * Soft-deletes an order.
     *
     * @param id the order identifier string
     */
    suspend fun deleteOrder(id: String) {
        logger.info("Deleting order: {}", id)

        val orderId = OrderId.of(id)
        domainService.deleteOrder(orderId)
    }
}
