package io.github.balaelangovan.orders.app.controller

import io.github.balaelangovan.orders.app.dto.request.CreateOrderRequest
import io.github.balaelangovan.orders.app.dto.request.UpdateOrderRequest
import io.github.balaelangovan.orders.app.dto.response.OrderResponse
import io.github.balaelangovan.orders.app.orchestrator.OrderOrchestrator
import io.github.balaelangovan.spring.core.annotation.BaseAuthController
import io.github.balaelangovan.spring.core.annotation.BaseErrorResponse
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Orders", description = "Order management API for creating, retrieving, updating, and cancelling orders")
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
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with customer details, line items, and billing address",
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Order created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request payload"),
        ApiResponse(responseCode = "409", description = "Duplicate order (external order ID already exists)"),
    )
    @Timed(value = "orders.create", description = "Time taken to create an order")
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
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves a single order by its unique identifier",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Order found"),
        ApiResponse(responseCode = "404", description = "Order not found"),
    )
    @Timed(value = "orders.get", description = "Time taken to get an order")
    @GetMapping("/{id}")
    suspend fun getOrder(
        @Parameter(description = "Order ID", example = "10-20250111-0000001")
        @PathVariable id: String,
    ): ResponseEntity<OrderResponse> {
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
    @Operation(
        summary = "List orders",
        description = "Retrieves a paginated list of orders with optional customer filter",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
    )
    @Timed(value = "orders.list", description = "Time taken to list orders")
    @GetMapping
    suspend fun getOrders(
        @Parameter(description = "Filter by customer ID", example = "CUST-001")
        @RequestParam(name = "customer_id", required = false) customerId: String?,
        @Parameter(description = "Page number (zero-based)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size", example = "20")
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
    @Operation(
        summary = "Update order",
        description = "Partially updates an order's notes and/or billing address",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Order updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid request payload"),
        ApiResponse(responseCode = "404", description = "Order not found"),
    )
    @Timed(value = "orders.update", description = "Time taken to update an order")
    @PatchMapping("/{id}")
    suspend fun updateOrder(
        @Parameter(description = "Order ID", example = "10-20250111-0000001")
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateOrderRequest,
    ): ResponseEntity<OrderResponse> {
        logger.info("Updating order: {}", id)
        val response = orchestrator.updateOrder(id, request)
        return ResponseEntity.ok(response)
    }

    /**
     * Cancels (soft-deletes) an order by transitioning it to CANCELLED status.
     *
     * @param id the order identifier
     * @return ResponseEntity with the cancelled order
     */
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order by transitioning it to CANCELLED status (soft-delete)",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        ApiResponse(responseCode = "400", description = "Order cannot be cancelled (invalid state transition)"),
        ApiResponse(responseCode = "404", description = "Order not found"),
    )
    @Timed(value = "orders.cancel", description = "Time taken to cancel an order")
    @DeleteMapping("/{id}")
    suspend fun deleteOrder(
        @Parameter(description = "Order ID", example = "10-20250111-0000001")
        @PathVariable id: String,
    ): ResponseEntity<OrderResponse> {
        logger.info("Cancelling order: {}", id)
        val response = orchestrator.cancelOrder(id)
        return ResponseEntity.ok(response)
    }
}