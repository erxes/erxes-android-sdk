package com.erxes.messenger.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.config.DisplayMode
import com.erxes.messenger.ui.chatmode.ChatModeScreen
import com.erxes.messenger.ui.theme.MessengerTheme

/**
 * Hosts the messenger UI in its own activity. Launched by [ErxesMessenger.show].
 * Presents the AI-assistant-style shell in [DisplayMode.CHAT], otherwise the classic shell.
 */
class MessengerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isChatMode = ErxesMessenger.config?.displayMode == DisplayMode.CHAT

        // Classic shell: open directly into a conversation when one was requested,
        // else the home screen.
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)
        val start = if (conversationId != null) {
            MessengerScreen.Chat(conversationId)
        } else {
            MessengerScreen.Home
        }

        setContent {
            MessengerTheme {
                Surface {
                    if (isChatMode) {
                        ChatModeScreen(onExit = { finish() })
                    } else {
                        MessengerRoot(start = start, onExit = { finish() })
                    }
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
