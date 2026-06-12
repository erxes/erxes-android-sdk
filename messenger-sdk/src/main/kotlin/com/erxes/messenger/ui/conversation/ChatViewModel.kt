package com.erxes.messenger.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.Message
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI state for the chat screen. */
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val isBotTyping: Boolean = false,
    val error: String? = null,
)

/**
 * Drives one conversation: loads its history, sends messages, and folds in realtime
 * inserts/typing. Mirrors `ChatViewModel` (iOS). A null conversation id means "new
 * conversation" — it is assigned by the server on the first send.
 */
class ChatViewModel(
    private val repository: MessengerRepository = ErxesMessenger.requireRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var conversationId: String? = null
    private var subscriptionJob: Job? = null

    /** Opens [conversationId]; pass null to start a fresh conversation. */
    fun open(conversationId: String?) {
        this.conversationId = conversationId
        if (conversationId == null) {
            _state.update { it.copy(isLoading = false, messages = emptyList()) }
            return
        }
        loadHistory(conversationId)
        subscribe(conversationId)
    }

    private fun loadHistory(id: String) {
        viewModelScope.launch {
            try {
                val detail = repository.conversationDetail(id)
                _state.update { it.copy(messages = detail?.messages.orEmpty(), isLoading = false) }
                repository.markRead(id)
            } catch (t: Throwable) {
                SdkLog.e("loadHistory failed", t)
                _state.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }

    private fun subscribe(id: String) {
        subscriptionJob?.cancel()
        subscriptionJob = viewModelScope.launch {
            launch {
                repository.messageStream(id).collect { incoming -> appendUnique(incoming, fromAgentMarkRead = id) }
            }
            launch {
                repository.botTypingStream(id).collect { typing -> _state.update { it.copy(isBotTyping = typing) } }
            }
        }
    }

    private fun appendUnique(message: Message, fromAgentMarkRead: String) {
        var added = false
        _state.update { current ->
            if (current.messages.any { it.id == message.id }) current
            else { added = true; current.copy(messages = current.messages + message) }
        }
        // An inbound (agent/bot) message while the chat is open should be marked read.
        if (added && !message.isFromCustomer) {
            viewModelScope.launch { runCatching { repository.markRead(fromAgentMarkRead) } }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isSending) return
        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            try {
                val sent = repository.sendMessage(conversationId, trimmed)
                appendUnique(sent, fromAgentMarkRead = sent.conversationId ?: conversationId.orEmpty())
                // First message in a new conversation — adopt the assigned id and subscribe.
                if (conversationId == null) {
                    sent.conversationId?.let { newId ->
                        conversationId = newId
                        subscribe(newId)
                    }
                }
                _state.update { it.copy(isSending = false) }
            } catch (t: Throwable) {
                SdkLog.e("send failed", t)
                _state.update { it.copy(isSending = false, error = t.message) }
            }
        }
    }
}
