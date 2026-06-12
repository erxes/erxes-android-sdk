package com.erxes.messenger.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.Conversation
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Loads the customer's conversation list. Mirrors `ConversationListViewModel` (iOS). */
class ConversationListViewModel(
    private val repository: MessengerRepository = ErxesMessenger.requireRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListUiState())
    val state: StateFlow<ConversationListUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.conversations()
                _state.update { it.copy(conversations = list, isLoading = false) }
            } catch (t: Throwable) {
                SdkLog.e("conversations load failed", t)
                _state.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }
}
