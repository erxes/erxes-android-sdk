package com.erxes.messenger.ui.chatmode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.config.ActionItem
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.ui.components.AvatarWithStatus
import com.erxes.messenger.ui.components.BotAvatar
import com.erxes.messenger.ui.components.BotBadge
import com.erxes.messenger.ui.components.ChatTitle
import com.erxes.messenger.ui.components.ComposerBar
import com.erxes.messenger.ui.conversation.ChatContent
import com.erxes.messenger.ui.conversation.ChatViewModel
import com.erxes.messenger.ui.conversation.ConversationListViewModel
import com.erxes.messenger.ui.screens.IdentityFormScreen
import com.erxes.messenger.util.AttachmentUrl
import com.erxes.messenger.util.ContentParser
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

    // Dismiss the keyboard when the user taps outside the composer or scrolls the
    // content — mirrors iOS `dismissKeyboardOnTap()` / `dismissKeyboardOnVerticalDrag()`.
    val focusManager = LocalFocusManager.current
    val dismissKeyboardOnScroll = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y != 0f) focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

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
    // conversation shows up under RECENTS. Also dismiss the keyboard as the drawer
    // starts opening (covers the edge-swipe gesture, not just the menu button).
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            focusManager.clearFocus()
            listVM.refresh()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                listVM = listVM,
                activeId = (target as? ChatTarget.Existing)?.conversation?.id,
                drawerActions = ErxesMessenger.config?.drawerActions.orEmpty(),
                onAction = { id ->
                    ErxesMessenger.onAction?.invoke(id)
                    closeDrawer()
                },
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
                        target = target,
                        onMenu = {
                            focusManager.clearFocus()
                            scope.launch { drawerState.open() }
                        },
                        onClose = onExit,
                        onNewChat = { goHome() },
                        homeActions = ErxesMessenger.config?.homeActions.orEmpty(),
                        showCloseButton = ErxesMessenger.config?.showCloseButton == true,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            },
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .consumeWindowInsets(inner)
                    .imePadding()
                    .nestedScroll(dismissKeyboardOnScroll)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
            ) {
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
                        chatModeAffordances = true,
                    )

                    is ChatTarget.Existing -> ChatContent(
                        conversationId = t.conversation.id,
                        vmKey = t.conversation.id,
                        chatModeAffordances = true,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatModeTopBar(
    target: ChatTarget,
    onMenu: () -> Unit,
    onClose: () -> Unit,
    onNewChat: () -> Unit,
    homeActions: List<ActionItem>,
    showCloseButton: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 12.dp),
        ) {
            FilledTonalIconButton(
                onClick = onMenu,
                modifier = Modifier.align(Alignment.CenterStart).size(42.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Conversations")
            }

            Box(modifier = Modifier.align(Alignment.Center)) {
                when (target) {
                    is ChatTarget.Existing -> {
                        val chatVM: ChatViewModel = viewModel(key = target.conversation.id)
                        val chatState by chatVM.state.collectAsStateWithLifecycle()
                        ChatTitle(
                            conversation = target.conversation,
                            isBot = chatState.isBot,
                            fileEndpoint = ErxesMessenger.config?.fileEndpoint.orEmpty(),
                        )
                    }
                    else -> Text(
                        text = "New chat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Mirrors iOS `MessengerChatModeView.topBar`: with no active conversation
                // (home or draft) we show the host-configured home actions; inside an
                // existing conversation we swap to a single new-chat affordance.
                if (target is ChatTarget.Existing) {
                    FilledTonalIconButton(
                        onClick = onNewChat,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "New chat")
                    }
                } else {
                    homeActions.forEach { item ->
                        IconButton(onClick = { ErxesMessenger.onAction?.invoke(item.id) }) {
                            ActionIcon(item)
                        }
                    }
                }

                // Close the full-screen messenger — only when there's a launcher to return
                // to (hidden in launcher-less chat mode). Mirrors iOS `showsCloseButton`.
                if (showCloseButton) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }
        }
    }
}

/** New-chat home: greeting headline/subtitle (from the connect payload) + a composer. */
@Composable
private fun NewChatHome(onSend: (String) -> Unit, onAttach: () -> Unit) {
    val messages = ErxesMessenger.connectResponse.collectAsStateWithLifecycle().value
        ?.messengerData?.messages

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                tonalElevation = 4.dp,
                modifier = Modifier.padding(bottom = 18.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                )
            }
            Text(
                text = messages?.greetTitle?.takeIf { it.isNotBlank() } ?: "How can I help?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            messages?.greet?.takeIf { it.isNotBlank() }?.let { greet ->
                Text(
                    text = greet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp),
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
    drawerActions: List<ActionItem>,
    onAction: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenConversation: (Conversation) -> Unit,
) {
    val state by listVM.state.collectAsStateWithLifecycle()

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(),
        drawerShape = RectangleShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 88.dp)) {
                Text(
                    text = "Chats",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, top = 26.dp, bottom = 14.dp),
                )

                drawerActions.forEach { item ->
                    NavigationDrawerItem(
                        icon = { ActionIcon(item) },
                        label = { Text(item.title) },
                        selected = false,
                        onClick = { onAction(item.id) },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                )

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
                                DrawerConversationRow(
                                    conversation = conv,
                                    selected = conv.id == activeId,
                                    onClick = { onOpenConversation(conv) },
                                )
                            }
                        }
                    }
                }
            }

            // Floating "New chat" pill, bottom-right — mirrors iOS
            // (square.and.pencil compose icon + label).
            ExtendedFloatingActionButton(
                onClick = onNewChat,
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text = { Text("New chat") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun DrawerConversationRow(
    conversation: Conversation,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fileEndpoint = ErxesMessenger.config?.fileEndpoint.orEmpty()
    val user = conversation.participatedUsers.firstOrNull()
    val lastMessage = conversation.lastMessage
    // Mirror iOS: a conversation reads as a bot thread when its newest message came from the bot.
    val isBot = lastMessage?.fromBot == true
    val title = when {
        isBot -> "AI Agent"
        user != null -> user.details?.displayName ?: "Support"
        else -> "Conversation"
    }
    val preview = lastMessage?.content?.let { ContentParser.toPlainText(it) }?.takeIf { it.isNotBlank() }
        ?: conversation.content?.let { ContentParser.toPlainText(it) }?.takeIf { it.isNotBlank() }
        ?: if (lastMessage?.attachments?.isNotEmpty() == true) "Attachment" else "No messages yet"
    val avatarUrl = AttachmentUrl.resolve(user?.details?.avatar ?: lastMessage?.user?.details?.avatar, fileEndpoint)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f) else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isBot) {
                BotAvatar(sizeDp = 38)
            } else {
                AvatarWithStatus(
                    url = avatarUrl,
                    name = title,
                    isOnline = user?.isOnline ?: false,
                    sizeDp = 38,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isBot) {
                        BotBadge(modifier = Modifier.padding(start = 6.dp))
                    }
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(item: ActionItem) {
    when {
        item.imageVector != null -> Icon(item.imageVector, contentDescription = item.title)
        item.drawableRes != null -> Icon(painterResource(item.drawableRes), contentDescription = item.title)
        else -> Icon(Icons.Filled.Star, contentDescription = item.title)
    }
}
