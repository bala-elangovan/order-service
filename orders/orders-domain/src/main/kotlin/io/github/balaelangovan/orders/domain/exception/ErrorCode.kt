package io.github.balaelangovan.orders.domain.exception

/**
 * Enumeration of error codes for domain exceptions.
 * Each code maps to an HTTP status and provides a default message.
 *
 * @param code the unique error code identifier
 * @param httpStatus the HTTP status code to return
 * @param defaultMessage the default error message
 */
enum class ErrorCode(val code: String, val httpStatus: Int, val defaultMessage: String) {
    // Validation errors (400)
    VALIDATION_FAILED("ORDERS.VALIDATION_FAILED", 400, "Validation failed"),
    INVALID_ORDER("ORDERS.INVALID_ORDER", 400, "Invalid order"),

    // Not found errors (404)
    RESOURCE_NOT_FOUND("ORDERS.RESOURCE_NOT_FOUND", 404, "Resource not found"),
    ORDER_NOT_FOUND("ORDERS.ORDER_NOT_FOUND", 404, "Order not found"),

    // Conflict errors (409)
    CONFLICT("ORDERS.CONFLICT", 409, "Conflict occurred"),
    INVALID_STATE_TRANSITION("ORDERS.INVALID_STATE_TRANSITION", 409, "Invalid state transition"),
    DUPLICATE_ORDER("ORDERS.DUPLICATE_ORDER", 409, "Order already exists"),

    // Processing errors (500)
    PROCESSING_FAILED("ORDERS.PROCESSING_FAILED", 500, "Processing failed"),
    EVENT_PROCESSING_FAILED("ORDERS.EVENT_PROCESSING_FAILED", 500, "Event processing failed"),

    // Persistence errors (500)
    PERSISTENCE_FAILED("ORDERS.PERSISTENCE_FAILED", 500, "Data persistence failed"),
}
