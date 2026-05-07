package com.example.grama_vaxi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GramaVaxiAI {

    // 🔐 Replace with your OpenRouter API Key
    private const val API_KEY =
        "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"

    private val client = OkHttpClient()

    suspend fun getEmergencyAdvice(
        animal: String,
        issue: String,
        symptoms: String
    ): String {

        return withContext(Dispatchers.IO) {

            try {

                // Request JSON
                val json = JSONObject()

                // ✅ Better model
                json.put(
                    "model",
                    "meta-llama/llama-3-8b-instruct"
                )

                val messages = JSONArray()

                val message = JSONObject()

                message.put(
                    "role",
                    "user"
                )

                // ✅ STRICT Veterinary Prompt
                message.put(
                    "content",
                    """
                    ROLE:
                    You are ONLY a livestock veterinary assistant.

                    The farmer owns ONLY this animal:

                    Animal:
                    $animal

                    Health Issue:
                    $issue

                    Symptoms:
                    $symptoms

                    TASK:
                    Give emergency first-aid advice ONLY for THIS animal.

                    STRICT RULES:
                    - Mention ONLY the given animal
                    - NEVER mention dog, cat, horse, or other animals
                    - NEVER compare animals
                    - NEVER give general examples
                    - NEVER explain unrelated diseases
                    - Give ONLY 3 short numbered points
                    - Keep language simple for farmers
                    - Advice must match symptoms
                    - End with contacting a veterinary doctor

                    OUTPUT FORMAT:

                    1. ...
                    2. ...
                    3. ...
                    """.trimIndent()
                )

                messages.put(message)

                json.put(
                    "messages",
                    messages
                )

                // Request Body
                val body = json.toString()
                    .toRequestBody(
                        "application/json"
                            .toMediaType()
                    )

                // API Request
                val request = Request.Builder()

                    .url(
                        "https://openrouter.ai/api/v1/chat/completions"
                    )

                    .addHeader(
                        "Authorization",
                        "Bearer $API_KEY"
                    )

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

                // Execute API
                client.newCall(request)
                    .execute()
                    .use { response ->

                        if (!response.isSuccessful) {

                            throw IOException(
                                "Unexpected code $response"
                            )
                        }

                        val responseBody =
                            response.body?.string()
                                ?: throw IOException(
                                    "Empty response body"
                                )

                        val result =
                            JSONObject(responseBody)

                        val choices =
                            result.optJSONArray("choices")

                        if (
                            choices != null
                            && choices.length() > 0
                        ) {

                            choices
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim()

                        } else {

                            val error =
                                result.optJSONObject("error")
                                    ?.optString("message")
                                    ?: "Unknown API Error"

                            throw IOException(error)
                        }
                    }

            } catch (e: Exception) {

                e.printStackTrace()

                """
                1. Isolate the animal from other livestock.
                2. Provide clean drinking water and keep the animal hydrated.
                3. Contact the nearest veterinary doctor immediately.
                """.trimIndent()
            }
        }
    }
}