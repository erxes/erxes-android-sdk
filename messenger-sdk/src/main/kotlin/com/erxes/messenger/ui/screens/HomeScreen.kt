package com.erxes.messenger.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erxes.messenger.ErxesMessenger

/** Landing screen: branded welcome header, then entry points to chat. */
@Composable
internal fun HomeScreen(
    onViewConversations: () -> Unit,
    onNewConversation: () -> Unit,
    onViewTickets: () -> Unit,
    onViewHelp: () -> Unit,
    onClose: () -> Unit,
) {
    val connect by ErxesMessenger.connectResponse.collectAsStateWithLifecycle()
    val messages = connect?.messengerData?.messages
    val hasTickets = connect?.messengerData?.ticketConfig != null
    val hasHelp = !connect?.messengerData?.knowledgeBaseTopicId.isNullOrBlank()
    val title = messages?.greetTitle?.takeIf { it.isNotBlank() } ?: "Hi there 👋"
    val subtitle = messages?.greet?.takeIf { it.isNotBlank() } ?: "How can we help you today?"

    Column(modifier = Modifier.fillMaxSize()) {
        // Branded header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(PaddingValues(start = 20.dp, end = 8.dp, top = 24.dp, bottom = 28.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(text = subtitle, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyLarge)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = onNewConversation, modifier = Modifier.fillMaxWidth()) {
                Text("Start a conversation")
            }
            OutlinedButton(onClick = onViewConversations, modifier = Modifier.fillMaxWidth()) {
                Text("View your conversations")
            }
            if (hasTickets) {
                OutlinedButton(onClick = onViewTickets, modifier = Modifier.fillMaxWidth()) {
                    Text("Support tickets")
                }
            }
            if (hasHelp) {
                OutlinedButton(onClick = onViewHelp, modifier = Modifier.fillMaxWidth()) {
                    Text("Browse help articles")
                }
            }

            connect?.messengerData?.links?.let { SocialLinksRow(it) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun SocialLinksRow(links: com.erxes.messenger.data.model.SocialLinks) {
    val context = LocalContext.current
    val entries = listOfNotNull(
        links.facebook?.let { "Facebook" to it },
        links.instagram?.let { "Instagram" to it },
        links.twitter?.let { "X" to it },
        links.youtube?.let { "YouTube" to it },
        links.linkedin?.let { "LinkedIn" to it },
        links.discord?.let { "Discord" to it },
        links.github?.let { "GitHub" to it },
    ).filter { it.second.isNotBlank() }
    if (entries.isEmpty()) return

    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        entries.forEach { (label, url) ->
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)),
                        )
                    }
                },
            )
        }
    }
}
