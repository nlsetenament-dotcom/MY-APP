package com.nls.chat.data.repository

import com.nls.chat.ai.GeminiService
import com.nls.chat.data.local.MessageDao
import com.nls.chat.model.CompanionConfig
import com.nls.chat.model.Message
import com.nls.chat.model.MessageSender
import com.nls.chat.model.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * ChatRepository
 * Punto único de acceso a datos del chat.
 * Combina Room (persistencia local) con GeminiService (IA remota).
 */
class ChatRepository(
    private val messageDao: MessageDao,
    private val geminiService: GeminiService
) {

    /** Flow reactivo con todos los mensajes */
    val messagesFlow: Flow<List<Message>> = messageDao.getAllMessagesFlow()

    /** Carga el historial al GeminiService al iniciar */
    suspend fun loadHistoryToService(config: CompanionConfig) {
        val messages = messageDao.getAllMessages()
        geminiService.loadHistory(config, messages)
    }

    /** Guarda un mensaje en la base de datos */
    suspend fun saveMessage(message: Message): Message {
        val id = messageDao.insertMessage(message)
        return message.copy(id = id)
    }

    /** Actualiza un mensaje en la base de datos */
    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }

    /**
     * Envía mensaje del usuario → obtiene respuesta de la IA → guarda ambos.
     * Retorna el mensaje de respuesta.
     */
    suspend fun sendMessageAndGetResponse(
        userText: String,
        config: CompanionConfig
    ): Result<Message> {
        // 1. Guardar mensaje del usuario
        val userMessage = Message(
            text = userText.trim(),
            sender = MessageSender.USER,
            status = MessageStatus.SENT
        )
        saveMessage(userMessage)

        // 2. Obtener respuesta de Gemini
        return geminiService.sendMessage(userText, config)
            .map { responseText ->
                val aiMessage = Message(
                    text = responseText,
                    sender = MessageSender.AI,
                    status = MessageStatus.SENT
                )
                // 3. Guardar respuesta en DB
                saveMessage(aiMessage)
            }
            .onFailure {
                // Guardar mensaje de error en DB para visibilidad
                val errMsg = Message(
                    text = "No pude conectarme. ¿Tienes internet? 🌐",
                    sender = MessageSender.AI,
                    status = MessageStatus.ERROR
                )
                saveMessage(errMsg)
            }
    }

    /**
     * Genera el mensaje de bienvenida al inicio del chat
     */
    suspend fun getWelcomeMessage(config: CompanionConfig): Result<Message> {
        return geminiService.getWelcomeMessage(config)
            .map { text ->
                val msg = Message(
                    text = text,
                    sender = MessageSender.AI,
                    status = MessageStatus.SENT
                )
                saveMessage(msg)
            }
    }

    /** Borra todo el historial local y reinicia el servicio */
    suspend fun clearAllMessages(config: CompanionConfig) {
        messageDao.deleteAllMessages()
        geminiService.clearHistory(config)
    }

    suspend fun getMessageCount() = messageDao.getMessageCount()
}
