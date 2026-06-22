package com.erxes.messenger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erxes.messenger.ErxesMessenger

/**
 * Parses a CSS-style hex color into a [Color], or null when malformed. Handles the
 * 3-digit (`#rgb`), 4-digit (`#argb`), 6-digit (`#rrggbb`), and 8-digit (`#aarrggbb`)
 * forms — the backend uses shorthand like `#000`/`#fff`.
 */
fun parseHexColor(hex: String?): Color? {
    val raw = hex?.trim()?.removePrefix("#") ?: return null
    // Expand shorthand (e.g. "abc" -> "aabbcc", "fabc" -> "ffaabbcc").
    val expanded = when (raw.length) {
        3, 4 -> raw.map { "$it$it" }.joinToString("")
        else -> raw
    }
    val value = expanded.toLongOrNull(16) ?: return null
    return when (expanded.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> null
    }
}

private val DefaultPrimary = Color(0xFF3F78D9)
private val DefaultBackground = Color(0xFFFFFFFF)

/** Readable on-color for a filled surface of [this] color. */
private fun Color.onColor(): Color = if (luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

/**
 * Theme for the messenger UI. Mirrors the iOS SDK's `effective*` color logic:
 *  - **primary** comes from the connect response's `uiOptions.color`, falling back to
 *    the configured appearance primary, then a default blue.
 *  - **background/surface** come from `uiOptions.backgroundColor` (default white).
 *  - the light/dark **color scheme is derived from the background's luminance** — the SDK
 *    presents a consistent look regardless of the host's day/night setting (as iOS does),
 *    only going dark when the server background itself is dark.
 */
@Composable
internal fun MessengerTheme(content: @Composable () -> Unit) {
    val connect by ErxesMessenger.connectResponse.collectAsStateWithLifecycle()
    val configured = ErxesMessenger.config?.appearance?.primaryColor ?: DefaultPrimary

    val primary = parseHexColor(connect?.uiOptions?.color) ?: configured
    val background = parseHexColor(connect?.uiOptions?.backgroundColor) ?: DefaultBackground
    val isLight = background.luminance() > 0.5f

    val base = if (isLight) lightColorScheme() else darkColorScheme()
    val scheme = base.copy(
        primary = primary,
        onPrimary = primary.onColor(),
        background = background,
        onBackground = background.onColor(),
        surface = background,
        onSurface = background.onColor(),
    )

    MaterialTheme(colorScheme = scheme, content = content)
}
