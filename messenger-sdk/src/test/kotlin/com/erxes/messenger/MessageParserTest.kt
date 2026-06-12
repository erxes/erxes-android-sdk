package com.erxes.messenger

import com.erxes.messenger.network.MessageParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageParserTest {

    private fun obj(text: String) = Json.parseToJsonElement(text) as JsonObject

    @Test
    fun `customer message is flagged from customer`() {
        val msg = MessageParser.parseMessage(
            obj(
                """
                { "_id": "m1", "content": "hello", "createdAt": "1700000000000",
                  "customerId": "c1", "conversationId": "conv1" }
                """.trimIndent()
            )
        )!!
        assertEquals("m1", msg.id)
        assertEquals("hello", msg.content)
        assertEquals(1700000000000L, msg.createdAt)
        assertTrue(msg.isFromCustomer)
        assertNull(msg.user)
    }

    @Test
    fun `agent message parses user details and attachments`() {
        val msg = MessageParser.parseMessage(
            obj(
                """
                { "_id": "m2", "content": "hi", "createdAt": "2023-01-02T03:04:05.000Z",
                  "fromBot": false,
                  "attachments": [{ "url": "a.png", "name": "a", "type": "image/png", "size": 12 }],
                  "user": { "_id": "u1", "details": { "fullName": "Jane", "avatar": "j.png" } } }
                """.trimIndent()
            )
        )!!
        assertFalse(msg.isFromCustomer)            // no customerId
        assertEquals("Jane", msg.user?.details?.fullName)
        assertEquals(1, msg.attachments.size)
        assertEquals("a.png", msg.attachments[0].url)
        assertEquals("a.png", msg.attachments[0].id)   // id defaults to url
    }

    @Test
    fun `conversation derives unread from server count`() {
        val conv = MessageParser.parseConversation(
            obj(
                """
                { "_id": "conv1", "content": "last", "createdAt": "1700000000000",
                  "unreadCount": 3,
                  "messages": [
                    { "_id": "a", "content": "x", "createdAt": "1", "customerId": "c1" },
                    { "_id": "b", "content": "y", "createdAt": "2", "user": { "_id": "u1" } }
                  ] }
                """.trimIndent()
            )
        )!!
        assertEquals("conv1", conv.id)
        assertEquals(2, conv.messages.size)
        assertEquals(3, conv.unreadCount)             // server-provided wins
        assertEquals("b", conv.lastMessage?.id)
    }

    @Test
    fun `conversation derives unread from agent messages when server count absent`() {
        val conv = MessageParser.parseConversation(
            obj(
                """
                { "_id": "conv2", "createdAt": "1",
                  "messages": [
                    { "_id": "a", "content": "x", "createdAt": "1", "customerId": "c1" },
                    { "_id": "b", "content": "y", "createdAt": "2", "user": { "_id": "u1" } }
                  ] }
                """.trimIndent()
            )
        )!!
        assertEquals(1, conv.unreadCount)             // one agent message, not read-tracked
    }
}
