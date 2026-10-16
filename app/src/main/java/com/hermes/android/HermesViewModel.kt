package com.hermes.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hermes.android.data.Conversation
import com.hermes.android.data.ConversationStore
import com.hermes.android.data.HermesApi
import com.hermes.android.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HermesViewModel(application: Application) : AndroidViewModel(application) {
    val store = ConversationStore(application)
    val api = HermesApi()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _baseUrl = MutableStateFlow("https://openrouter.ai/api/v1")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    init {
        refreshConversations()
    }

    fun refreshConversations() {
        _conversations.value = store.getAll()
    }

    fun newConversation() {
        val conv = store.create()
        _currentConversation.value = conv
        refreshConversations()
    }

    fun selectConversation(id: String) {
        _currentConversation.value = store.getById(id)
    }

    fun deleteConversation(id: String) {
        store.delete(id)
        if (_currentConversation.value?.id == id) {
            _currentConversation.value = null
        }
        refreshConversations()
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
        api.configure(_baseUrl.value, key)
    }

    fun setBaseUrl(url: String) {
        _baseUrl.value = url
        api.configure(url, _apiKey.value)
    }

    fun sendMessage(content: String) {
        val conv = _currentConversation.value ?: return
        val userMsg = Message("user", content)
        store.addMessage(conv.id, userMsg)
        _currentConversation.value = store.getById(conv.id)
        refreshConversations()

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val reply = api.chat(conv)
                store.addMessage(conv.id, Message("assistant", reply))
                _currentConversation.value = store.getById(conv.id)
                refreshConversations()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "未知错误"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
