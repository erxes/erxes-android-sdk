package com.erxes.messenger.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.Ticket
import com.erxes.messenger.data.model.TicketConfig
import com.erxes.messenger.data.model.TicketTag
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketsUiState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Loads the customer's tickets. The active [ticketConfig] comes from the connect response. */
class TicketsViewModel(
    private val repository: MessengerRepository = ErxesMessenger.requireRepository(),
) : ViewModel() {

    val ticketConfig: TicketConfig?
        get() = ErxesMessenger.connectResponse.value?.messengerData?.ticketConfig

    private val _state = MutableStateFlow(TicketsUiState())
    val state: StateFlow<TicketsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                _state.update { it.copy(tickets = repository.tickets(), isLoading = false) }
            } catch (t: Throwable) {
                SdkLog.e("tickets load failed", t)
                _state.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }
}

data class CreateTicketUiState(
    val tags: List<TicketTag> = emptyList(),
    val selectedTagIds: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

/** Backs the create-ticket form: loads tags when enabled and submits the ticket. */
class CreateTicketViewModel(
    private val repository: MessengerRepository = ErxesMessenger.requireRepository(),
) : ViewModel() {

    val config: TicketConfig? = ErxesMessenger.connectResponse.value?.messengerData?.ticketConfig

    private val _state = MutableStateFlow(CreateTicketUiState())
    val state: StateFlow<CreateTicketUiState> = _state.asStateFlow()

    init {
        val cfg = config
        if (cfg != null && cfg.formFields.tags?.isShow == true) {
            viewModelScope.launch {
                runCatching { repository.ticketTags(cfg.id, cfg.parentId) }
                    .onSuccess { tags -> _state.update { it.copy(tags = tags) } }
            }
        }
    }

    fun toggleTag(id: String) {
        _state.update {
            val next = if (id in it.selectedTagIds) it.selectedTagIds - id else it.selectedTagIds + id
            it.copy(selectedTagIds = next)
        }
    }

    fun submit(name: String, description: String) {
        val cfg = config ?: return
        if (name.isBlank() || _state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                repository.createTicket(
                    name = name.trim(),
                    description = description.trim().ifBlank { null },
                    statusId = cfg.selectedStatusId,
                    tagIds = _state.value.selectedTagIds.toList(),
                )
                _state.update { it.copy(isSubmitting = false, done = true) }
            } catch (t: Throwable) {
                SdkLog.e("createTicket failed", t)
                _state.update { it.copy(isSubmitting = false, error = t.message ?: "Could not create ticket") }
            }
        }
    }
}
