package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when an operation conflicts with the current state.
 * Used for state transition errors, duplicate resources, or concurrent modification issues.
 *
 * @param message the conflict error message
 * @param errorCode the specific error code (defaults to CONFLICT)
 * @param cause the underlying cause
 */
open class ConflictException(message: String, errorCode: ErrorCode = ErrorCode.CONFLICT, cause: Throwable? = null) :
    DomainException(
        errorCode = errorCode,
        message = message,
        cause = cause,
    )
