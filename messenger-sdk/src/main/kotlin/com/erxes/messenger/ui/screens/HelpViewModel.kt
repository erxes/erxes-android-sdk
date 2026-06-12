package com.erxes.messenger.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.data.MessengerRepository
import com.erxes.messenger.data.model.KbArticle
import com.erxes.messenger.data.model.KbCategory
import com.erxes.messenger.data.model.KbTopic
import com.erxes.messenger.network.KbParser
import com.erxes.messenger.util.SdkLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HelpUiState(
    val topic: KbTopic? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/** Loads the configured knowledge-base topic. Mirrors `HelpViewModel` (iOS). */
class HelpViewModel(
    private val repository: MessengerRepository = ErxesMessenger.requireRepository(),
) : ViewModel() {

    private val topicId: String?
        get() = ErxesMessenger.connectResponse.value?.messengerData?.knowledgeBaseTopicId

    private val _state = MutableStateFlow(HelpUiState())
    val state: StateFlow<HelpUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        val id = topicId
        if (id == null) {
            _state.update { it.copy(isLoading = false, error = "Knowledge base not configured") }
            return
        }
        viewModelScope.launch {
            try {
                _state.update { it.copy(topic = repository.knowledgeBase(id), isLoading = false) }
            } catch (t: Throwable) {
                SdkLog.e("knowledge base load failed", t)
                _state.update { it.copy(isLoading = false, error = t.message) }
            }
        }
    }

    /** Articles for a category, including its child categories' articles (deduped). */
    fun articlesFor(category: KbCategory): List<KbArticle> =
        KbParser.articlesFor(category, _state.value.topic?.categories.orEmpty())
}
