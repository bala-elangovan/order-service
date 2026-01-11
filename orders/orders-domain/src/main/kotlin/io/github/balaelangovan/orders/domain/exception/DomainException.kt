package io.github.balaelangovan.orders.domain.exception

/**
 * Base exception for all domain-level exceptions.
 * Provides error code support for consistent error handling across the application.
 *
 * @param errorCode the error code identifying the type of error
 * @param message the detailed error message
 * @param cause the underlying cause of the exception
 */
abstract class DomainException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.defaultMessage,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {

    /**
     * @return the HTTP status code associated with this exception
     */
    val httpStatus: Int
        get() = errorCode.httpStatus

    /**
     * @return the error code string
     */
    val code: String
        get() = errorCode.code
}
