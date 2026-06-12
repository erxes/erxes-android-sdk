package com.erxes.messenger.network

import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

/**
 * Realtime subscriptions over `graphql-transport-ws`. Mirrors the WebSocket lifecycle in
 * the iOS `ChatViewModel`: connect → connection_init → connection_ack → subscribe →
 * next/error/complete, with exponential-backoff reconnect.
 *
 * Each subscription is a cold [Flow]: collecting opens a socket, cancelling closes it.
 * The WS lives on the configured `endpoint` (not fileEndpoint), upgraded to ws/wss.
 */
class RealtimeClient(
    private val endpoint: String,
    private val httpClient: OkHttpClient = sharedClient,
) {
    /** Emits each new message inserted into [conversationId]. Reconnects on drop. */
    fun messageInserted(conversationId: String): Flow<JsonObject> =
        subscribe(
            operation = "conversationMessageInserted",
            query = MessengerOperations.SUB_MESSAGE_INSERTED,
            variables = buildJsonObject { put("_id", conversationId) },
            field = "conversationMessageInserted",
        )

    /** Emits bot typing-status payloads (`{ _id, typing }`) for [conversationId]. */
    fun botTyping(conversationId: String): Flow<JsonObject> =
        subscribe(
            operation = "conversationBotTypingStatus",
            query = MessengerOperations.SUB_BOT_TYPING,
            variables = buildJsonObject { put("_id", conversationId) },
            field = "conversationBotTypingStatus",
        )

    private fun subscribe(
        operation: String,
        query: String,
        variables: JsonObject,
        field: String,
    ): Flow<JsonObject> = openOnce(operation, query, variables, field)
        .retryWhen { cause, attempt ->
            val backoffMs = (INITIAL_BACKOFF_MS shl attempt.toInt().coerceAtMost(5)).coerceAtMost(MAX_BACKOFF_MS)
            SdkLog.e("WS $operation disconnected ($cause). Reconnecting in ${backoffMs}ms…")
            delay(backoffMs)
            true
        }

    /** One connection lifecycle. Completes on server `complete`; throws on drop to trigger retry. */
    private fun openOnce(
        operation: String,
        query: String,
        variables: JsonObject,
        field: String,
    ): Flow<JsonObject> = callbackFlow {
        val subscriptionId = UUID.randomUUID().toString()
        val request = Request.Builder()
            .url(wsUrl(endpoint))
            .header("Sec-WebSocket-Protocol", "graphql-transport-ws")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(frame { put("type", "connection_init") })
                SdkLog.d("WS → connection_init ($operation)")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = runCatching { GraphQLClient.json.parseToJsonElement(text).jsonObjectOrNull() }
                    .getOrNull() ?: return
                when (json.str("type")) {
                    "connection_ack" -> {
                        webSocket.send(
                            frame {
                                put("id", subscriptionId)
                                put("type", "subscribe")
                                put("payload", buildJsonObject {
                                    put("query", query)
                                    put("variables", variables)
                                })
                            }
                        )
                        SdkLog.d("WS → subscribe $operation")
                    }

                    "next" -> {
                        val payload = json.obj("payload")
                        val data = payload?.obj("data")
                        (data?.get(field) as? JsonObject)?.let { trySend(it) }
                    }

                    "error" -> SdkLog.e("WS $operation error: $text")

                    "complete" -> {
                        SdkLog.d("WS $operation completed by server")
                        close() // clean stop; no reconnect
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t) // surfaces to retryWhen for reconnect
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(NORMAL_CLOSURE, null)
                close()
            }
        }

        val socket = httpClient.newWebSocket(request, listener)
        awaitClose {
            runCatching { socket.send(frame { put("id", subscriptionId); put("type", "complete") }) }
            socket.cancel()
        }
    }

    private fun frame(build: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit): String =
        GraphQLClient.json.encodeToString(JsonObject.serializer(), buildJsonObject(build))

    companion object {
        private const val INITIAL_BACKOFF_MS = 1_000L
        private const val MAX_BACKOFF_MS = 30_000L
        private const val NORMAL_CLOSURE = 1000

        /** ws/wss gateway URL for an endpoint base. */
        fun wsUrl(endpoint: String): String {
            val base = endpoint.trimEnd('/')
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://")
            return "$base/gateway/graphql"
        }

        private val sharedClient: OkHttpClient by lazy { OkHttpClient() }
    }
}

private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject
