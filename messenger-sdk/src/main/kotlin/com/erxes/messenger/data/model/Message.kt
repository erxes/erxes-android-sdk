package com.erxes.messenger.data.model

/** A single chat message. Mirrors `Models/Message.swift` (iOS). */
data class Message(
    val id: String,
    val content: String,
    /** Epoch milliseconds (see [com.erxes.messenger.util.DateParsing]). */
    val createdAt: Long,
    /** True when the message was sent by the customer/visitor (has a customerId). */
    val isFromCustomer: Boolean,
    val attachments: List<Attachment> = emptyList(),
    val fromBot: Boolean = false,
    val customerId: String? = null,
    val conversationId: String? = null,
    /** Agent who sent the message, if any. */
    val user: MessageUser? = null,
)

/** The agent author of a message. */
data class MessageUser(
    val id: String,
    val isOnline: Boolean? = null,
    val details: UserDetails? = null,
)

/** Shared user detail fragment. */
data class UserDetails(
    val avatar: String? = null,
    val fullName: String? = null,
) {
    val displayName: String get() = fullName ?: "Support"
}

/** A message attachment. `id` defaults to the url since the API does not supply one. */
data class Attachment(
    val url: String,
    val type: String? = null,
    val name: String? = null,
    val size: Int? = null,
) {
    val id: String get() = url
}
