package com.erxes.messenger.ui.conversation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.ui.components.ComposerBar
import com.erxes.messenger.ui.components.MessageBubble
import com.erxes.messenger.ui.components.TypingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Full chat screen for one conversation (classic shell: provides its own top bar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    conversationId: String?,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { inner ->
        ChatContent(
            conversationId = conversationId,
            vmKey = conversationId ?: "new",
            modifier = Modifier.padding(inner),
        )
    }
}

/**
 * Reusable conversation body — message list, typing indicator, error, and composer
 * (with image attachments) — without a top bar. Shared by the classic [ChatScreen] and
 * the chat-mode shell, which supply their own surrounding chrome. Mirrors `ChatContentView`
 * (iOS) minus the speech-to-text dictation.
 *
 * A distinct [vmKey] gives each conversation (and each fresh "new chat") its own
 * [ChatViewModel] instance, so its WebSocket subscription survives view switching.
 * Pass a non-blank [autoSendText] to send an opening message as soon as the (new)
 * conversation is created; set [autoOpenPicker] to open the image picker immediately
 * (used when the user tapped "+" on the new-chat home).
 */
@Composable
internal fun ChatContent(
    conversationId: String?,
    vmKey: String,
    modifier: Modifier = Modifier,
    autoSendText: String? = null,
    autoOpenPicker: Boolean = false,
) {
    val viewModel: ChatViewModel = viewModel(key = vmKey)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fileEndpoint = ErxesMessenger.config?.fileEndpoint.orEmpty()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val picked = withContext(Dispatchers.IO) { loadPickedImageAsJpeg(context, uri) }
                if (picked != null) {
                    viewModel.attach(uri.toString(), picked.bytes, picked.name, picked.mime)
                }
            }
        }
    }
    fun openPicker() = picker.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )

    LaunchedEffect(vmKey) {
        viewModel.open(conversationId)
        autoSendText?.takeIf { it.isNotBlank() }?.let { viewModel.send(it) }
        if (autoOpenPicker) openPicker()
    }

    // Keep the newest message in view.
    LaunchedEffect(state.messages.size, state.isBotTyping) {
        val count = state.messages.size + if (state.isBotTyping) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    MessageBubble(message = msg, fileEndpoint = fileEndpoint)
                }
                if (state.isBotTyping) {
                    item(key = "typing") { TypingIndicator() }
                }
            }
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (state.pendingAttachments.isNotEmpty()) {
            PendingAttachmentsStrip(
                attachments = state.pendingAttachments,
                onRemove = viewModel::removePending,
            )
        }

        ChatComposer(
            enabled = !state.isSending,
            hasReadyAttachment = state.pendingAttachments.any { it.uploaded != null },
            onPickImage = { openPicker() },
            onSend = viewModel::send,
        )
    }
}

@Composable
private fun ChatComposer(
    enabled: Boolean,
    hasReadyAttachment: Boolean,
    onPickImage: () -> Unit,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val canSend = text.isNotBlank() || hasReadyAttachment
    ComposerBar(
        text = text,
        onTextChange = { text = it },
        onSend = {
            if (canSend) {
                onSend(text)
                text = ""
            }
        },
        enabled = enabled,
        canSend = canSend,
        onAttach = onPickImage,
    )
}

@Composable
private fun PendingAttachmentsStrip(
    attachments: List<PendingAttachment>,
    onRemove: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(attachments, key = { it.id }) { pending ->
            Box {
                AsyncImage(
                    model = pending.previewUri,
                    contentDescription = "Attachment",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                if (pending.isUploading) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)) }
                } else {
                    IconButton(
                        onClick = { onRemove(pending.id) },
                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove attachment",
                            tint = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(2.dp),
                        )
                    }
                }
            }
        }
    }
}

/** A picked image decoded to JPEG bytes, ready to upload (the gateway accepts PNG/JPEG). */
private data class PickedImage(val bytes: ByteArray, val name: String, val mime: String)

/**
 * Reads the picked [uri] and re-encodes it as JPEG so the upload is always an accepted
 * type (mirrors iOS exporting picked photos as JPEG). Returns null if it can't be decoded.
 */
private fun loadPickedImageAsJpeg(context: Context, uri: Uri): PickedImage? {
    val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.size) ?: return null
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
    return PickedImage(out.toByteArray(), "image-${System.currentTimeMillis()}.jpg", "image/jpeg")
}
