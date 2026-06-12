package com.erxes.messenger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erxes.messenger.ErxesMessenger

/** Parses a `#rrggbb` / `#aarrggbb` hex string into a [Color], or null when malformed. */
fun parseHexColor(hex: String?): Color? {
    val raw = hex?.trim()?.removePrefix("#") ?: return null
    val value = raw.toLongOrNull(16) ?: return null
    return when (raw.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> null
    }
}

private val DefaultPrimary = Color(0xFF3F78D9)

/**
 * Theme for the messenger UI. Primary color is sourced from the connect response's
 * `uiOptions.color`, falling back to the configured appearance primary.
 */
@Composable
internal fun MessengerTheme(content: @Composable () -> Unit) {
    val connect by ErxesMessenger.connectResponse.collectAsStateWithLifecycle()
    val configured = ErxesMessenger.config?.appearance?.primaryColor ?: DefaultPrimary
    val primary = parseHexColor(connect?.uiOptions?.color) ?: configured

    MaterialTheme(
        colorScheme = lightColorScheme(primary = primary, onPrimary = Color.White),
        content = content,
    )
}
