package com.erxes.messenger

import com.erxes.messenger.network.ConnectParser
import com.erxes.messenger.network.TicketParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketParserTest {

    private fun obj(text: String) = Json.parseToJsonElement(text) as JsonObject

    @Test
    fun `connect parses top-level ticketConfig with form fields`() {
        val response = ConnectParser.parse(
            obj(
                """
                {
                  "integrationId": "i1",
                  "messengerData": {},
                  "ticketConfig": {
                    "_id": "tc1", "name": "Support", "pipelineId": "p1",
                    "selectedStatusId": "s1", "channelId": "c1", "parentId": "par1",
                    "formFields": {
                      "name": { "isShow": true, "label": "Subject", "order": 1 },
                      "tags": { "isShow": true, "order": 2 }
                    }
                  }
                }
                """.trimIndent()
            ),
            fallbackIntegrationId = "i1",
        )
        val cfg = response.messengerData.ticketConfig
        assertTrue(cfg != null)
        assertEquals("tc1", cfg!!.id)
        assertEquals("p1", cfg.pipelineId)
        assertEquals("s1", cfg.selectedStatusId)
        assertEquals("Subject", cfg.formFields.name?.label)
        assertEquals(true, cfg.formFields.tags?.isShow)
        assertNull(cfg.formFields.description)
    }

    @Test
    fun `ticketConfig null when required ids missing`() {
        val response = ConnectParser.parse(
            obj("""{ "ticketConfig": { "name": "x" } }"""),
            fallbackIntegrationId = "i1",
        )
        assertNull(response.messengerData.ticketConfig)
    }

    @Test
    fun `parses ticket with status and assignee`() {
        val ticket = TicketParser.parseTicket(
            obj(
                """
                { "_id": "t1", "name": "Printer broken", "number": 42, "createdAt": "1700000000000",
                  "statusId": "s1", "tagIds": ["a", "b"],
                  "status": { "_id": "s1", "name": "Open", "color": "#00ff00", "type": "active" },
                  "assignee": { "_id": "u1", "details": { "firstName": "Jane", "lastName": "Doe" } } }
                """.trimIndent()
            )
        )!!
        assertEquals("t1", ticket.id)
        assertEquals(42, ticket.number)
        assertEquals(listOf("a", "b"), ticket.tagIds)
        assertEquals("Open", ticket.status?.name)
        assertEquals("Jane Doe", ticket.assignee?.displayName)
    }
}
