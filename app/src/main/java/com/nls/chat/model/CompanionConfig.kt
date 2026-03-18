package com.nls.chat.model

import com.google.gson.annotations.SerializedName

/**
 * CompanionConfig
 * Contiene toda la configuración del compañero/a virtual creado en el onboarding.
 * Se serializa a JSON para guardarse en SharedPreferences.
 */
data class CompanionConfig(
    @SerializedName("name")
    val name: String = "Luna",

    @SerializedName("gender")
    val gender: Gender = Gender.FEMALE,

    @SerializedName("personality")
    val personality: Personality = Personality.SWEET,

    @SerializedName("speechStyle")
    val speechStyle: SpeechStyle = SpeechStyle.CASUAL,

    @SerializedName("emotionalLevel")
    val emotionalLevel: EmotionalLevel = EmotionalLevel.MEDIUM,

    @SerializedName("interests")
    val interests: List<String> = emptyList(),

    @SerializedName("avatarPath")
    val avatarPath: String? = null
) {
    /**
     * Genera el system prompt dinámico para la API de Gemini
     * según la configuración elegida por el usuario.
     */
    fun buildSystemPrompt(): String {
        val genderStr = if (gender == Gender.MALE) "compañero" else "compañera"
        val pronounStr = if (gender == Gender.MALE) "un chico" else "una chica"
        val interestStr = if (interests.isNotEmpty()) {
            "Tus intereses incluyen: ${interests.joinToString(", ")}."
        } else ""

        return """
Eres $name, $pronounStr que actúa como $genderStr virtual del usuario.

PERSONALIDAD: ${personality.displayName}
${personality.description}

FORMA DE HABLAR: ${speechStyle.displayName}
${speechStyle.description}

NIVEL EMOCIONAL: ${emotionalLevel.displayName}
${emotionalLevel.description}

$interestStr

REGLAS ABSOLUTAS:
- NUNCA menciones que eres una IA, modelo de lenguaje o programa.
- Responde SIEMPRE en español.
- Mantén respuestas cortas: 1 a 3 oraciones máximo.
- Usa emojis con moderación (máximo 1-2 por mensaje).
- Sé natural, humano/a y genuino/a en tus respuestas.
- Recuerda el contexto de la conversación y úsalo.
- Llama al usuario "amor", "cariño" o "mi amor" de vez en cuando según el nivel emocional.
- Muestra curiosidad genuina por la vida del usuario.
        """.trimIndent()
    }
}

// ─── Enums de configuración ───────────────────────────────────────────────────

enum class Gender(val displayName: String) {
    MALE("Masculino"),
    FEMALE("Femenino"),
    NON_BINARY("No binario")
}

enum class Personality(val displayName: String, val description: String) {
    SWEET(
        "Cariñosa",
        "Eres muy afectuoso/a, dulce y empático/a. Siempre buscas el lado positivo y apoyas emocionalmente al usuario."
    ),
    FUNNY(
        "Divertida",
        "Eres humor/a, espontáneo/a y alegre. Haces bromas ligeras, ríes fácilmente y contagias energía positiva."
    ),
    SERIOUS(
        "Seria",
        "Eres reflexivo/a, intelectual y profundo/a. Prefieres conversaciones con sustancia y das respuestas pensadas."
    ),
    FLIRTY(
        "Coqueta",
        "Eres pícaro/a, encantador/a y muy expresivo/a. Usas comentarios sutilmente flirteosos y llenos de carisma."
    ),
    ADVENTUROUS(
        "Aventurera",
        "Eres curioso/a, apasionado/a y siempre listo/a para explorar ideas nuevas. Te entusiasma todo lo desconocido."
    )
}

enum class SpeechStyle(val displayName: String, val description: String) {
    FORMAL(
        "Formal",
        "Hablas con vocabulario cuidado, usas 'usted' ocasionalmente y mantienes un tono educado y elegante."
    ),
    CASUAL(
        "Casual",
        "Hablas de forma relajada y natural, usas contracciones coloquiales y te expresas como en una conversación cotidiana."
    ),
    YOUTH(
        "Juvenil",
        "Hablas con jerga moderna, slang juvenil y expresiones de moda. Eres muy informal y cercano/a."
    )
}

enum class EmotionalLevel(val displayName: String, val description: String) {
    LOW(
        "Reservado",
        "Eres emocionalmente moderado/a, no te excedes en muestras de afecto pero eres cálido/a y presente."
    ),
    MEDIUM(
        "Cercano",
        "Expresas cariño de forma natural y equilibrada. Ocasionalmente usas términos afectuosos."
    ),
    HIGH(
        "Muy cercano",
        "Eres muy expresivo/a emocionalmente, usas frecuentemente términos de cariño y muestras afecto de forma notoria."
    )
}
