package com.erxes.messenger.data.model

/** A conversation thread. Mirrors `Models/Conversation.swift` (iOS). */
data class Conversation(
    val id: String,
    val content: String?,
    /** Epoch milliseconds. */
    val createdAt: Long,
    val participatedUsers: List<ParticipatedUser> = emptyList(),
    val messages: List<Message> = emptyList(),
    /** Server-provided unread count when present (list query); else derived from [messages]. */
    private val serverUnreadCount: Int? = null,
) {
    val lastMessage: Message? get() = messages.lastOrNull()

    /** Unread = agent messages the customer hasn't read. Falls back to the server count. */
    val unreadCount: Int
        get() = serverUnreadCount ?: messages.count { !it.isFromCustomer }
}

/** A participant (agent) in a conversation. */
data class ParticipatedUser(
    val id: String,
    val details: UserDetails? = null,
    val isOnline: Boolean = false,
)
