package io.github.balaelangovan.orders.infra.generator

import io.github.balaelangovan.orders.domain.valueobject.Channel
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Generates business order IDs in the format: {channel}-{YYYYMMDD}-{sequence}
 * Example: 10-20251225-0000001
 *
 * Structure:
 * - Channel prefix (2 digits): 10=Web, 20=Mobile, 30=API, 40=POS, 50=CallCenter
 * - Date (8 digits): YYYYMMDD
 * - Sequence (7 digits): Daily counter per channel (resets daily)
 *
 * Total: 19 characters (similar to Amazon's order ID format)
 */
@Component
class OrderIdGenerator {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    // Map of "channelPrefix-date" -> sequence counter
    private val sequences = ConcurrentHashMap<String, AtomicLong>()

    /**
     * Generates the next order ID for the given channel.
     *
     * @param channel The channel through which the order was placed
     * @return Order ID in format: {channel}-{YYYYMMDD}-{sequence}
     */
    fun generateOrderId(channel: Channel): String {
        val today = LocalDate.now()
        val datePart = today.format(dateFormatter)
        val key = "${channel.prefix}-$datePart"

        // Get or create sequence for this channel-date combination
        val sequence = sequences.computeIfAbsent(key) { AtomicLong(0) }
        val nextSequence = sequence.incrementAndGet()

        // Format: channel-YYYYMMDD-sequence
        return String.format("%02d-%s-%07d", channel.prefix, datePart, nextSequence)
    }

    /**
     * Parses an order ID to extract channel, date, and sequence.
     */
    fun parseOrderId(orderId: String): OrderIdComponents {
        val parts = orderId.split("-")
        require(parts.size == 3) { "Invalid order ID format: $orderId" }

        val channelPrefix = parts[0].toInt()
        val channel = Channel.fromPrefix(channelPrefix)
        val date = LocalDate.parse(parts[1], dateFormatter)
        val sequence = parts[2].toLong()

        return OrderIdComponents(channel, date, sequence)
    }

    data class OrderIdComponents(val channel: Channel, val date: LocalDate, val sequence: Long)
}
