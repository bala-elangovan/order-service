package io.github.balaelangovan.orders.infra.annotation

import io.github.balaelangovan.orders.infra.generator.TimestampIdGenerator
import org.hibernate.annotations.IdGeneratorType

/**
 * Custom annotation for generating timestamp-based IDs.
 * Format: YYYYMMDDHHmmssSSS (17 digits)
 * Example: 20251225182345678
 *
 * This provides:
 * - Time-ordered IDs (better for database indexing)
 * - Uniqueness through timestamp precision + sequence
 * - Readable format showing when the record was created
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IdGeneratorType(TimestampIdGenerator::class)
annotation class TimestampId
