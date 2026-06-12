package com.erxes.messenger.network

import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Thrown when a GraphQL response carries an `errors` array or cannot be parsed. */
class GraphQLException(message: String) : Exception(message)

/**
 * Shared GraphQL transport. Centralizes endpoint URL construction, request building,
 * status logging, and error handling. Mirrors `Network/GraphQLClient.swift` (iOS).
 *
 * The messenger gateway always lives at `{base}/gateway/graphql`. A trailing slash on
 * the configured endpoint is stripped before appending.
 */
class GraphQLClient(
    private val httpClient: OkHttpClient = defaultClient,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /** Performs a GraphQL POST and returns the full decoded top-level JSON object. */
    suspend fun send(
        endpoint: String,
        operation: String,
        query: String,
        variables: JsonObject,
    ): JsonObject = withContext(io) {
        val url = urlFor(endpoint)
        val payload = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }
        val request = Request.Builder()
            .url(url)
            .post(json.encodeToString(JsonObject.serializer(), payload).toRequestBody(jsonMediaType))
            .build()

        val response = httpClient.newCall(request).await()
        response.use {
            val status = it.code
            SdkLog.d("$operation HTTP $status")
            val bodyText = it.body?.string().orEmpty()
            runCatching { json.parseToJsonElement(bodyText).jsonObject }
                .getOrElse { throw GraphQLException("Failed to parse $operation response") }
        }
    }

    /** Throws [GraphQLException] when the response has an `errors` array, else returns `data[field]`. */
    suspend fun objectField(
        endpoint: String,
        operation: String,
        query: String,
        variables: JsonObject,
        field: String,
    ): JsonObject {
        val jsonResponse = send(endpoint, operation, query, variables)
        jsonResponse.errorsOrNull()?.let { errors ->
            SdkLog.e("$operation errors: $errors")
            throw GraphQLException(errors.firstMessage() ?: "Unknown error")
        }
        return jsonResponse.dataField(field)?.jsonObject
            ?: throw GraphQLException("Failed to parse $field")
    }

    /** Tolerant list fetch: logs any `errors` but does not throw; returns `data[field]` array or empty. */
    suspend fun arrayField(
        endpoint: String,
        operation: String,
        query: String,
        variables: JsonObject,
        field: String,
    ): JsonArray {
        val jsonResponse = send(endpoint, operation, query, variables)
        jsonResponse.errorsOrNull()?.let { SdkLog.e("$operation errors: $it") }
        return jsonResponse.dataField(field) as? JsonArray ?: JsonArray(emptyList())
    }

    companion object {
        /** GraphQL gateway URL for an endpoint base, stripping a trailing slash. */
        fun urlFor(endpoint: String): String {
            val base = endpoint.trimEnd('/')
            return "$base/gateway/graphql"
        }

        internal val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

        private val defaultClient: OkHttpClient by lazy { OkHttpClient() }
    }
}

// ── helpers ───────────────────────────────────────────────────────────────────

private fun JsonObject.errorsOrNull(): JsonArray? =
    (this["errors"] as? JsonArray)?.takeIf { it.isNotEmpty() }

private fun JsonArray.firstMessage(): String? =
    (firstOrNull() as? JsonObject)?.get("message")?.jsonPrimitive?.content

private fun JsonObject.dataField(field: String): JsonElement? =
    (this["data"] as? JsonObject)?.get(field)
