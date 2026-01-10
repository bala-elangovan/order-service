package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when processing fails (e.g., Kafka event processing).
 *
 * @param message the processing error message
 * @param errorCode the specific error code (defaults to PROCESSING_FAILED)
 * @param cause the underlying cause
 */
open class ProcessingException(
    message: String,
    errorCode: ErrorCode = ErrorCode.PROCESSING_FAILED,
    cause: Throwable? = null,
) : DomainException(
    errorCode = errorCode,
    message = message,
    cause = cause,
)
