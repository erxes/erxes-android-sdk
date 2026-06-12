package com.erxes.messenger

import com.erxes.messenger.network.RealtimeClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RealtimeClientTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `builds ws and wss urls from http and https`() {
        assertEquals("wss://x.io/gateway/graphql", RealtimeClient.wsUrl("https://x.io/"))
        assertEquals("ws://x.io/gateway/graphql", RealtimeClient.wsUrl("http://x.io"))
    }

    @Test
    fun `completes graphql-transport-ws handshake and emits next payload`() = runBlocking {
        // Server: ack the init, then push one `next` frame on subscribe.
        server.enqueue(
            MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    when {
                        text.contains("connection_init") ->
                            webSocket.send("""{"type":"connection_ack"}""")
                        text.contains("\"subscribe\"") ->
                            webSocket.send(
                                """{"type":"next","payload":{"data":{"conversationMessageInserted":
                                   {"_id":"m1","content":"hi","createdAt":"1700000000000","user":{"_id":"u1"}}}}}"""
                                    .trimIndent()
                            )
                    }
                }
            })
        )

        val endpoint = server.url("/").toString().trimEnd('/') // http://… → ws://…
        val client = RealtimeClient(endpoint)

        val payload: JsonObject = withTimeout(5_000) {
            client.messageInserted("conv1").first()
        }

        assertEquals("\"m1\"", payload["_id"].toString())
        assertEquals("hi", (payload["content"] as JsonPrimitive).content)
    }
}
