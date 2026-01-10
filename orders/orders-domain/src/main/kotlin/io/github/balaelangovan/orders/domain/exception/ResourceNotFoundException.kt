package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * @param resource the type of resource (e.g., "Order", "OrderLine")
 * @param field the field used for lookup (e.g., "id", "orderId")
 * @param identifier the identifier value that was not found
 * @param errorCode the specific error code (defaults to RESOURCE_NOT_FOUND)
 */
open class ResourceNotFoundException(
    val resource: String,
    val field: String,
    val identifier: Any,
    errorCode: ErrorCode = ErrorCode.RESOURCE_NOT_FOUND,
) : DomainException(
    errorCode = errorCode,
    message = "$resource with $field '$identifier' not found",
)
