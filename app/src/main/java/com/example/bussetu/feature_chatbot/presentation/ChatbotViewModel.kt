package com.example.bussetu.feature_chatbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bussetu.feature_chatbot.domain.model.ChatMessage
import com.example.bussetu.feature_chatbot.domain.repository.ChatbotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatbotUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = "bot",
            text = "Hello! 👋 I'm the **BusSetu Assistant**.\n\nI can help you find bus routes, stops, and live tracking info.\n\nType **help** to see all available commands!",
            icon = "bus"
        )
    ),
    val isTyping: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val repository: ChatbotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(sender = "user", text = text)
        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                isTyping = true,
                error = null
            ) 
        }

        viewModelScope.launch {
            repository.sendMessage(text)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + response,
                            isTyping = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            messages = it.messages + ChatMessage(
                                sender = "bot",
                                text = "⚠️ Sorry, I had trouble fetching that data. Please make sure the backend server is running and try again.",
                                icon = "help"
                            ),
                            isTyping = false,
                            error = error.localizedMessage
                        )
                    }
                }
        }
    }
}
