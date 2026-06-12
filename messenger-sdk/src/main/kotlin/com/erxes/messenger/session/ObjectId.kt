package com.erxes.messenger.session

import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * Generates a MongoDB-style ObjectId (24 hex chars) used as the anonymous `visitorId`.
 * Layout: 4-byte epoch seconds + 5-byte random + 3-byte incrementing counter.
 * Mirrors `Utils/ObjectId.swift` in the iOS SDK.
 */
object ObjectId {
    private val random = SecureRandom()

    // 5-byte per-process random value, fixed for the lifetime of the process.
    private val randomValue: Long = (random.nextLong() and 0xFF_FFFF_FFFFL)

    private val counter = AtomicInteger(random.nextInt())

    fun generate(): String {
        val timestamp = (System.currentTimeMillis() / 1000L) and 0xFFFFFFFFL
        val count = counter.getAndIncrement() and 0xFFFFFF

        val sb = StringBuilder(24)
        // 4 bytes timestamp
        sb.append(hex(timestamp, 8))
        // 5 bytes random
        sb.append(hex(randomValue, 10))
        // 3 bytes counter
        sb.append(hex(count.toLong(), 6))
        return sb.toString()
    }

    private fun hex(value: Long, width: Int): String {
        val s = java.lang.Long.toHexString(value)
        return if (s.length >= width) s.substring(s.length - width) else "0".repeat(width - s.length) + s
    }
}
