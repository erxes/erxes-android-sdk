package com.erxes.messenger

import com.erxes.messenger.network.GraphQLClient
import com.erxes.messenger.network.GraphQLException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GraphQLClientTest {

    private lateinit var server: MockWebServer
    private val client = GraphQLClient()

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun endpoint() = server.url("/").toString().trimEnd('/')

    @Test
    fun `builds gateway graphql url stripping trailing slash`() {
        assertEquals("https://x.io/gateway/graphql", GraphQLClient.urlFor("https://x.io/"))
        assertEquals("https://x.io/gateway/graphql", GraphQLClient.urlFor("https://x.io"))
    }

    @Test
    fun `posts to gateway graphql path`() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"ok":true}}"""))
        client.send(endpoint(), "op", "query {}", JsonObject(emptyMap()))
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/gateway/graphql", recorded.path)
        assertTrue(recorded.getHeader("Content-Type")!!.contains("application/json"))
    }

    @Test
    fun `objectField throws on errors array`() = runTest {
        server.enqueue(MockResponse().setBody("""{"errors":[{"message":"boom"}]}"""))
        val ex = assertThrows(GraphQLException::class.java) {
            kotlinx.coroutines.runBlocking {
                client.objectField(endpoint(), "op", "q", JsonObject(emptyMap()), "field")
            }
        }
        assertEquals("boom", ex.message)
    }

    @Test
    fun `objectField returns data field`() = runTest {
        server.enqueue(MockResponse().setBody("""{"data":{"widget":{"_id":"1"}}}"""))
        val obj = client.objectField(endpoint(), "op", "q", JsonObject(emptyMap()), "widget")
        assertEquals("\"1\"", obj["_id"].toString())
    }
}
