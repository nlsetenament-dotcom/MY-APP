package com.nls.chat.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * GeminiApi
 * Interfaz Retrofit para el endpoint de Google Gemini.
 */
interface GeminiApi {

    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
