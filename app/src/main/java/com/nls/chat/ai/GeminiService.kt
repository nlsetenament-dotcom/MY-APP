package com.nls.chat.ai

import com.nls.chat.BuildConfig
import com.nls.chat.model.CompanionConfig
import com.nls.chat.model.Message
import com.nls.chat.model.MessageSender

/**
 * GeminiService
 * Clase principal de comunicación con la API de Google Gemini.
 *
 * Responsabilidades:
 * - Construir el prompt dinámico según CompanionConfig
 * - Mantener el historial de conversación en memoria
 * - Enviar requests HTTP y parsear respuestas
 * - Gestionar errores de red y de API
 */
class GeminiService {

    private val api = RetrofitClient.geminiApi
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // Historial de la conversación actual (role: user/model)
    private val conversationHistory = mutableListOf<GeminiContent>()

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Carga el historial guardado en Room al iniciar el chat.
     * Esto permite que la IA tenga contexto de conversaciones anteriores.
     */
    fun loadHistory(config: CompanionConfig, messages: List<Message>) {
        conversationHistory.clear()
        // Inyectar el system prompt como contexto inicial
        injectSystemPrompt(config)
        // Añadir mensajes históricos
        messages.forEach { msg ->
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            conversationHistory.add(
                GeminiContent(role = role, parts = listOf(GeminiPart(msg.text)))
            )
        }
    }

    /**
     * Envía el mensaje del usuario y retorna la respuesta de la IA.
     */
    suspend fun sendMessage(userText: String, config: CompanionConfig): Result<String> {
        // Inicializar contexto si está vacío
        if (conversationHistory.isEmpty()) injectSystemPrompt(config)

        return try {
            // Agregar mensaje del usuario al historial
            val userContent = GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(userText))
            )
            conversationHistory.add(userContent)

            // Construir y enviar el request
            val request = buildRequest()
            val response = api.generateContent(apiKey = apiKey, request = request)

            if (response.isSuccessful) {
                val body = response.body()
                val text = body?.extractText()

                if (!text.isNullOrBlank()) {
                    // Guardar respuesta en historial
                    conversationHistory.add(
                        GeminiContent(role = "model", parts = listOf(GeminiPart(text)))
                    )
                    // Mantener historial en límite razonable (evitar tokens excesivos)
                    trimHistoryIfNeeded()
                    Result.success(text)
                } else {
                    val blockReason = body?.promptFeedback?.blockReason
                    conversationHistory.removeLastOrNull()
                    Result.failure(Exception("Respuesta bloqueada: $blockReason"))
                }
            } else {
                conversationHistory.removeLastOrNull()
                val code = response.code()
                val err = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("HTTP $code: $err"))
            }
        } catch (e: Exception) {
            conversationHistory.removeLastOrNull()
            Result.failure(e)
        }
    }

    /**
     * Genera el primer mensaje de bienvenida personalizado.
     */
    suspend fun getWelcomeMessage(config: CompanionConfig): Result<String> {
        return try {
            val systemPrompt = config.buildSystemPrompt()
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent("user", listOf(GeminiPart(systemPrompt))),
                    GeminiContent("model", listOf(GeminiPart("Entendido, seré ${config.name}."))),
                    GeminiContent(
                        "user",
                        listOf(
                            GeminiPart(
                                "Saluda al usuario por primera vez de forma cariñosa y breve. " +
                                        "Máximo 2 oraciones. Pregunta cómo está su día."
                            )
                        )
                    )
                )
            )

            val response = api.generateContent(apiKey = apiKey, request = request)
            if (response.isSuccessful) {
                val text = response.body()?.extractText()
                    ?: "¡Hola! Me alegra tanto que estés aquí 💕 ¿Cómo estuvo tu día?"

                // Agregar al historial para continuidad
                conversationHistory.add(
                    GeminiContent("model", listOf(GeminiPart(text)))
                )
                Result.success(text)
            } else {
                Result.success("¡Hola! Me alegra tanto que estés aquí 💕 ¿Cómo estuvo tu día?")
            }
        } catch (e: Exception) {
            Result.success("¡Hola amor! ¿Cómo estás hoy? 💕")
        }
    }

    /**
     * Reinicia el historial y re-inyecta el system prompt.
     */
    fun clearHistory(config: CompanionConfig) {
        conversationHistory.clear()
        injectSystemPrompt(config)
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Inyecta el system prompt como primer par user/model del historial.
     * Técnica estándar para Gemini (no tiene campo system nativo en v1beta).
     */
    private fun injectSystemPrompt(config: CompanionConfig) {
        val systemPrompt = config.buildSystemPrompt()
        conversationHistory.add(
            GeminiContent("user", listOf(GeminiPart(systemPrompt)))
        )
        conversationHistory.add(
            GeminiContent(
                "model",
                listOf(GeminiPart("Perfecto, actuaré exactamente como ${config.name}. Estoy listo/a."))
            )
        )
    }

    /** Construye el GeminiRequest completo con el historial actual */
    private fun buildRequest(): GeminiRequest {
        return GeminiRequest(
            contents = conversationHistory.toList(),
            generationConfig = GenerationConfig(
                temperature = 0.9f,
                maxOutputTokens = 300
            )
        )
    }

    /**
     * Limita el historial a los últimos 30 turnos (conservando siempre el
     * system prompt inicial) para no exceder el límite de tokens.
     */
    private fun trimHistoryIfNeeded() {
        if (conversationHistory.size > 34) {
            // Conservar los 2 primeros (system prompt) + últimos 30
            val systemPromptPair = conversationHistory.take(2)
            val recent = conversationHistory.takeLast(30)
            conversationHistory.clear()
            conversationHistory.addAll(systemPromptPair)
            conversationHistory.addAll(recent)
        }
    }
}
