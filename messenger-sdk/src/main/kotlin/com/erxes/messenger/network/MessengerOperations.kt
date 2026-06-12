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
}
