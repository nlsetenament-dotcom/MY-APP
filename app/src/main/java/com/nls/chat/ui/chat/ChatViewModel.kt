package com.nls.chat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nls.chat.ai.GeminiService
import com.nls.chat.data.local.AppDatabase
import com.nls.chat.data.local.PreferencesManager
import com.nls.chat.data.repository.ChatRepository
import com.nls.chat.model.CompanionConfig
import com.nls.chat.model.Message
import com.nls.chat.model.MessageSender
import kotlinx.coroutines.launch

/**
 * ChatViewModel
 * Gestiona el estado del chat siguiendo el patrón MVVM.
 * Expone LiveData para que la UI observe cambios reactivamente.
 */
class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)
    private val db    = AppDatabase.getInstance(app)
    private val repo  = ChatRepository(db.messageDao(), GeminiService())

    val config: CompanionConfig get() = prefs.companionConfig ?: CompanionConfig()

    /** Lista de mensajes como LiveData (observada por ChatActivity) */
    val messages: LiveData<List<Message>> = repo.messagesFlow.asLiveData()

    /** true mientras la IA está procesando */
    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    /** Mensajes de error para mostrar en Snackbar */
    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> = _errorEvent

    /** Controla si ya se cargó el saludo inicial */
    private var welcomeShown = false

    init {
        loadHistory()
    }

    /** Carga el historial guardado en Room al servicio Gemini */
    private fun loadHistory() {
        viewModelScope.launch {
            repo.loadHistoryToService(config)
        }
    }

    /** Muestra el saludo inicial de la IA (solo una vez por sesión) */
    fun showWelcomeIfNeeded() {
        viewModelScope.launch {
            if (welcomeShown) return@launch
            val count = repo.getMessageCount()
            if (count == 0) {
                welcomeShown = true
                _isTyping.value = true
                repo.getWelcomeMessage(config).onFailure {
                    _isTyping.value = false
                }
                _isTyping.value = false
            } else {
                welcomeShown = true
            }
        }
    }

    /** Envía mensaje del usuario y obtiene respuesta de la IA */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _isTyping.value = true
        viewModelScope.launch {
            repo.sendMessageAndGetResponse(text.trim(), config)
                .onSuccess { _isTyping.value = false }
                .onFailure {
                    _isTyping.value = false
                    _errorEvent.value = "Error de conexión. Verifica tu internet 🌐"
                }
        }
    }

    /** Borra todo el historial */
    fun clearChat() {
        viewModelScope.launch {
            repo.clearAllMessages(config)
            welcomeShown = false
            showWelcomeIfNeeded()
        }
    }

    fun clearError() { _errorEvent.value = null }
}
