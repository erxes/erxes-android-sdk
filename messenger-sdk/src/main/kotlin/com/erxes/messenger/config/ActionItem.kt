package com.erxes.messenger.config

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Host-configurable chat-mode action.
 *
 * Tapping the action invokes [com.erxes.messenger.ErxesMessenger.onAction] with [id].
 * Supply either [imageVector] or [drawableRes] for the rendered icon.
 */
data class ActionItem(
    val id: String,
    val title: String,
    val imageVector: ImageVector? = null,
    @DrawableRes val drawableRes: Int? = null,
)
