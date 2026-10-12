package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.ChatMessage
import com.example.data.ChatSession
import com.example.data.ChatSettings
import com.example.data.GeminiRepository
import com.example.data.GeminiResult
import com.example.data.MessageSender
import com.example.data.PersonalityPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputQuery = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _settings = MutableStateFlow(ChatSettings())
    val settings: StateFlow<ChatSettings> = _settings.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow(UUID.randomUUID().toString())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    init {
        // Welcome message in Azerbaijani
        addInitialWelcomeMessage()
    }

    private fun addInitialWelcomeMessage() {
        if (_messages.value.isEmpty()) {
            val welcomeText = "Salam! Mən **SUPH AI** – Google Gemini ilə təchiz edilmiş ağıllı süni intellekt köməkçinizəm.\n\n" +
                "Sizə necə kömək edə bilərəm? İstənilən sualınızı verə, proqramlaşdırma kodu yaza, mətn tərtib edə və ya ideya müzakirə edə bilərik."
            _messages.value = listOf(
                ChatMessage(
                    sender = MessageSender.AI,
                    text = welcomeText
                )
            )
        }
    }

    fun onInputChange(text: String) {
        _inputQuery.value = text
    }

    fun sendMessage(overridePrompt: String? = null) {
        val query = (overridePrompt ?: _inputQuery.value).trim()
        if (query.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )

        val updatedMessages = _messages.value + userMessage
        _messages.value = updatedMessages
        _inputQuery.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.sendMessage(
                history = updatedMessages.dropLast(1),
                newPrompt = query,
                settings = _settings.value
            )

            when (result) {
                is GeminiResult.Success -> {
                    val aiMessage = ChatMessage(
                        sender = MessageSender.AI,
                        text = result.text
                    )
                    _messages.value = _messages.value + aiMessage
                    updateCurrentSessionRecord()
                }
                is GeminiResult.Error -> {
                    val errorMessage = ChatMessage(
                        sender = MessageSender.AI,
                        text = result.userFriendlyMessage,
                        isError = true,
                        errorMessage = result.technicalDetails
                    )
                    _messages.value = _messages.value + errorMessage
                }
            }
            _isLoading.value = false
        }
    }

    fun retryLastMessage() {
        val lastUserMessage = _messages.value.findLast { it.sender == MessageSender.USER }
        if (lastUserMessage != null) {
            // Remove last error message if present
            val currentList = _messages.value.toMutableList()
            if (currentList.isNotEmpty() && currentList.last().isError) {
                currentList.removeAt(currentList.lastIndex)
            }
            _messages.value = currentList
            sendMessage(overridePrompt = lastUserMessage.text)
        }
    }

    fun startNewChat() {
        saveCurrentSession()
        _currentSessionId.value = UUID.randomUUID().toString()
        _messages.value = emptyList()
        addInitialWelcomeMessage()
    }

    private fun saveCurrentSession() {
        val currentMsgs = _messages.value
        if (currentMsgs.size > 1) { // More than just welcome message
            val firstUserMsg = currentMsgs.firstOrNull { it.sender == MessageSender.USER }?.text ?: "Söhbət"
            val title = if (firstUserMsg.length > 30) firstUserMsg.take(30) + "..." else firstUserMsg
            val existingIndex = _sessions.value.indexOfFirst { it.id == _currentSessionId.value }
            val newSession = ChatSession(
                id = _currentSessionId.value,
                title = title,
                messages = currentMsgs
            )
            if (existingIndex >= 0) {
                val updated = _sessions.value.toMutableList()
                updated[existingIndex] = newSession
                _sessions.value = updated
            } else {
                _sessions.value = listOf(newSession) + _sessions.value
            }
        }
    }

    private fun updateCurrentSessionRecord() {
        saveCurrentSession()
    }

    fun loadSession(session: ChatSession) {
        saveCurrentSession()
        _currentSessionId.value = session.id
        _messages.value = session.messages
    }

    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        if (_currentSessionId.value == sessionId) {
            startNewChat()
        }
    }

    fun updateSettings(newSettings: ChatSettings) {
        _settings.value = newSettings
    }

    fun clearAllHistory() {
        _messages.value = emptyList()
        _sessions.value = emptyList()
        _currentSessionId.value = UUID.randomUUID().toString()
        addInitialWelcomeMessage()
    }

    fun isApiKeyConfigured(): Boolean {
        return _settings.value.customApiKey.isNotBlank() ||
            (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
    }
}
