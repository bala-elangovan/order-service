package io.github.balaelangovan.orders.app.controller

import io.github.balaelangovan.orders.app.dto.request.CreateOrderRequest
import io.github.balaelangovan.orders.app.dto.request.UpdateOrderRequest
import io.github.balaelangovan.orders.app.dto.response.OrderResponse
import io.github.balaelangovan.orders.app.orchestrator.OrderOrchestrator
import io.github.balaelangovan.spring.core.annotation.BaseAuthController
import io.github.balaelangovan.spring.core.annotation.BaseErrorResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * REST controller for order management operations.
 * Provides endpoints for creating, reading, updating, and cancelling orders.
 * Delegates all operations to the OrderOrchestrator, which handles DTO mapping
 * and coordinates with the domain service.
 */
@BaseAuthController
@BaseErrorResponse
@RequestMapping
class OrderController(private val orchestrator: OrderOrchestrator) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Creates a new order from the request payload.
     *
     * @param request the order creation request containing customer and line items
     * @return ResponseEntity with the created order and HTTP 201 status
     */
    @PostMapping
    suspend fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        logger.info("Creating order for customer: {}", request.customerId)
        val response = orchestrator.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Retrieves a single order by its identifier.
     *
     * @param id the order identifier
     * @return ResponseEntity with the order details
     */
    @GetMapping("/{id}")
    suspend fun getOrder(@PathVariable id: String): ResponseEntity<OrderResponse> {
        logger.debug("Getting order: {}", id)
        val response = orchestrator.getOrderById(id)
        return ResponseEntity.ok(response)
    }

    /**
     * Retrieves orders with optional customer filter and pagination.
     *
     * @param customerId optional customer identifier to filter by
     * @param page zero-based page number
     * @param size number of orders per page
     * @return ResponseEntity with list of orders
     */
    @GetMapping
    suspend fun getOrders(
        @RequestParam(required = false) customerId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<List<OrderResponse>> {
        logger.debug("Getting orders - customerId: {}, page: {}, size: {}", customerId, page, size)
        val response = orchestrator.getOrders(customerId, page, size)
        return ResponseEntity.ok(response)
    }

    /**
     * Partially updates an order's notes and/or billing address.
     *
     * @param id the order identifier
     * @param request the update request containing optional notes and billing address
     * @return ResponseEntity with the updated order
     */
    @PatchMapping("/{id}")
    suspend fun updateOrder(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateOrderRequest,
    ): ResponseEntity<OrderResponse> {
        logger.info("Updating order: {}", id)
        val response = orchestrator.updateOrder(id, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Cancels an order by transitioning it to CANCELLED status.
     *
     * @param id the order identifier
     * @return ResponseEntity with the canceled order
     */
    @PostMapping("/{id}/cancel")
    suspend fun cancelOrder(@PathVariable id: String): ResponseEntity<OrderResponse> {
        logger.info("Cancelling order: {}", id)
        val response = orchestrator.cancelOrder(id)
        return ResponseEntity.ok(response)
    }

    /**
     * Soft-deletes an order by marking it as canceled.
     *
     * @param id the order identifier
     * @return ResponseEntity with HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    suspend fun deleteOrder(@PathVariable id: String): ResponseEntity<Unit> {
        logger.info("Deleting order: {}", id)
        orchestrator.deleteOrder(id)
        return ResponseEntity.noContent().build()
    }
}
