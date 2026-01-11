package io.github.balaelangovan.orders.domain.exception

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class DomainExceptionTest :
    DescribeSpec({

        describe("ErrorCode") {
            it("should have correct codes for validation errors") {
                ErrorCode.VALIDATION_FAILED.code shouldBe "ORDERS.VALIDATION_FAILED"
                ErrorCode.VALIDATION_FAILED.httpStatus shouldBe 400
                ErrorCode.VALIDATION_FAILED.defaultMessage shouldBe "Validation failed"

                ErrorCode.INVALID_ORDER.code shouldBe "ORDERS.INVALID_ORDER"
                ErrorCode.INVALID_ORDER.httpStatus shouldBe 400
            }

            it("should have correct codes for not found errors") {
                ErrorCode.RESOURCE_NOT_FOUND.code shouldBe "ORDERS.RESOURCE_NOT_FOUND"
                ErrorCode.RESOURCE_NOT_FOUND.httpStatus shouldBe 404

                ErrorCode.ORDER_NOT_FOUND.code shouldBe "ORDERS.ORDER_NOT_FOUND"
                ErrorCode.ORDER_NOT_FOUND.httpStatus shouldBe 404
            }

            it("should have correct codes for conflict errors") {
                ErrorCode.CONFLICT.code shouldBe "ORDERS.CONFLICT"
                ErrorCode.CONFLICT.httpStatus shouldBe 409

                ErrorCode.INVALID_STATE_TRANSITION.code shouldBe "ORDERS.INVALID_STATE_TRANSITION"
                ErrorCode.INVALID_STATE_TRANSITION.httpStatus shouldBe 409

                ErrorCode.DUPLICATE_ORDER.code shouldBe "ORDERS.DUPLICATE_ORDER"
                ErrorCode.DUPLICATE_ORDER.httpStatus shouldBe 409
            }

            it("should have correct codes for processing errors") {
                ErrorCode.PROCESSING_FAILED.code shouldBe "ORDERS.PROCESSING_FAILED"
                ErrorCode.PROCESSING_FAILED.httpStatus shouldBe 500

                ErrorCode.EVENT_PROCESSING_FAILED.code shouldBe "ORDERS.EVENT_PROCESSING_FAILED"
                ErrorCode.EVENT_PROCESSING_FAILED.httpStatus shouldBe 500
            }

            it("should have correct codes for persistence errors") {
                ErrorCode.PERSISTENCE_FAILED.code shouldBe "ORDERS.PERSISTENCE_FAILED"
                ErrorCode.PERSISTENCE_FAILED.httpStatus shouldBe 500
            }
        }

        describe("ValidationException") {
            it("should create with message and default error code") {
                val exception = ValidationException("Field is invalid")

                exception.message shouldBe "Field is invalid"
                exception.errorCode shouldBe ErrorCode.VALIDATION_FAILED
                exception.httpStatus shouldBe 400
                exception.code shouldBe "ORDERS.VALIDATION_FAILED"
            }

            it("should create with custom error code") {
                val exception = ValidationException("Invalid order", ErrorCode.INVALID_ORDER)

                exception.message shouldBe "Invalid order"
                exception.errorCode shouldBe ErrorCode.INVALID_ORDER
            }

            it("should create with cause") {
                val cause = RuntimeException("Root cause")
                val exception = ValidationException("Validation failed", cause = cause)

                exception.cause shouldBe cause
            }

            it("should be a DomainException") {
                val exception = ValidationException("Test")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }

        describe("InvalidStateTransitionException") {
            it("should create with message") {
                val exception = InvalidStateTransitionException(
                    "Cannot transition from CREATED to DELIVERED",
                )

                exception.message shouldBe "Cannot transition from CREATED to DELIVERED"
                exception.errorCode shouldBe ErrorCode.INVALID_STATE_TRANSITION
                exception.httpStatus shouldBe 409
            }

            it("should be a DomainException") {
                val exception = InvalidStateTransitionException("Test")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }

        describe("ResourceNotFoundException") {
            it("should create with resource details") {
                val exception = ResourceNotFoundException(
                    resource = "Order",
                    field = "id",
                    identifier = "12345",
                )

                exception.message shouldBe "Order with id '12345' not found"
                exception.resource shouldBe "Order"
                exception.field shouldBe "id"
                exception.identifier shouldBe "12345"
                exception.errorCode shouldBe ErrorCode.RESOURCE_NOT_FOUND
                exception.httpStatus shouldBe 404
            }

            it("should create with custom error code") {
                val exception = ResourceNotFoundException(
                    resource = "Order",
                    field = "id",
                    identifier = "12345",
                    errorCode = ErrorCode.ORDER_NOT_FOUND,
                )

                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
            }

            it("should be a DomainException") {
                val exception = ResourceNotFoundException("Order", "id", "123")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }

        describe("ConflictException") {
            it("should create with message") {
                val exception = ConflictException("Resource already exists")

                exception.message shouldBe "Resource already exists"
                exception.errorCode shouldBe ErrorCode.CONFLICT
                exception.httpStatus shouldBe 409
            }

            it("should create with custom error code") {
                val exception = ConflictException(
                    "Order already exists",
                    ErrorCode.DUPLICATE_ORDER,
                )

                exception.errorCode shouldBe ErrorCode.DUPLICATE_ORDER
            }

            it("should create with cause") {
                val cause = RuntimeException("Root cause")
                val exception = ConflictException("Conflict", cause = cause)

                exception.cause shouldBe cause
            }

            it("should be a DomainException") {
                val exception = ConflictException("Test")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }

        describe("ProcessingException") {
            it("should create with message") {
                val exception = ProcessingException("Processing failed")

                exception.message shouldBe "Processing failed"
                exception.errorCode shouldBe ErrorCode.PROCESSING_FAILED
                exception.httpStatus shouldBe 500
            }

            it("should create with custom error code") {
                val exception = ProcessingException(
                    "Event processing failed",
                    ErrorCode.EVENT_PROCESSING_FAILED,
                )

                exception.errorCode shouldBe ErrorCode.EVENT_PROCESSING_FAILED
            }

            it("should create with cause") {
                val cause = RuntimeException("Root cause")
                val exception = ProcessingException("Failed", cause = cause)

                exception.cause shouldBe cause
            }

            it("should be a DomainException") {
                val exception = ProcessingException("Test")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }

        describe("DataPersistenceException") {
            it("should create with message") {
                val exception = DataPersistenceException("Database error")

                exception.message shouldBe "Database error"
                exception.errorCode shouldBe ErrorCode.PERSISTENCE_FAILED
                exception.httpStatus shouldBe 500
            }

            it("should create with cause") {
                val cause = RuntimeException("Database connection failed")
                val exception = DataPersistenceException("Failed to save", cause = cause)

                exception.cause shouldBe cause
            }

            it("should be a DomainException") {
                val exception = DataPersistenceException("Test")

                exception.shouldBeInstanceOf<DomainException>()
            }
        }
    })
