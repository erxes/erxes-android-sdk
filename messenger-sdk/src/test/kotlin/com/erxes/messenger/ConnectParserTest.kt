package com.erxes.messenger

import com.erxes.messenger.network.ConnectParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectParserTest {

    private fun parse(jsonText: String) =
        ConnectParser.parse(Json.parseToJsonElement(jsonText) as JsonObject, fallbackIntegrationId = "fallback")

    @Test
    fun `parses real-shape uiOptions and messengerData`() {
        val response = parse(
            """
            {
              "integrationId": "int-1",
              "customerId": "cust-1",
              "visitorId": "vis-1",
              "languageCode": "en",
              "uiOptions": {
                "primary": { "DEFAULT": "#7c3aed" },
                "backgroundColor": "#ffffff",
                "logo": "logo.png"
              },
              "messengerData": {
                "supporterIds": ["a", "b"],
                "isOnline": true,
                "requireAuth": true,
                "showChat": false,
                "onlineHours": [{ "day": "Monday", "from": "09:00", "to": "17:00" }],
                "messages": {
                  "greetings": { "title": "Hi there", "message": "How can we help?" },
                  "away": "We are away",
                  "welcome": "Welcome"
                },
                "links": { "facebook": "fb.com/x", "x": "x.com/y" }
              }
            }
            """.trimIndent()
        )

        assertEquals("int-1", response.integrationId)
        assertEquals("cust-1", response.customerId)
        assertEquals("#7c3aed", response.uiOptions.color)
        assertEquals("#ffffff", response.uiOptions.backgroundColor)
        assertEquals(listOf("a", "b"), response.messengerData.supporterIds)
        assertTrue(response.messengerData.isOnline)
        assertTrue(response.messengerData.requireAuth)
        assertFalse(response.messengerData.showChat)
        assertEquals("Hi there", response.messengerData.messages.greetTitle)
        assertEquals("How can we help?", response.messengerData.messages.greet)
        assertEquals(1, response.messengerData.onlineHours.size)
        assertEquals("Monday", response.messengerData.onlineHours[0].day)
        // `x` maps onto twitter when twitter is absent
        assertEquals("x.com/y", response.messengerData.links.twitter)
        assertEquals("fb.com/x", response.messengerData.links.facebook)
    }

    @Test
    fun `applies sensible defaults when fields missing`() {
        val response = parse("""{ "customerId": "c" }""")
        assertEquals("fallback", response.integrationId)
        assertNull(response.uiOptions.color)
        assertTrue(response.messengerData.showChat)        // default true
        assertTrue(response.messengerData.showLauncher)    // default true
        assertFalse(response.messengerData.requireAuth)    // default false
        assertEquals(emptyList<String>(), response.messengerData.supporterIds)
    }

    @Test
    fun `falls back to uiOptions color when primary absent`() {
        val response = parse("""{ "uiOptions": { "color": "#123456" } }""")
        assertEquals("#123456", response.uiOptions.color)
    }
}
