package com.erxes.messenger.network

import com.erxes.messenger.data.model.Ticket
import com.erxes.messenger.data.model.TicketAssignee
import com.erxes.messenger.data.model.TicketStatus
import com.erxes.messenger.data.model.TicketTag
import com.erxes.messenger.util.DateParsing
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Pure parsing for tickets/tags. Mirrors `TicketListViewModel.parseTicket` (iOS). */
object TicketParser {

    fun parseTicket(json: JsonObject): Ticket? {
        val id = json.str("_id") ?: return null
        return Ticket(
            id = id,
            name = json.str("name").orEmpty(),
            description = json.str("description"),
            statusId = json.str("statusId"),
            priority = json.str("priority"),
            number = json.int("number"),
            tagIds = stringList(json.arr("tagIds")),
            createdAt = DateParsing.toEpochMillis(json.str("createdAt")),
            status = json.obj("status")?.let { s ->
                TicketStatus(
                    id = s.str("_id").orEmpty(),
                    name = s.str("name").orEmpty(),
                    color = s.str("color"),
                    type = s.str("type"),
                )
            },
            assignee = json.obj("assignee")?.let { a ->
                val d = a.obj("details")
                val full = d?.str("fullName")
                    ?: listOfNotNull(d?.str("firstName"), d?.str("lastName")).joinToString(" ").ifBlank { null }
                TicketAssignee(id = a.str("_id").orEmpty(), avatar = d?.str("avatar"), displayName = full)
            },
        )
    }

    fun parseTickets(array: List<JsonObject>): List<Ticket> = array.mapNotNull(::parseTicket)

    fun parseTag(json: JsonObject): TicketTag? {
        val id = json.str("_id") ?: return null
        return TicketTag(id = id, name = json.str("name").orEmpty(), colorCode = json.str("colorCode"))
    }

    private fun stringList(arr: JsonArray?): List<String> =
        arr.orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
}
