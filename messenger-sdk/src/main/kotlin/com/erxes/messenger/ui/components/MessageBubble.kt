package com.erxes.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.util.AttachmentUrl

/** A single chat message row: customer messages align right, agent/bot left with avatar. */
@Composable
internal fun MessageBubble(message: Message, fileEndpoint: String) {
    val fromCustomer = message.isFromCustomer
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (fromCustomer) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!fromCustomer) {
            Avatar(
                url = AttachmentUrl.resolve(message.user?.details?.avatar, fileEndpoint),
                name = message.user?.details?.fullName,
                sizeDp = 28,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        val bubbleColor =
            if (fromCustomer) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        val textColor =
            if (fromCustomer) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        val shape = RoundedCornerShape(
            topStart = 16.dp, topEnd = 16.dp,
            bottomStart = if (fromCustomer) 16.dp else 4.dp,
            bottomEnd = if (fromCustomer) 4.dp else 16.dp,
        )

        Column(modifier = Modifier.widthIn(max = 280.dp).clip(shape).background(bubbleColor)) {
            message.attachments.forEach { att ->
                val url = AttachmentUrl.resolve(att.url, fileEndpoint)
                if (att.type?.startsWith("image") == true) {
                    AsyncImage(
                        model = url,
                        contentDescription = att.name,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = att.name ?: "Attachment",
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            if (message.content.isNotBlank()) {
                Text(
                    text = message.content,
                    color = textColor,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}
