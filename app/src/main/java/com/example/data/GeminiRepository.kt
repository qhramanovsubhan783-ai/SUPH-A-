package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Error(val userFriendlyMessage: String, val technicalDetails: String? = null) : GeminiResult()
}

class GeminiRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Using recommended Gemini model for text tasks
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun sendMessage(
        history: List<ChatMessage>,
        newPrompt: String,
        settings: ChatSettings
    ): GeminiResult = withContext(Dispatchers.IO) {
        val effectiveApiKey = when {
            settings.customApiKey.isNotBlank() -> settings.customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY.trim()
            else -> ""
        }

        if (effectiveApiKey.isEmpty()) {
            return@withContext GeminiResult.Error(
                userFriendlyMessage = "⚠️ Gemini API açarı tapılmadı!\n\n" +
                    "Google Gemini ilə əlaqə qurmaq üçün API açarınızı əlavə edin:\n" +
                    "1. AI Studio Secrets panelində 'GEMINI_API_KEY' açarını təyin edin, və ya\n" +
                    "2. Tənzimləmələr (⚙️) bölməsindən birbaşa API açarınızı daxil edin.",
                technicalDetails = "No valid GEMINI_API_KEY found in BuildConfig or Settings."
            )
        }

        // Build multi-turn context
        val contentsList = mutableListOf<GeminiContent>()

        // Add valid non-error history messages
        history.filter { !it.isError && it.text.isNotBlank() }.forEach { message ->
            val role = if (message.sender == MessageSender.USER) "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = message.text))
                )
            )
        }

        // Add the new user prompt
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = newPrompt))
            )
        )

        // System Instruction based on personality or custom prompt
        val effectiveSystemPrompt = when (settings.personality) {
            PersonalityPreset.CUSTOM -> settings.customSystemInstruction.trim()
            else -> {
                if (settings.customSystemInstruction.isNotBlank()) {
                    "${settings.personality.systemPrompt}\n\nƏlavə Təlimat: ${settings.customSystemInstruction.trim()}"
                } else {
                    settings.personality.systemPrompt
                }
            }
        }

        val systemInstructionContent = if (effectiveSystemPrompt.isNotBlank()) {
            GeminiContent(
                parts = listOf(GeminiPart(text = effectiveSystemPrompt))
            )
        } else null

        val geminiRequest = GeminiRequest(
            contents = contentsList,
            generationConfig = GeminiGenerationConfig(
                temperature = settings.temperature,
                maxOutputTokens = 4096
            ),
            systemInstruction = systemInstructionContent
        )

        val jsonBody = try {
            requestAdapter.toJson(geminiRequest)
        } catch (e: Exception) {
            return@withContext GeminiResult.Error(
                userFriendlyMessage = "Sorğu hazırlanarkən xəta baş verdi.",
                technicalDetails = e.message
            )
        }

        val requestUrl = "$baseUrl?key=$effectiveApiKey"
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val httpRequest = Request.Builder()
            .url(requestUrl)
            .post(requestBody)
            .build()

        try {
            val response = httpClient.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val errorCode = response.code
                val errorMessage = when (errorCode) {
                    400 -> "API Sorğu xətası (400): Göndərilən parametrlər və ya API açarı formatı düzgün deyil."
                    403 -> "İcazə xətası (403): Gemini API açarınız etibarsızdır və ya Gemini API bu layihədə aktiv deyil."
                    429 -> "Limit bitdi (429): Google Gemini API sorğu kvotası aşıldı. Zəhmət olmasa bir az sonra yenidən cəhd edin."
                    500, 503 -> "Server xətası ($errorCode): Google Gemini serverləri müvəqqəti cavab vermir. Yenidən cəhd edin."
                    else -> "API xətası ($errorCode): Əlaqə qurula bilmədi."
                }
                return@withContext GeminiResult.Error(
                    userFriendlyMessage = errorMessage,
                    technicalDetails = "HTTP $errorCode: $responseBody"
                )
            }

            if (responseBody.isNullOrBlank()) {
                return@withContext GeminiResult.Error(
                    userFriendlyMessage = "Gemini API-dən boş cavab alındı.",
                    technicalDetails = "Response body was empty."
                )
            }

            val parsedResponse = responseAdapter.fromJson(responseBody)
            val candidateText = parsedResponse?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.mapNotNull { it.text }
                ?.joinToString("")

            if (!candidateText.isNullOrBlank()) {
                GeminiResult.Success(candidateText.trim())
            } else {
                val errorStatus = parsedResponse?.error?.message ?: "Cavab mətni tapılmadı."
                GeminiResult.Error(
                    userFriendlyMessage = "Gemini tərəfindən cavab generasiya edilə bilmədi: $errorStatus",
                    technicalDetails = responseBody
                )
            }
        } catch (e: IOException) {
            GeminiResult.Error(
                userFriendlyMessage = "İnternet bağlantısı xətası: Zəhmət olmasa şəbəkə bağlantınızı yoxlayın və yenidən cəhd edin.",
                technicalDetails = e.message
            )
        } catch (e: Exception) {
            GeminiResult.Error(
                userFriendlyMessage = "Gözlənilməz xəta baş verdi: ${e.localizedMessage ?: "Naməlum xəta"}",
                technicalDetails = e.message
            )
        }
    }
}
