package com.erxes.messenger.util

import com.erxes.messenger.data.model.Message
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A single item in the flat chat list — either a date separator or a message.
 * Mirrors `ChatRow` (iOS `Utils/MessageGrouper.swift`).
 */
sealed interface ChatRow {
    val id: String

    data class DateSeparator(val label: String, val key: String) : ChatRow {
        override val id: String get() = "sep-$key"
    }

    data class MessageRow(
        val message: Message,
        val isFirstInGroup: Boolean,
        val isLastInGroup: Boolean,
    ) : ChatRow {
        override val id: String get() = message.id
    }
}

/**
 * Groups consecutive messages from the same sender within a 5-minute window and inserts
 * date separators when the day changes. Pure (no Android deps) so it is unit-testable.
 * Mirrors `MessageGrouper.buildChatRows` (iOS) / the RN SDK's `buildChatRows()`.
 *
 * Uses [Calendar]/[SimpleDateFormat] rather than `java.time` so the SDK doesn't require
 * core-library desugaring (same rationale as [DateParsing]).
 */
object MessageGrouper {

    /** Same-sender grouping window, in milliseconds. */
    private const val GROUPING_WINDOW_MS = 5 * 60 * 1000L

    fun buildChatRows(messages: List<Message>): List<ChatRow> {
        // Keep messages with text OR attachments — an attachment-only message has empty
        // content but must still render its image/file.
        val filtered = messages.filter { it.content.isNotEmpty() || it.attachments.isNotEmpty() }
        if (filtered.isEmpty()) return emptyList()

        val rows = ArrayList<ChatRow>(filtered.size + 4)
        var currentDayKey = ""

        for (index in filtered.indices) {
            val message = filtered[index]
            val dayKey = dayKey(message.createdAt)

            if (dayKey != currentDayKey) {
                currentDayKey = dayKey
                rows.add(ChatRow.DateSeparator(label = friendlyDateLabel(message.createdAt), key = dayKey))
            }

            val prev = filtered.getOrNull(index - 1)
            val next = filtered.getOrNull(index + 1)

            rows.add(
                ChatRow.MessageRow(
                    message = message,
                    isFirstInGroup = !sameGroup(message, prev),
                    isLastInGroup = !sameGroup(message, next),
                )
            )
        }
        return rows
    }

    private fun sameGroup(a: Message, b: Message?): Boolean {
        if (b == null) return false
        if (a.isFromCustomer != b.isFromCustomer) return false
        return kotlin.math.abs(a.createdAt - b.createdAt) <= GROUPING_WINDOW_MS
    }

    private val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun dayKey(epochMillis: Long): String = dayKeyFmt.format(Date(epochMillis))

    private fun friendlyDateLabel(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val now = Calendar.getInstance()
        if (isSameDay(cal, now)) return "Today"
        now.add(Calendar.DAY_OF_YEAR, -1)
        if (isSameDay(cal, now)) return "Yesterday"
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(epochMillis))
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
