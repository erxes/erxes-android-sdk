package com.erxes.messenger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erxes.messenger.ErxesMessenger

/** Landing screen: branded welcome header, then entry points to chat. */
@Composable
internal fun HomeScreen(
    onViewConversations: () -> Unit,
    onNewConversation: () -> Unit,
    onClose: () -> Unit,
) {
    val connect by ErxesMessenger.connectResponse.collectAsStateWithLifecycle()
    val messages = connect?.messengerData?.messages
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
        }
    }
}
