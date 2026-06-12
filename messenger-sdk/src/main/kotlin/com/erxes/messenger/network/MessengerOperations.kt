package com.erxes.messenger.network

/**
 * GraphQL operation documents for the messenger gateway. Ported from the iOS SDK's
 * `Network/Operations` graphql files. Kept as raw strings (no codegen) until Apollo
 * is introduced in Phase 3 for subscriptions. See docs/PROTOCOL.md.
 */
internal object MessengerOperations {

    /** `widgetsMessengerConnect` — handshake run on every launch. */
    val CONNECT = """
        mutation connect(${'$'}integrationId: String!, ${'$'}visitorId: String, ${'$'}cachedCustomerId: String, ${'$'}email: String, ${'$'}phone: String, ${'$'}name: String, ${'$'}data: JSON) {
          widgetsMessengerConnect(integrationId: ${'$'}integrationId, visitorId: ${'$'}visitorId, cachedCustomerId: ${'$'}cachedCustomerId, email: ${'$'}email, phone: ${'$'}phone, name: ${'$'}name, data: ${'$'}data) {
            integrationId
            customerId
            visitorId
            languageCode
            uiOptions
            messengerData
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
            unreadCount
            participatedUsers { _id details { avatar fullName } }
            messages {
              _id content createdAt customerId conversationId fromBot
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
              _id conversationId customerId content createdAt internal fromBot contentType
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
            _id conversationId customerId content createdAt fromBot contentType
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
}
