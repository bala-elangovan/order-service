package io.github.balaelangovan.orders.infra.generator

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

/**
 * Generates timestamp-based IDs in the format: YYYYMMDDHHmmssSSSNN
 * Example: 2025122518234567801
 *
 * Structure:
 * - YYYYMMDD: Date (8 digits)
 * - HHmmss: Time (6 digits)
 * - SSS: Milliseconds (3 digits)
 * - NN: Sequence within millisecond (2 digits, 00-99)
 *
 * Total: 19 digits (fits in Long.MAX_VALUE = 9,223,372,036,854,775,807)
 */
class TimestampIdGenerator : IdentifierGenerator {

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("UTC"))

        private val sequence = AtomicInteger(0)
        private var lastTimestamp = 0L
        private val lock = Any()
    }

    override fun generate(session: SharedSessionContractImplementor?, entity: Any?): Any {
        val now = Instant.now()
        val timestamp = now.toEpochMilli()

        val seq: Int
        synchronized(lock) {
            if (timestamp != lastTimestamp) {
                sequence.set(0)
                lastTimestamp = timestamp
            }
            seq = sequence.getAndIncrement() % 100
        }

        // Format: YYYYMMDDHHmmss (14 digits)
        val dateTimePart = formatter.format(now)

        // SSS: milliseconds (3 digits)
        val millisPart = String.format("%03d", timestamp % 1000)

        // NN: sequence (2 digits)
        val seqPart = String.format("%02d", seq)

        return "$dateTimePart$millisPart$seqPart".toLong()
    }
}
