package com.erxes.messenger.network

import com.erxes.messenger.data.model.Attachment
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.data.model.MessageUser
import com.erxes.messenger.data.model.ParticipatedUser
import com.erxes.messenger.data.model.UserDetails
import com.erxes.messenger.util.DateParsing
import kotlinx.serialization.json.JsonObject

/**
 * Parses conversation and message payloads. Pure functions (no I/O) so they are
 * unit-testable against captured JSON. Mirrors the decoding in `ChatViewModel` /
 * `ConversationListViewModel` (iOS).
 */
object MessageParser {

    fun parseMessage(json: JsonObject): Message? {
        val id = json.str("_id") ?: return null
        val customerId = json.str("customerId")
        return Message(
            id = id,
            content = botContent(json),
            createdAt = DateParsing.toEpochMillis(json.str("createdAt")),
            isFromCustomer = customerId != null,
            attachments = (json.arr("attachments")).orEmpty().mapNotNull { el ->
                val a = el as? JsonObject ?: return@mapNotNull null
                val url = a.str("url") ?: return@mapNotNull null
                Attachment(url = url, type = a.str("type"), name = a.str("name"), size = a.int("size"))
            },
            fromBot = json.bool("fromBot") ?: false,
            customerId = customerId,
            conversationId = json.str("conversationId"),
            isCustomerRead = json.bool("isCustomerRead") ?: false,
            user = parseUser(json.obj("user")),
        )
    }

    /**
     * Message text, falling back to `botData` when `content` is empty. Bot messages arrive
     * with `content: null` and carry their text in a `botData` array of `{ type, text }`
     * entries, so join those so they render in chat and list previews.
     */
    private fun botContent(json: JsonObject): String {
        val content = json.str("content").orEmpty()
        if (content.isNotEmpty()) return content
        return json.arr("botData").orEmpty()
            .mapNotNull { (it as? JsonObject)?.str("text") }
            .joinToString("\n")
    }

    fun parseConversation(json: JsonObject): Conversation? {
        val id = json.str("_id") ?: return null
        return Conversation(
            id = id,
            content = json.str("content"),
            createdAt = DateParsing.toEpochMillis(json.str("createdAt")),
            participatedUsers = (json.arr("participatedUsers")).orEmpty().mapNotNull { el ->
                val u = el as? JsonObject ?: return@mapNotNull null
                ParticipatedUser(
                    id = u.str("_id") ?: return@mapNotNull null,
                    details = parseDetails(u.obj("details")),
                    isOnline = u.bool("isOnline") ?: false,
                )
            },
            messages = (json.arr("messages")).orEmpty().mapNotNull { (it as? JsonObject)?.let(::parseMessage) },
        )
    }

    fun parseConversations(array: List<JsonObject>): List<Conversation> =
        array.mapNotNull(::parseConversation)

    private fun parseUser(json: JsonObject?): MessageUser? {
        val id = json?.str("_id") ?: return null
        return MessageUser(id = id, isOnline = json.bool("isOnline"), details = parseDetails(json.obj("details")))
    }

    private fun parseDetails(json: JsonObject?): UserDetails? {
        if (json == null) return null
        return UserDetails(avatar = json.str("avatar"), fullName = json.str("fullName"))
    }
}
