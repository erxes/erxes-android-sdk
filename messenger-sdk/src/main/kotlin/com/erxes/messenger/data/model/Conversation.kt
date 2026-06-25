package com.erxes.messenger.data.model

/** A conversation thread. Mirrors `Models/Conversation.swift` (iOS). */
data class Conversation(
    val id: String,
    val content: String?,
    /** Epoch milliseconds. */
    val createdAt: Long,
    val participatedUsers: List<ParticipatedUser> = emptyList(),
    val messages: List<Message> = emptyList(),
) {
    val lastMessage: Message? get() = messages.lastOrNull()

    /**
     * Time used to order the "Recent" list: the newest message's time, or the conversation's
     * creation time when it has no messages yet. Sorting by this (not [createdAt]) keeps a
     * freshly-replied old conversation above a newer but idle one.
     */
    val lastActivityAt: Long get() = lastMessage?.createdAt ?: createdAt
}

/** A participant (agent) in a conversation. */
data class ParticipatedUser(
    val id: String,
    val details: UserDetails? = null,
    val isOnline: Boolean = false,
)
