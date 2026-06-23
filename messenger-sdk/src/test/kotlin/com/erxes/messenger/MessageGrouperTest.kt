package com.erxes.messenger

import com.erxes.messenger.data.model.Attachment
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.util.ChatRow
import com.erxes.messenger.util.MessageGrouper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageGrouperTest {

    // 2023-11-14T22:13:20Z, well-defined and stable across runs.
    private val base = 1_700_000_000_000L
    private val oneDay = 24 * 60 * 60 * 1000L

    private fun msg(
        id: String,
        at: Long,
        fromCustomer: Boolean = true,
        content: String = "hi",
        attachments: List<Attachment> = emptyList(),
    ) = Message(
        id = id,
        content = content,
        createdAt = at,
        isFromCustomer = fromCustomer,
        attachments = attachments,
        customerId = if (fromCustomer) "c1" else null,
    )

    private fun rows(list: List<Message>) =
        MessageGrouper.buildChatRows(list).filterIsInstance<ChatRow.MessageRow>()

    private fun separators(list: List<Message>) =
        MessageGrouper.buildChatRows(list).filterIsInstance<ChatRow.DateSeparator>()

    @Test
    fun `same sender within window forms one group`() {
        val rows = rows(
            listOf(
                msg("a", base),
                msg("b", base + 60_000),         // +1 min
                msg("c", base + 2 * 60_000),     // +2 min
            )
        )
        assertEquals(3, rows.size)
        assertTrue(rows[0].isFirstInGroup)
        assertFalse(rows[0].isLastInGroup)
        assertFalse(rows[1].isFirstInGroup)
        assertFalse(rows[1].isLastInGroup)
        assertFalse(rows[2].isFirstInGroup)
        assertTrue(rows[2].isLastInGroup)
    }

    @Test
    fun `gap over five minutes breaks the group`() {
        val rows = rows(
            listOf(
                msg("a", base),
                msg("b", base + 6 * 60_000), // +6 min > 5-min window
            )
        )
        assertTrue(rows[0].isFirstInGroup && rows[0].isLastInGroup)
        assertTrue(rows[1].isFirstInGroup && rows[1].isLastInGroup)
    }

    @Test
    fun `sender change breaks the group`() {
        val rows = rows(
            listOf(
                msg("a", base, fromCustomer = true),
                msg("b", base + 30_000, fromCustomer = false),
            )
        )
        assertTrue(rows[0].isLastInGroup)
        assertTrue(rows[1].isFirstInGroup)
    }

    @Test
    fun `day change inserts a date separator`() {
        val all = MessageGrouper.buildChatRows(
            listOf(
                msg("a", base),
                msg("b", base + oneDay),
            )
        )
        assertEquals(2, separators(listOf(msg("a", base), msg("b", base + oneDay))).size)
        // Order: separator, message, separator, message
        assertTrue(all[0] is ChatRow.DateSeparator)
        assertTrue(all[1] is ChatRow.MessageRow)
        assertTrue(all[2] is ChatRow.DateSeparator)
        assertTrue(all[3] is ChatRow.MessageRow)
    }

    @Test
    fun `attachment-only message is retained, empty message is dropped`() {
        val rows = rows(
            listOf(
                msg("a", base, content = "", attachments = listOf(Attachment(url = "img.png", type = "image/png"))),
                msg("b", base + 1000, content = ""), // no content, no attachments → dropped
            )
        )
        assertEquals(1, rows.size)
        assertEquals("a", rows[0].message.id)
    }

    @Test
    fun `empty input yields no rows`() {
        assertTrue(MessageGrouper.buildChatRows(emptyList()).isEmpty())
    }
}
