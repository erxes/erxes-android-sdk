package com.erxes.messenger.data.model

/** A support ticket. Mirrors `Models/Ticket.swift` (iOS), trimmed to fields the UI uses. */
data class Ticket(
    val id: String,
    val name: String,
    val description: String?,
    val statusId: String?,
    val priority: String?,
    val number: Int?,
    val tagIds: List<String> = emptyList(),
    val createdAt: Long,
    val status: TicketStatus?,
    val assignee: TicketAssignee?,
)

data class TicketStatus(
    val id: String,
    val name: String,
    val color: String?,
    val type: String?,
)

data class TicketAssignee(
    val id: String,
    val avatar: String?,
    val displayName: String?,
)

/** A selectable ticket tag (`widgetsGetTicketTags`). */
data class TicketTag(
    val id: String,
    val name: String,
    val colorCode: String?,
)
