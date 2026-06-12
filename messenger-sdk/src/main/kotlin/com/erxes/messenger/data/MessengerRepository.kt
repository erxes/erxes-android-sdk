package com.erxes.messenger.data

import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import com.erxes.messenger.data.model.ConnectResponse
import com.erxes.messenger.data.model.Supporter
import com.erxes.messenger.network.ConnectParser
import com.erxes.messenger.network.GraphQLClient
import com.erxes.messenger.network.MessengerOperations
import com.erxes.messenger.session.SessionStore
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * Orchestrates the connect handshake and supporter/browser-info follow-ups.
 * Mirrors the network-facing half of `AppViewModel` (iOS). UI state is layered on
 * top of this in later phases.
 */
class MessengerRepository(
    private val config: MessengerConfig,
    private val session: SessionStore,
    private val graphQL: GraphQLClient = GraphQLClient(),
) {
    /** GraphQL is served from the file endpoint (no `w.` subdomain). See docs/PROTOCOL.md. */
    private val endpoint: String get() = config.fileEndpoint

    /**
     * Runs `widgetsMessengerConnect`, persists the returned customerId, and fires the
     * non-blocking browser-info + supporters calls on [scope]. Returns the parsed response.
     */
    suspend fun connect(user: MessengerUser?, scope: CoroutineScope): ConnectResponse {
        session.bind(config.integrationId)
        val visitorId = session.visitorId()
        val cachedCustomerId = session.cachedCustomerId()

        val variables = buildJsonObject {
            put("integrationId", config.integrationId)
            put("visitorId", visitorId)
            if (cachedCustomerId != null) put("cachedCustomerId", cachedCustomerId)
            user?.email?.takeIf { it.isNotEmpty() }?.let { put("email", it) }
            user?.phone?.takeIf { it.isNotEmpty() }?.let { put("phone", it) }
            user?.name?.takeIf { it.isNotEmpty() }?.let { put("name", it) }
        }

        val json = graphQL.send(endpoint, "connect", MessengerOperations.CONNECT, variables)
        json["errors"]?.let { SdkLog.e("connect GraphQL errors: $it") }

        val data = (json["data"] as? JsonObject)?.get("widgetsMessengerConnect") as? JsonObject
        val response = ConnectParser.parse(data, fallbackIntegrationId = config.integrationId)

        response.customerId?.takeIf { it.isNotEmpty() }?.let { session.setCachedCustomerId(it) }

        // Fire-and-forget; must not block readiness.
        scope.launch { runCatching { saveBrowserInfo(response.customerId, visitorId) } }
        scope.launch { runCatching { fetchSupporters() } }

        return response
    }

    /** Registers a minimal browser-info payload. May trigger automated welcome messages. */
    private suspend fun saveBrowserInfo(customerId: String?, visitorId: String) {
        val variables = buildJsonObject {
            if (customerId != null) put("customerId", customerId)
            put("visitorId", visitorId)
            put("browserInfo", buildJsonObject {
                put("url", "/")
                put("platform", "android")
            })
        }
        graphQL.send(endpoint, "saveBrowserInfo", MessengerOperations.SAVE_BROWSER_INFO, variables)
    }

    /** Loads online supporters for the launcher/header. Tolerant of partial data. */
    suspend fun fetchSupporters(): List<Supporter> {
        val variables = buildJsonObject { put("integrationId", config.integrationId) }
        val json = graphQL.send(endpoint, "widgetsMessengerSupporters", MessengerOperations.SUPPORTERS, variables)
        val result = (json["data"] as? JsonObject)?.get("widgetsMessengerSupporters") as? JsonObject
        val list = result?.get("supporters") as? JsonArray ?: return emptyList()
        return list.mapNotNull { el ->
            val s = el as? JsonObject ?: return@mapNotNull null
            val details = s["details"] as? JsonObject
            Supporter(
                id = (s["_id"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null,
                fullName = (details?.get("fullName") as? JsonPrimitive)?.contentOrNull,
                avatar = (details?.get("avatar") as? JsonPrimitive)?.contentOrNull,
                isOnline = (s["isOnline"] as? JsonPrimitive)?.booleanOrNull ?: false,
            )
        }
    }
}
