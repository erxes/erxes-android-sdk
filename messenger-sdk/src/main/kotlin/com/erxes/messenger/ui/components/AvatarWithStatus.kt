package com.erxes.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Circular avatar with an optional online status dot. Mirrors iOS `AvatarWithStatusView`. */
@Composable
internal fun AvatarWithStatus(
    url: String?,
    name: String?,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Int = 36,
) {
    Box(modifier = modifier.size(sizeDp.dp)) {
        Avatar(url = url, name = name, sizeDp = sizeDp)

        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((sizeDp * 0.28f).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E))
                    .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape),
            )
        }
    }
}
