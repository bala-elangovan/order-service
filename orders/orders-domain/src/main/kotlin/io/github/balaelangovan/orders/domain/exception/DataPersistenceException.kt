package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when data persistence operations fail.
 *
 * @param message the persistence error message
 * @param errorCode the specific error code (defaults to PERSISTENCE_FAILED)
 * @param cause the underlying cause
 */
open class DataPersistenceException(
    message: String,
    errorCode: ErrorCode = ErrorCode.PERSISTENCE_FAILED,
    cause: Throwable? = null,
) : DomainException(
    errorCode = errorCode,
    message = message,
    cause = cause,
)
