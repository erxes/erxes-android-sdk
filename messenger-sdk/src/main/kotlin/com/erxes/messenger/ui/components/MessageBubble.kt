package com.erxes.messenger.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.util.AttachmentUrl
import com.erxes.messenger.util.ContentParser

/**
 * A single chat message row: customer messages align right, agent/bot left with avatar.
 *
 * [isFirstInGroup]/[isLastInGroup] come from [com.erxes.messenger.util.MessageGrouper] and
 * shape the bubble: same-sender runs tighten their adjacent corners and the agent avatar is
 * shown only on the group's last (bottom) message, matching iOS `MessageBubble`.
 */
@Composable
internal fun MessageBubble(
    message: Message,
    fileEndpoint: String,
    isFirstInGroup: Boolean = true,
    isLastInGroup: Boolean = true,
) {
    val fromCustomer = message.isFromCustomer
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    // Message content can arrive as HTML or BlockNote JSON — flatten to display text.
    val displayText = ContentParser.toPlainText(message.content)
    val copyText = displayText.trim()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = if (isFirstInGroup) 6.dp else 1.dp, bottom = 1.dp),
        horizontalArrangement = if (fromCustomer) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Agent avatar sits at the bottom of the sender's run; reserve its width otherwise
        // so stacked bubbles stay left-aligned.
        if (!fromCustomer) {
            if (isLastInGroup) {
                if (message.fromBot) {
                    BotAvatar(sizeDp = 28, modifier = Modifier.padding(end = 8.dp))
                } else {
                    Avatar(
                        url = AttachmentUrl.resolve(message.user?.details?.avatar, fileEndpoint),
                        name = message.user?.details?.fullName,
                        sizeDp = 28,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            } else {
                Spacer(Modifier.width(36.dp))
            }
        }

        val bubbleColor =
            if (fromCustomer) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        val textColor =
            if (fromCustomer) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        val big = 20.dp
        val small = 6.dp
        val shape = if (fromCustomer) {
            RoundedCornerShape(
                topStart = big,
                topEnd = if (isFirstInGroup) big else small,
                bottomStart = big,
                bottomEnd = small,
            )
        } else {
            RoundedCornerShape(
                topStart = if (isFirstInGroup) big else small,
                topEnd = big,
                bottomStart = small,
                bottomEnd = big,
            )
        }

        Surface(
            modifier = Modifier
                .widthIn(max = 292.dp)
                .copyOnLongPress(copyText) {
                    clipboard.setText(AnnotatedString(copyText))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
            shape = shape,
            color = bubbleColor,
            contentColor = textColor,
            tonalElevation = if (fromCustomer) 0.dp else 1.dp,
        ) {
            Column {
                message.attachments.forEach { att ->
                    val url = AttachmentUrl.resolve(att.url, fileEndpoint)
                    if (att.type?.startsWith("image") == true) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = att.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(maxWidth = 260.dp, maxHeight = 360.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                            )
                        }
                    } else {
                        Text(
                            text = att.name ?: "Attachment",
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
                if (displayText.isNotBlank()) {
                    Text(
                        text = displayText,
                        color = textColor,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private fun Modifier.copyOnLongPress(text: String, onCopy: () -> Unit): Modifier {
    if (text.isBlank()) return this
    return pointerInput(text) {
        detectTapGestures(onLongPress = { onCopy() })
    }
}
