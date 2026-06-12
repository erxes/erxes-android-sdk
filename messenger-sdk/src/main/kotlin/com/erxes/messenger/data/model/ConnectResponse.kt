package com.erxes.messenger.data.model

/**
 * Parsed result of the `widgetsMessengerConnect` handshake. Mirrors the iOS
 * `ConnectResponse` plus the nested `UIOptions` / `MessengerData` it carries.
 * Only the fields the client currently consumes are modelled; ticketConfig and
 * websiteApps are deferred to their feature phases (see ROADMAP).
 */
data class ConnectResponse(
    val integrationId: String,
    val customerId: String?,
    val visitorId: String?,
    val languageCode: String?,
    val uiOptions: UiOptions,
    val messengerData: MessengerData,
)

/** Visual options. `color` comes from `uiOptions.primary.DEFAULT` (hex string). */
data class UiOptions(
    val color: String? = null,
    val textColor: String? = null,
    val backgroundColor: String? = null,
    val wallpaper: String? = null,
    val logo: String? = null,
)

data class MessengerData(
    val supporterIds: List<String> = emptyList(),
    val notifyCustomer: Boolean = false,
    val availabilityMethod: String? = null,
    val isOnline: Boolean = false,
    val onlineHours: List<OnlineHour> = emptyList(),
    val timezone: String? = null,
    val messages: GreetingMessages = GreetingMessages(),
    val links: SocialLinks = SocialLinks(),
    val knowledgeBaseTopicId: String? = null,
    val responseRate: String? = null,
    /** Ticket pipeline config; non-null enables the Tickets feature. */
    val ticketConfig: TicketConfig? = null,
    val requireAuth: Boolean = false,
    val showChat: Boolean = true,
    val showLauncher: Boolean = true,
    val forceLogoutWhenResolve: Boolean = false,
    val showVideoCallRequest: Boolean = false,
)

data class OnlineHour(val day: String, val from: String, val to: String)

data class GreetingMessages(
    /** `messages.greetings.title` — hero headline. */
    val greetTitle: String? = null,
    /** `messages.greetings.message` — hero subtitle. */
    val greet: String? = null,
    val away: String? = null,
    val thank: String? = null,
    val welcome: String? = null,
)

data class SocialLinks(
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val linkedin: String? = null,
    val discord: String? = null,
    val github: String? = null,
)
