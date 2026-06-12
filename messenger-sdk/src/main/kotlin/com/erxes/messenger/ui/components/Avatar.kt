package com.erxes.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/** A circular avatar that shows the agent's image, falling back to an initial. */
@Composable
internal fun Avatar(
    url: String?,
    name: String?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 32,
) {
    val shape = CircleShape
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(sizeDp.dp).clip(shape),
        )
    } else {
        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (name?.trim()?.firstOrNull()?.uppercase() ?: "?"),
                color = MaterialTheme.colorScheme.primary,
                fontSize = (sizeDp / 2.2).sp,
            )
        }
    }
}
