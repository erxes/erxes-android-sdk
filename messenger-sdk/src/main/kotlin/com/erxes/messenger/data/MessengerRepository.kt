package com.erxes.messenger.data

import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import com.erxes.messenger.data.model.Attachment
import com.erxes.messenger.data.model.ConnectResponse
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.data.model.KbTopic
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.data.model.Supporter
import com.erxes.messenger.data.model.Ticket
import com.erxes.messenger.data.model.TicketTag
import com.erxes.messenger.network.ConnectParser
import com.erxes.messenger.network.FileUploader
import com.erxes.messenger.network.GraphQLClient
import com.erxes.messenger.network.KbParser
import com.erxes.messenger.network.MessageParser
import com.erxes.messenger.network.MessengerOperations
import com.erxes.messenger.network.RealtimeClient
import com.erxes.messenger.network.TicketParser
import com.erxes.messenger.network.UploadedAttachment
import com.erxes.messenger.session.SessionStore
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Orchestrates the connect handshake and supporter/browser-info follow-ups.
 * Mirrors the network-facing half of `AppViewModel` (iOS). UI state is layered on
 * top of this in later phases.
 */
class MessengerRepository(
    private val config: MessengerConfig,
    private val session: SessionStore,
    private val graphQL: GraphQLClient = GraphQLClient(),
    // WebSocket lives on `endpoint` (not fileEndpoint), upgraded to ws/wss.
    private val realtime: RealtimeClient = RealtimeClient(config.endpoint),
    private val fileUploader: FileUploader = FileUploader(),
) {
    /** GraphQL is served from the file endpoint (no `w.` subdomain). See docs/PROTOCOL.md. */
    private val endpoint: String get() = config.fileEndpoint

    /**
     * Runs `widgetsMessengerConnect`, persists the returned customerId, and fires the
     * non-blocking browser-info + supporters calls on [scope]. Returns the parsed response.
     */
    suspend fun connect(user: MessengerUser?, scope: CoroutineScope): ConnectResponse {
        session.bind(config.integrationId)
        // Reset the cached customer when the host connects with a different email/phone,
        // otherwise the stale cachedCustomerId re-identifies the previous person and surfaces
        // their old conversations. Runs before cachedCustomerId is read below so a different
        // identity drops the stale id first.
        session.bind(user?.email, user?.phone)
        val visitorId = session.visitorId()
        val cachedCustomerId = session.cachedCustomerId()

        val variables = buildJsonObject {
            put("integrationId", config.integrationId)
            put("visitorId", visitorId)
            if (cachedCustomerId != null) put("cachedCustomerId", cachedCustomerId)
            user?.email?.takeIf { it.isNotEmpty() }?.let { put("email", it) }
            user?.phone?.takeIf { it.isNotEmpty() }?.let { put("phone", it) }
            // The schema has no top-level `name` arg; carry the name in `data`.
            user?.name?.takeIf { it.isNotEmpty() }?.let {
                put("data", buildJsonObject { put("name", it) })
            }
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

    // ── Conversations & messaging (Phase 2) ────────────────────────────────────

    /** Lists this customer/visitor's conversations, ordered by latest activity (newest first). */
    suspend fun conversations(): List<Conversation> {
        val variables = buildJsonObject {
            put("integrationId", config.integrationId)
            // Identity rule (mirrors iOS): prefer the registered customerId, fall
            // back to the guest visitorId. They are mutually exclusive on the
            // backend — sending both makes it key off the empty guest id.
            val customerId = session.cachedCustomerId()
            if (!customerId.isNullOrEmpty()) {
                put("customerId", customerId)
            } else {
                put("visitorId", session.visitorId())
            }
        }
        val array = graphQL.arrayField(
            endpoint, "widgetsConversations", MessengerOperations.CONVERSATIONS, variables, "widgetsConversations",
        )
        return MessageParser.parseConversations(array.filterIsInstance<JsonObject>())
            .sortedByDescending { it.lastActivityAt }
    }

    /** Full thread for one conversation. Returns null when not found. */
    suspend fun conversationDetail(conversationId: String): Conversation? {
        val variables = buildJsonObject {
            put("_id", conversationId)
            put("integrationId", config.integrationId)
        }
        val json = graphQL.send(
            endpoint, "widgetsConversationDetail", MessengerOperations.CONVERSATION_DETAIL, variables,
        )
        json["errors"]?.let { SdkLog.e("conversationDetail errors: $it") }
        val detail = (json["data"] as? JsonObject)?.get("widgetsConversationDetail") as? JsonObject
            ?: return null
        return MessageParser.parseConversation(detail)
    }

    /**
     * Sends a message. Pass a null [conversationId] to start a new conversation; the
     * returned [Message] carries the conversationId the server assigned, which the
     * caller should persist as the active conversation.
     */
    suspend fun sendMessage(
        conversationId: String?,
        text: String,
        attachments: List<Attachment> = emptyList(),
    ): Message {
        val variables = buildJsonObject {
            put("integrationId", config.integrationId)
            session.cachedCustomerId()?.let { put("customerId", it) }
            put("visitorId", session.visitorId())
            conversationId?.let { put("conversationId", it) }
            put("message", text)
            put("contentType", "text")
            if (attachments.isNotEmpty()) {
                putJsonArray("attachments") {
                    attachments.forEach { att ->
                        add(buildJsonObject {
                            put("url", att.url)
                            att.name?.let { put("name", it) }
                            att.type?.let { put("type", it) }
                            att.size?.let { put("size", it) }
                        })
                    }
                }
            }
        }
        val obj = graphQL.objectField(
            endpoint, "widgetsInsertMessage", MessengerOperations.INSERT_MESSAGE, variables, "widgetsInsertMessage",
        )
        val message = MessageParser.parseMessage(obj)
            ?: throw com.erxes.messenger.network.GraphQLException("Malformed insertMessage response")
        message.conversationId?.let { session.setLastConversationId(it) }
        return message
    }

    /** Marks every message in a conversation as read. */
    suspend fun markRead(conversationId: String) {
        val variables = buildJsonObject { put("conversationId", conversationId) }
        graphQL.send(endpoint, "widgetsReadConversationMessages", MessengerOperations.READ_MESSAGES, variables)
    }

    /** Total unread badge count for this customer/visitor. */
    suspend fun totalUnreadCount(): Int {
        val variables = buildJsonObject {
            put("integrationId", config.integrationId)
            session.cachedCustomerId()?.let { put("customerId", it) }
            put("visitorId", session.visitorId())
        }
        val json = graphQL.send(endpoint, "widgetsTotalUnreadCount", MessengerOperations.TOTAL_UNREAD, variables)
        return ((json["data"] as? JsonObject)?.get("widgetsTotalUnreadCount") as? JsonPrimitive)?.intOrNull ?: 0
    }

    // ── Realtime (Phase 3) ─────────────────────────────────────────────────────

    /** Live stream of new messages in [conversationId]. Cold; collecting opens the socket. */
    fun messageStream(conversationId: String): Flow<Message> =
        realtime.messageInserted(conversationId).mapNotNull { MessageParser.parseMessage(it) }

    /** Live bot typing indicator for [conversationId] (`true` while the bot is typing). */
    fun botTypingStream(conversationId: String): Flow<Boolean> =
        realtime.botTyping(conversationId).mapNotNull { (it["typing"] as? JsonPrimitive)?.booleanOrNull }

    // ── File upload (Phase 4) ──────────────────────────────────────────────────

    /**
     * Uploads an image (PNG/JPEG) to the gateway and returns its attachment descriptor,
     * ready to pass to [sendMessage]. Throws [com.erxes.messenger.network.UploadException]
     * on rejection/failure.
     */
    suspend fun uploadAttachment(bytes: ByteArray, filename: String, mimeType: String): UploadedAttachment =
        fileUploader.upload(bytes, filename, mimeType, config.fileEndpoint)

    // ── requireAuth identity & notifications (Phase 6) ──────────────────────────

    /** Contact channel a visitor can be identified/notified by. */
    enum class ContactKind { EMAIL, PHONE }

    /**
     * Attaches [value] (and optional name) to the connect-created customer via
     * `widgetsTicketCustomersEdit`, persists the returned id, and marks the session
     * identified. Used by the requireAuth form before a conversation can start.
     */
    suspend fun identify(kind: ContactKind, value: String, firstName: String?, lastName: String?) {
        val customerId = session.cachedCustomerId()
            ?: throw com.erxes.messenger.network.GraphQLException("No customer to identify")

        val variables = buildJsonObject {
            put("customerId", customerId)
            firstName?.takeIf { it.isNotBlank() }?.let { put("firstName", it) }
            lastName?.takeIf { it.isNotBlank() }?.let { put("lastName", it) }
            when (kind) {
                ContactKind.EMAIL -> putJsonArray("emails") { add(value) }
                ContactKind.PHONE -> putJsonArray("phones") { add(value) }
            }
        }

        val obj = graphQL.objectField(
            endpoint, "widgetsTicketCustomersEdit", MessengerOperations.CUSTOMERS_EDIT, variables,
            "widgetsTicketCustomersEdit",
        )
        (obj["_id"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() }
            ?.let { session.setCachedCustomerId(it) }
        session.setIdentified(true)
    }

    /** Stores an email/phone for follow-up notifications (e.g. email opt-in when offline). */
    suspend fun saveGetNotified(kind: ContactKind, value: String) {
        val variables = buildJsonObject {
            session.cachedCustomerId()?.let { put("customerId", it) }
            put("visitorId", session.visitorId())
            put("type", if (kind == ContactKind.EMAIL) "email" else "phone")
            put("value", value)
        }
        graphQL.send(endpoint, "widgetsSaveCustomerGetNotified", MessengerOperations.SAVE_GET_NOTIFIED, variables)
    }

    // ── Tickets (Phase 6b) ─────────────────────────────────────────────────────

    /** Lists the current customer's tickets. Empty for anonymous visitors. */
    suspend fun tickets(): List<Ticket> {
        val customerId = session.cachedCustomerId() ?: return emptyList()
        val variables = buildJsonObject { put("customerId", customerId) }
        val array = graphQL.arrayField(
            endpoint, "widgetTicketsByCustomer", MessengerOperations.TICKETS_BY_CUSTOMER, variables,
            "widgetTicketsByCustomer",
        )
        return TicketParser.parseTickets(array.filterIsInstance<JsonObject>())
    }

    /** Selectable tags for the ticket create form. */
    suspend fun ticketTags(configId: String, parentId: String?): List<TicketTag> {
        val variables = buildJsonObject {
            put("configId", configId)
            parentId?.let { put("parentId", it) }
        }
        val array = graphQL.arrayField(
            endpoint, "widgetsGetTicketTags", MessengerOperations.TICKET_TAGS, variables, "widgetsGetTicketTags",
        )
        return array.filterIsInstance<JsonObject>().mapNotNull(TicketParser::parseTag)
    }

    /**
     * Creates a ticket in the configured pipeline/status for the current customer.
     * Returns the new ticket id. Requires a registered customer (identify first if anonymous).
     */
    suspend fun createTicket(
        name: String,
        description: String?,
        statusId: String,
        tagIds: List<String> = emptyList(),
        attachments: List<Attachment> = emptyList(),
    ): String {
        val customerId = session.cachedCustomerId()
            ?: throw com.erxes.messenger.network.GraphQLException("No customer to create a ticket for")
        val variables = buildJsonObject {
            put("name", name)
            put("statusId", statusId)
            putJsonArray("customerIds") { add(customerId) }
            description?.takeIf { it.isNotBlank() }?.let { put("description", it) }
            if (tagIds.isNotEmpty()) putJsonArray("tagIds") { tagIds.forEach { add(it) } }
            if (attachments.isNotEmpty()) {
                putJsonArray("attachments") {
                    attachments.forEach { att ->
                        add(buildJsonObject {
                            put("url", att.url)
                            att.name?.let { put("name", it) }
                            att.type?.let { put("type", it) }
                            att.size?.let { put("size", it) }
                        })
                    }
                }
            }
        }
        val obj = graphQL.objectField(
            endpoint, "widgetTicketCreated", MessengerOperations.TICKET_CREATE, variables, "widgetTicketCreated",
        )
        return (obj["_id"] as? JsonPrimitive)?.contentOrNull
            ?: throw com.erxes.messenger.network.GraphQLException("Failed to parse widgetTicketCreated")
    }

    // ── Knowledge base (Phase 6c) ──────────────────────────────────────────────

    /** Fetches a knowledge-base topic (categories + articles). Returns null when not found. */
    suspend fun knowledgeBase(topicId: String): KbTopic? {
        val variables = buildJsonObject { put("_id", topicId) }
        val json = graphQL.send(
            endpoint, "cpKnowledgeBaseTopicDetail", MessengerOperations.KB_TOPIC_DETAIL, variables,
        )
        val detail = (json["data"] as? JsonObject)?.get("cpKnowledgeBaseTopicDetail") as? JsonObject
            ?: return null
        return KbParser.parseTopic(detail)
    }
}
