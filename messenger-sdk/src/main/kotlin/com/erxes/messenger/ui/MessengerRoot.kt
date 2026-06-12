package com.erxes.messenger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.ui.conversation.ChatScreen
import com.erxes.messenger.ui.conversation.ConversationListScreen
import com.erxes.messenger.ui.screens.HomeScreen
import com.erxes.messenger.ui.screens.IdentityFormScreen

/** Internal navigation target within the messenger. */
internal sealed interface MessengerScreen {
    data object Home : MessengerScreen
    data object List : MessengerScreen
    data object Auth : MessengerScreen
    data class Chat(val conversationId: String?) : MessengerScreen
}

/**
 * Lightweight in-activity navigation (no nav-compose dependency). Maintains a small
 * back stack: Home → List/Chat. [onExit] is called when backing out of the root.
 */
@Composable
internal fun MessengerRoot(
    start: MessengerScreen = MessengerScreen.Home,
    onExit: () -> Unit,
) {
    val backStack = remember { mutableStateListOf(start) }

    fun push(screen: MessengerScreen) { backStack.add(screen) }
    fun replaceTop(screen: MessengerScreen) {
        backStack[backStack.lastIndex] = screen
    }
    fun pop() {
        if (backStack.size <= 1) onExit() else backStack.removeAt(backStack.lastIndex)
    }

    // Starting a new conversation goes through the requireAuth gate when needed.
    fun startNewConversation() {
        if (ErxesMessenger.requireAuth && !ErxesMessenger.isIdentified.value) {
            push(MessengerScreen.Auth)
        } else {
            push(MessengerScreen.Chat(null))
        }
    }

    when (val current = backStack.last()) {
        is MessengerScreen.Home -> HomeScreen(
            onViewConversations = { push(MessengerScreen.List) },
            onNewConversation = { startNewConversation() },
            onClose = onExit,
        )

        is MessengerScreen.List -> ConversationListScreen(
            onOpenConversation = { id -> push(MessengerScreen.Chat(id)) },
            onNewConversation = { startNewConversation() },
            onBack = { pop() },
        )

        is MessengerScreen.Auth -> IdentityFormScreen(
            // Replace the gate with the chat so back from chat returns to the previous screen.
            onIdentified = { replaceTop(MessengerScreen.Chat(null)) },
            onBack = { pop() },
        )

        is MessengerScreen.Chat -> ChatScreen(
            conversationId = current.conversationId,
            onBack = { pop() },
        )
    }
}
