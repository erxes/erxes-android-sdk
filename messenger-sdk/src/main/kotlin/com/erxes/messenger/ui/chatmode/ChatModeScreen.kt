package com.erxes.messenger.ui.chatmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.ui.components.ComposerBar
import com.erxes.messenger.ui.conversation.ChatContent
import com.erxes.messenger.ui.conversation.ConversationListViewModel
import com.erxes.messenger.ui.screens.IdentityFormScreen
import kotlinx.coroutines.launch

/** What the main pane is currently showing. */
private sealed interface ChatTarget {
    /** New-chat home: greeting + composer, no active conversation. */
    data object Home : ChatTarget

    /** A brand-new conversation being composed; [nonce] keeps each draft's VM distinct. */
    data class Draft(val nonce: Int, val autoSend: String?, val openPicker: Boolean = false) : ChatTarget

    /** An existing conversation opened from the drawer. */
    data class Existing(val conversation: Conversation) : ChatTarget
}

/**
 * AI-assistant-style messenger shell (`displayMode == CHAT`). Mirrors `MessengerChatModeView`
 * (iOS): a new-chat home, a left drawer holding the conversation list, and inline chats.
 *
 * Reuses [ChatContent] (message list + composer) and [ConversationListViewModel] so the
 * underlying chat plumbing — realtime subscriptions, optimistic send — is shared with the
 * classic shell.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatModeScreen(onExit: () -> Unit) {
    // Chat mode auto-presents before the connect handshake finishes, so show a spinner
    // on the themed background until ready, then fade in the shell (mirrors iOS).
    val isReady by ErxesMessenger.isReady.collectAsStateWithLifecycle()
    if (!isReady) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        return
    }

    val listVM: ConversationListViewModel = viewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var target by remember { mutableStateOf<ChatTarget>(ChatTarget.Home) }
    var nonce by remember { mutableIntStateOf(0) }

    // requireAuth gate: when a new chat is requested but the visitor isn't identified,
    // show the identity form first, remembering the action (auto-send text and/or open
    // the image picker) to resume afterwards.
    var pendingAuthText by remember { mutableStateOf<String?>(null) }
    var pendingAuthPicker by remember { mutableStateOf(false) }
    var showAuth by remember { mutableStateOf(false) }
    val isIdentified by ErxesMessenger.isIdentified.collectAsStateWithLifecycle()

    fun closeDrawer() = scope.launch { drawerState.close() }

    fun startDraft(autoSend: String?, openPicker: Boolean = false) {
        nonce += 1
        target = ChatTarget.Draft(nonce, autoSend, openPicker)
    }

    // Returns to the new-chat home (greeting + composer). The draft conversation is
    // only created once the visitor actually sends, mirroring iOS `startNewChat()`.
    fun goHome() {
        closeDrawer()
        target = ChatTarget.Home
    }

    // Starting a chat from home (sending text, or tapping "+" to attach) creates the
    // draft conversation, routing through the requireAuth gate first when required.
    fun startFromHome(autoSend: String?, openPicker: Boolean) {
        if (ErxesMessenger.requireAuth && !isIdentified) {
            pendingAuthText = autoSend
            pendingAuthPicker = openPicker
            showAuth = true
        } else {
            startDraft(autoSend, openPicker)
        }
    }

    if (showAuth) {
        IdentityFormScreen(
            onIdentified = {
                showAuth = false
                startDraft(pendingAuthText, pendingAuthPicker)
                pendingAuthText = null
                pendingAuthPicker = false
            },
            onBack = {
                showAuth = false
                pendingAuthText = null
                pendingAuthPicker = false
            },
        )
        return
    }

    // Refresh the conversation list whenever the drawer opens so a just-created
    // conversation shows up under RECENTS.
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Open) listVM.refresh()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                listVM = listVM,
                activeId = (target as? ChatTarget.Existing)?.conversation?.id,
                onNewChat = { goHome() },
                onOpenConversation = { conv ->
                    target = ChatTarget.Existing(conv)
                    closeDrawer()
                },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    ChatModeTopBar(
                        title = target.title(),
                        onMenu = { scope.launch { drawerState.open() } },
                        onClose = onExit,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            },
        ) { inner ->
            Box(modifier = Modifier.fillMaxSize().padding(inner)) {
                when (val t = target) {
                    is ChatTarget.Home -> NewChatHome(
                        onSend = { startFromHome(autoSend = it, openPicker = false) },
                        // Attaching an image skips the requireAuth gate — it opens the
                        // picker straight into a draft conversation.
                        onAttach = { startDraft(autoSend = null, openPicker = true) },
                    )

                    is ChatTarget.Draft -> ChatContent(
                        conversationId = null,
                        vmKey = "draft-${t.nonce}",
                        autoSendText = t.autoSend,
                        autoOpenPicker = t.openPicker,
                    )

                    is ChatTarget.Existing -> ChatContent(
                        conversationId = t.conversation.id,
                        vmKey = t.conversation.id,
                    )
                }
            }
        }
    }
}

private fun ChatTarget.title(): String = when (this) {
    is ChatTarget.Existing -> "Conversation"
    else -> "New chat"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatModeTopBar(
    title: String,
    onMenu: () -> Unit,
    onClose: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "Conversations")
            }
        },
        actions = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

/** New-chat home: greeting headline/subtitle (from the connect payload) + a composer. */
@Composable
private fun NewChatHome(onSend: (String) -> Unit, onAttach: () -> Unit) {
    val messages = ErxesMessenger.connectResponse.collectAsStateWithLifecycle().value
        ?.messengerData?.messages

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp).padding(bottom = 16.dp),
            )
            Text(
                text = messages?.greetTitle?.takeIf { it.isNotBlank() } ?: "How can I help?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            messages?.greet?.takeIf { it.isNotBlank() }?.let { greet ->
                Text(
                    text = greet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        HomeComposer(onSend = onSend, onAttach = onAttach)
    }
}

@Composable
private fun HomeComposer(onSend: (String) -> Unit, onAttach: () -> Unit) {
    var text by remember { mutableStateOf("") }
    ComposerBar(
        text = text,
        onTextChange = { text = it },
        onSend = {
            if (text.isNotBlank()) {
                onSend(text)
                text = ""
            }
        },
        placeholder = "Ask anything…",
        onAttach = onAttach,
    )
}

@Composable
private fun ChatDrawer(
    listVM: ConversationListViewModel,
    activeId: String?,
    onNewChat: () -> Unit,
    onOpenConversation: (Conversation) -> Unit,
) {
    val state by listVM.state.collectAsStateWithLifecycle()

    ModalDrawerSheet {
        Text(
            text = "Chats",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 12.dp),
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            label = { Text("New chat") },
            selected = false,
            onClick = onNewChat,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            state.conversations.isEmpty() -> Text(
                text = state.error ?: "No conversations yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            )
            else -> {
                Text(
                    text = "RECENTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp, bottom = 4.dp),
                )
                LazyColumn {
                    items(state.conversations, key = { it.id }) { conv ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = conv.lastMessage?.content?.takeIf { it.isNotBlank() }
                                        ?: conv.content?.takeIf { it.isNotBlank() }
                                        ?: "Conversation",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            selected = conv.id == activeId,
                            onClick = { onOpenConversation(conv) },
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
