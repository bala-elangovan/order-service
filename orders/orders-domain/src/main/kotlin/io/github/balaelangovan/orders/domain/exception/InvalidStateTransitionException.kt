package io.github.balaelangovan.orders.domain.exception

/**
 * Exception thrown when attempting an invalid order status transition.
 *
 * @param message the state transition error message
 */
class InvalidStateTransitionException(message: String) :
    DomainException(
        message = message,
        errorCode = ErrorCode.INVALID_STATE_TRANSITION,
    )
