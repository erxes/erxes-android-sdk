package com.erxes.messenger.network

/**
 * GraphQL operation documents for the messenger gateway. Ported from the iOS SDK's
 * `Network/Operations` graphql files. Kept as raw strings (no codegen) until Apollo
 * is introduced in Phase 3 for subscriptions. See docs/PROTOCOL.md.
 */
internal object MessengerOperations {

    /**
     * `widgetsMessengerConnect` — handshake run on every launch. The customer name is
     * passed inside the `data` JSON (the backend schema has no top-level `name` argument;
     * see the iOS `connect` mutation).
     */
    val CONNECT = """
        mutation connect(${'$'}integrationId: String!, ${'$'}visitorId: String, ${'$'}cachedCustomerId: String, ${'$'}email: String, ${'$'}phone: String, ${'$'}data: JSON) {
          widgetsMessengerConnect(integrationId: ${'$'}integrationId, visitorId: ${'$'}visitorId, cachedCustomerId: ${'$'}cachedCustomerId, email: ${'$'}email, phone: ${'$'}phone, data: ${'$'}data) {
            integrationId
            customerId
            visitorId
            languageCode
            uiOptions
            messengerData
            ticketConfig
          }
        }
    """.trimIndent()

    /** `widgetsSaveBrowserInfo` — registers a page visit; may trigger auto messages. */
    val SAVE_BROWSER_INFO = """
        mutation saveBrowserInfo(${'$'}customerId: String, ${'$'}visitorId: String, ${'$'}browserInfo: JSON!) {
          widgetsSaveBrowserInfo(customerId: ${'$'}customerId, visitorId: ${'$'}visitorId, browserInfo: ${'$'}browserInfo) {
            _id
            content
            createdAt
          }
        }
    """.trimIndent()

    /** `widgetsMessengerSupporters` — online supporters for the launcher. */
    val SUPPORTERS = """
        query widgetsMessengerSupporters(${'$'}integrationId: String!) {
          widgetsMessengerSupporters(integrationId: ${'$'}integrationId) {
            supporters { _id details { avatar fullName } isOnline }
            isOnline
          }
        }
    """.trimIndent()

    /** `widgetsConversations` — list conversations for this customer/visitor. */
    val CONVERSATIONS = """
        query widgetsConversations(${'$'}integrationId: String!, ${'$'}customerId: String, ${'$'}visitorId: String) {
          widgetsConversations(integrationId: ${'$'}integrationId, customerId: ${'$'}customerId, visitorId: ${'$'}visitorId) {
            _id
            content
            createdAt
            participatedUsers { _id details { avatar fullName } isOnline }
            messages {
              _id content createdAt customerId conversationId fromBot botData
              attachments { url name type size }
              user { _id details { avatar fullName } }
            }
          }
        }
    """.trimIndent()

    /** `widgetsConversationDetail` — full thread of one conversation. */
    val CONVERSATION_DETAIL = """
        query widgetsConversationDetail(${'$'}_id: String!, ${'$'}integrationId: String) {
          widgetsConversationDetail(_id: ${'$'}_id, integrationId: ${'$'}integrationId) {
            _id
            messages {
              _id conversationId customerId content createdAt internal fromBot botData contentType
              attachments { url name size type }
              user { _id details { avatar fullName } }
            }
            isOnline
            supporters { _id details { avatar fullName } isOnline }
          }
        }
    """.trimIndent()

    /** `widgetsInsertMessage` — send a chat message. */
    val INSERT_MESSAGE = """
        mutation widgetsInsertMessage(${'$'}integrationId: String!, ${'$'}customerId: String, ${'$'}visitorId: String, ${'$'}conversationId: String, ${'$'}message: String, ${'$'}contentType: String, ${'$'}attachments: [AttachmentInput]) {
          widgetsInsertMessage(integrationId: ${'$'}integrationId, customerId: ${'$'}customerId, visitorId: ${'$'}visitorId, conversationId: ${'$'}conversationId, message: ${'$'}message, contentType: ${'$'}contentType, attachments: ${'$'}attachments) {
            _id conversationId customerId content createdAt fromBot contentType
            attachments { url name size type }
            user { _id details { avatar fullName } }
          }
        }
    """.trimIndent()

    /** `widgetsReadConversationMessages` — mark all messages in a conversation as read. */
    val READ_MESSAGES = """
        mutation widgetsReadConversationMessages(${'$'}conversationId: String!) {
          widgetsReadConversationMessages(conversationId: ${'$'}conversationId)
        }
    """.trimIndent()

    /** `widgetsTotalUnreadCount` — total unread badge count. */
    val TOTAL_UNREAD = """
        query widgetsTotalUnreadCount(${'$'}integrationId: String!, ${'$'}customerId: String, ${'$'}visitorId: String) {
          widgetsTotalUnreadCount(integrationId: ${'$'}integrationId, customerId: ${'$'}customerId, visitorId: ${'$'}visitorId)
        }
    """.trimIndent()

    /** `conversationMessageInserted` — realtime new message in a conversation (graphql-transport-ws). */
    val SUB_MESSAGE_INSERTED = """
        subscription conversationMessageInserted(${'$'}_id: String!) {
          conversationMessageInserted(_id: ${'$'}_id) {
            _id conversationId customerId content createdAt fromBot botData contentType
            attachments { url name size type }
            user { _id details { avatar fullName } }
          }
        }
    """.trimIndent()

    /** `conversationBotTypingStatus` — realtime bot typing indicator. */
    val SUB_BOT_TYPING = """
        subscription conversationBotTypingStatus(${'$'}_id: String!) {
          conversationBotTypingStatus(_id: ${'$'}_id) { _id typing }
        }
    """.trimIndent()

    /** `widgetsTicketCustomersEdit` — attach email/phone (+name) to the connect-created customer. */
    val CUSTOMERS_EDIT = """
        mutation CustomersEdit(${'$'}customerId: String!, ${'$'}firstName: String, ${'$'}lastName: String, ${'$'}emails: [String], ${'$'}phones: [String]) {
          widgetsTicketCustomersEdit(customerId: ${'$'}customerId, firstName: ${'$'}firstName, lastName: ${'$'}lastName, emails: ${'$'}emails, phones: ${'$'}phones) {
            _id firstName lastName primaryEmail primaryPhone emails phones
          }
        }
    """.trimIndent()

    /** `widgetsSaveCustomerGetNotified` — store an email/phone for follow-up notifications. */
    val SAVE_GET_NOTIFIED = """
        mutation widgetsSaveCustomerGetNotified(${'$'}customerId: String, ${'$'}visitorId: String, ${'$'}type: String!, ${'$'}value: String!) {
          widgetsSaveCustomerGetNotified(customerId: ${'$'}customerId, visitorId: ${'$'}visitorId, type: ${'$'}type, value: ${'$'}value)
        }
    """.trimIndent()

    /** `widgetTicketsByCustomer` — list this customer's tickets. */
    val TICKETS_BY_CUSTOMER = """
        query WidgetTicketsByCustomer(${'$'}customerId: String) {
          widgetTicketsByCustomer(customerId: ${'$'}customerId) {
            _id name description pipelineId statusId priority labelIds tagIds number
            startDate targetDate createdAt updatedAt
            status { _id color name description type }
            assignee { _id details { avatar firstName lastName fullName } }
          }
        }
    """.trimIndent()

    /** `widgetTicketCreated` — create a ticket; returns id + number. */
    val TICKET_CREATE = """
        mutation WidgetTicketCreated(${'$'}name: String!, ${'$'}statusId: String!, ${'$'}customerIds: [String!]!, ${'$'}description: String, ${'$'}attachments: [AttachmentInput], ${'$'}tagIds: [String!]) {
          widgetTicketCreated(name: ${'$'}name, statusId: ${'$'}statusId, customerIds: ${'$'}customerIds, description: ${'$'}description, attachments: ${'$'}attachments, tagIds: ${'$'}tagIds) {
            _id number
          }
        }
    """.trimIndent()

    /** `widgetsGetTicketTags` — selectable tags for the ticket form. */
    val TICKET_TAGS = """
        query WidgetsGetTicketTags(${'$'}configId: String, ${'$'}parentId: String) {
          widgetsGetTicketTags(configId: ${'$'}configId, parentId: ${'$'}parentId) {
            _id name colorCode
          }
        }
    """.trimIndent()

    /** `cpKnowledgeBaseTopicDetail` — knowledge base topic with categories + articles. */
    val KB_TOPIC_DETAIL = """
        query cpKnowledgeBaseTopicDetail(${'$'}_id: String!) {
          cpKnowledgeBaseTopicDetail(_id: ${'$'}_id) {
            _id title description color code
            categories {
              _id title description numOfArticles(status: "publish") countArticles
              parentCategoryId icon
              articles(status: "publish") { _id title summary content code viewCount categoryId publishedAt }
            }
            parentCategories {
              _id title description numOfArticles(status: "publish") parentCategoryId icon
              childrens { _id }
              articles { _id title summary content code viewCount categoryId publishedAt }
            }
          }
        }
    """.trimIndent()
}
