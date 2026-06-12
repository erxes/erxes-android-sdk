package com.erxes.messenger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.erxes.messenger.ui.conversation.ChatScreen
import com.erxes.messenger.ui.conversation.ConversationListScreen
import com.erxes.messenger.ui.screens.HomeScreen

/** Internal navigation target within the messenger. */
internal sealed interface MessengerScreen {
    data object Home : MessengerScreen
    data object List : MessengerScreen
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
    fun pop() {
        if (backStack.size <= 1) onExit() else backStack.removeAt(backStack.lastIndex)
    }

    when (val current = backStack.last()) {
        is MessengerScreen.Home -> HomeScreen(
            onViewConversations = { push(MessengerScreen.List) },
            onNewConversation = { push(MessengerScreen.Chat(null)) },
            onClose = onExit,
        )

        is MessengerScreen.List -> ConversationListScreen(
            onOpenConversation = { id -> push(MessengerScreen.Chat(id)) },
            onNewConversation = { push(MessengerScreen.Chat(null)) },
            onBack = { pop() },
        )

        is MessengerScreen.Chat -> ChatScreen(
            conversationId = current.conversationId,
            onBack = { pop() },
        )
    }
}
