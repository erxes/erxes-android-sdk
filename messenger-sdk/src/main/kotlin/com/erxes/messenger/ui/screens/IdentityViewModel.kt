package com.erxes.messenger.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.MessengerRepository.ContactKind
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdentityUiState(
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

/**
 * Submits the requireAuth identity (or get-notified opt-in). On success flips [IdentityUiState.done],
 * which the caller observes to proceed into the conversation.
 */
class IdentityViewModel : ViewModel() {

    private val _state = MutableStateFlow(IdentityUiState())
    val state: StateFlow<IdentityUiState> = _state.asStateFlow()

    fun submit(kind: ContactKind, value: String, name: String) {
        if (value.isBlank() || _state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                val (first, last) = splitName(name)
                ErxesMessenger.identify(kind, value.trim(), first, last)
                _state.update { it.copy(isSubmitting = false, done = true) }
            } catch (t: Throwable) {
                SdkLog.e("identify failed", t)
                _state.update { it.copy(isSubmitting = false, error = t.message ?: "Could not save your details") }
            }
        }
    }

    private fun splitName(name: String): Pair<String?, String?> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null to null
        val parts = trimmed.split(Regex("\\s+"), limit = 2)
        return parts[0] to parts.getOrNull(1)
    }
}
