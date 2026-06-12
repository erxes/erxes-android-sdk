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
 */
data class MessengerConfig(
    val endpoint: String,
    val integrationId: String,
    val fileEndpoint: String = endpoint,
    val cachedCustomerId: String? = null,
    val appearance: Appearance = Appearance(),
)

/** Visual customization for the launcher and messenger. Mirrors `MessengerConfig.Appearance`. */
data class Appearance(
    val primaryColor: Color = Color(red = 0.25f, green = 0.47f, blue = 0.85f, alpha = 1f),
)
