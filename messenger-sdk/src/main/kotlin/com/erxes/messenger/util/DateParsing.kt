package com.erxes.messenger.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Parses erxes `createdAt` values into epoch milliseconds. The backend may send either
 * a numeric epoch-millis string or an ISO-8601 timestamp. Uses [SimpleDateFormat] rather
 * than `java.time` so the SDK does not require core-library desugaring from consumers.
 * Mirrors `Utils/DateParsing.swift` (iOS).
 */
object DateParsing {

    private val isoPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
    )

    /** Returns epoch millis, or 0 when [raw] is null/blank/unparseable. */
    fun toEpochMillis(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        // Numeric epoch millis (erxes often serializes Date as a millis string).
        raw.toLongOrNull()?.let { return it }
        for (pattern in isoPatterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) fmt.timeZone = TimeZone.getTimeZone("UTC")
                return fmt.parse(raw)?.time ?: continue
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return 0L
    }
}
