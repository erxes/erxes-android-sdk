package com.erxes.messenger.config

import androidx.compose.ui.graphics.Color

/**
 * Configuration for the erxes messenger. Mirrors `MessengerConfig` in the iOS SDK.
 *
 * @property endpoint Base URL of your erxes server, e.g. `https://app.example.io`.
 * @property integrationId Integration ID from Dashboard → Settings → Integrations.
 * @property fileEndpoint Base URL for files/GraphQL; defaults to [endpoint]. Override only
 *   if your file server differs. Note: GraphQL is served from this host (see docs/PROTOCOL.md).
 * @property cachedCustomerId Optional customer id from a previous session.
 * @property appearance Visual customization.
 * @property displayMode Which UI shell to present. Defaults to [DisplayMode.CLASSIC]
 *   so existing hosts are unaffected.
 * @property homeActions Chat-mode header actions shown on the new-chat home.
 * @property drawerActions Chat-mode drawer action rows shown above recents.
 */
data class MessengerConfig(
    val endpoint: String,
    val integrationId: String,
    val fileEndpoint: String = endpoint,
    val cachedCustomerId: String? = null,
    val appearance: Appearance = Appearance(),
    val displayMode: DisplayMode = DisplayMode.CLASSIC,
    val homeActions: List<ActionItem> = emptyList(),
    val drawerActions: List<ActionItem> = emptyList(),
)

/**
 * Which UI shell the messenger presents. Mirrors `DisplayMode` in the iOS SDK.
 */
enum class DisplayMode {
    /** The classic widget (Home / Messages / Help / Tickets) with a floating launcher. */
    CLASSIC,

    /**
     * An AI-assistant-style shell: a new-chat home, a left drawer holding the
     * conversation list, and inline full-screen chats.
     */
    CHAT,
}

/** Visual customization for the launcher and messenger. Mirrors `MessengerConfig.Appearance`. */
data class Appearance(
    val primaryColor: Color = Color(red = 0.25f, green = 0.47f, blue = 0.85f, alpha = 1f),
)
