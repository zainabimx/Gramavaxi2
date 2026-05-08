package com.example.grama_vaxi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object KannadaTranslator {

    private const val API_KEY =
"API_KEY"
    private val client =
        OkHttpClient()

    suspend fun translateToKannada(
        text: String
    ): String {

        return withContext(Dispatchers.IO) {

            try {

                val json = JSONObject()

                // ✅ Better translation model
                json.put(
                    "model",
                    "openai/gpt-3.5-turbo"
                )

                val messages =
                    JSONArray()

                val message =
                    JSONObject()

                message.put(
                    "role",
                    "user"
                )

                // ✅ Strong translation prompt
                message.put(
                    "content",
                    """
                    You are a professional Kannada translator.

                    Translate the following English text into pure Kannada.

                    STRICT RULES:
                    - Return ONLY Kannada translation
                    - No English words
                    - No explanation
                    - No quotes
                    - No extra formatting
                    - Keep the meaning accurate

                    English Text:
                    $text
                    """.trimIndent()
                )

                messages.put(message)

                json.put(
                    "messages",
                    messages
                )

                val body =
                    json.toString()
                        .toRequestBody(
                            "application/json"
                                .toMediaType()
                        )

                val request =
                    Request.Builder()

                        .url(
                            "https://openrouter.ai/api/v1/chat/completions"
                        )

                        .addHeader(
                            "Authorization",
                            "Bearer $API_KEY"
                        )

                        // ✅ Important OpenRouter headers
                        .addHeader(
                            "HTTP-Referer",
                            "https://gramavaxi.app"
                        )

                        .addHeader(
                            "X-Title",
                            "GramaVaxi"
                        )

                        .post(body)

                        .build()

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val responseBody =
                            response.body?.string()
                                ?: ""

                        val result =
                            JSONObject(responseBody)

                        // ✅ Proper response parsing
                        result
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim()
                    }

            } catch (e: Exception) {

                e.printStackTrace()

                // fallback
                text
            }
        }
    }
}