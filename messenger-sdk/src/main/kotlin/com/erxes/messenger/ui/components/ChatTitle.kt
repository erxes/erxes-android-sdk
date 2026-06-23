package com.erxes.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.util.AttachmentUrl

/** Conversation title for chat chrome: agent, bot, or plain fallback. */
@Composable
internal fun ChatTitle(
    conversation: Conversation,
    isBot: Boolean,
    fileEndpoint: String,
    modifier: Modifier = Modifier,
) {
    val user = conversation.participatedUsers.firstOrNull()
    when {
        user != null -> AgentTitle(
            name = user.details?.displayName ?: "Support",
            avatarUrl = AttachmentUrl.resolve(user.details?.avatar, fileEndpoint),
            isOnline = user.isOnline,
            modifier = modifier,
        )
        isBot -> BotTitle(modifier)
        else -> Text(
            text = "Conversation",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    }
}

@Composable
private fun AgentTitle(
    name: String,
    avatarUrl: String?,
    isOnline: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        AvatarWithStatus(url = avatarUrl, name = name, isOnline = isOnline, sizeDp = 34)
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOnline) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BotTitle(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF7A40E6),
                            Color(0xFF471AB3),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp),
            )
        }

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AI agent",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "AI",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
            Text(
                text = "Automated - replies instantly",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
