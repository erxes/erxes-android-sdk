package com.erxes.messenger.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.ui.conversation.ChatScreen
import com.erxes.messenger.ui.theme.MessengerTheme

/**
 * Hosts the messenger UI in its own activity. Launched by [ErxesMessenger.show].
 * Phase 5a opens straight into the chat screen; the conversation list/home is added in 5b.
 */
class MessengerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Resume the last conversation if there is one, else start fresh.
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)

        setContent {
            MessengerTheme {
                Surface {
                    ChatScreen(
                        conversationId = conversationId,
                        onBack = { finish() },
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_CONVERSATION_ID = "erxes.conversationId"

        fun intent(context: Context, conversationId: String? = null): Intent =
            Intent(context, MessengerActivity::class.java).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                conversationId?.let { putExtra(EXTRA_CONVERSATION_ID, it) }
            }
    }
}
