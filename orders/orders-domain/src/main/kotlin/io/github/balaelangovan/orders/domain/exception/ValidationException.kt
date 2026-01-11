package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when validation fails for domain objects or value objects.
 *
 * @param message the validation error message
 * @param errorCode the specific error code (defaults to VALIDATION_FAILED)
 * @param cause the underlying cause
 */
open class ValidationException(
    message: String,
    errorCode: ErrorCode = ErrorCode.VALIDATION_FAILED,
    cause: Throwable? = null,
) : DomainException(
    errorCode = errorCode,
    message = message,
    cause = cause,
)
