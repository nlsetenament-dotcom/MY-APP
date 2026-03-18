package com.nls.chat.ai

import com.google.gson.annotations.SerializedName

// ════════════════════════════════════════════════════════════════════════════
// REQUEST MODELS
// ════════════════════════════════════════════════════════════════════════════

data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<GeminiContent>,

    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig(),

    @SerializedName("safetySettings")
    val safetySettings: List<SafetySetting> = defaultSafetySettings()
)

data class GeminiContent(
    @SerializedName("role")
    val role: String,   // "user" | "model"

    @SerializedName("parts")
    val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text")
    val text: String
)

data class GenerationConfig(
    @SerializedName("temperature")
    val temperature: Float = 0.85f,

    @SerializedName("topK")
    val topK: Int = 40,

    @SerializedName("topP")
    val topP: Float = 0.95f,

    @SerializedName("maxOutputTokens")
    val maxOutputTokens: Int = 300,

    @SerializedName("candidateCount")
    val candidateCount: Int = 1
)

data class SafetySetting(
    @SerializedName("category")
    val category: String,

    @SerializedName("threshold")
    val threshold: String
)

fun defaultSafetySettings() = listOf(
    SafetySetting("HARM_CATEGORY_HARASSMENT",        "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_HATE_SPEECH",       "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
    SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE")
)

// ════════════════════════════════════════════════════════════════════════════
// RESPONSE MODELS
// ════════════════════════════════════════════════════════════════════════════

data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<GeminiCandidate>?,

    @SerializedName("promptFeedback")
    val promptFeedback: PromptFeedback?
)

data class GeminiCandidate(
    @SerializedName("content")
    val content: GeminiContent?,

    @SerializedName("finishReason")
    val finishReason: String?,

    @SerializedName("index")
    val index: Int?
)

data class PromptFeedback(
    @SerializedName("blockReason")
    val blockReason: String?
)

// ─── Helper extension ────────────────────────────────────────────────────────

/** Extrae el texto de la primera respuesta del candidato */
fun GeminiResponse.extractText(): String? =
    candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
