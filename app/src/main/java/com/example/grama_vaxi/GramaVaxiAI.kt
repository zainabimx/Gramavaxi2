package com.example.grama_vaxi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GramaVaxiAI {

    // 🔐 OPENROUTER API KEY
    private const val API_KEY =
        "API_KEY"

    // ✅ STABLE CLIENT
    private val client =

        OkHttpClient.Builder()

            .connectTimeout(
                30,
                TimeUnit.SECONDS
            )

            .readTimeout(
                30,
                TimeUnit.SECONDS
            )

            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )

            .build()

    suspend fun getEmergencyAdvice(

        animal: String,

        issue: String,

        symptoms: String,

        isKannada: Boolean = false

    ): String {

        return withContext(Dispatchers.IO) {

            try {

                val json =
                    JSONObject()

                // ✅ STABLE MODEL
                json.put(

                    "model",

                    "openai/gpt-3.5-turbo"
                )

                // ✅ RESPONSE CONTROL
                json.put(
                    "max_tokens",
                    120
                )

                json.put(
                    "temperature",
                    0.3
                )

                json.put(
                    "top_p",
                    0.8
                )

                val messages =
                    JSONArray()

                val message =
                    JSONObject()

                message.put(
                    "role",
                    "user"
                )

                // ✅ ALWAYS REASON IN ENGLISH
                message.put(

                    "content",

                    """
    Respond ONLY in English.

    ROLE:
    You are a senior livestock veterinary emergency doctor helping rural farmers before a real veterinarian arrives.

    IMPORTANT:
    Your advice must be medically meaningful and symptom-specific.

    Animal:
    $animal

    Main Problem:
    $issue

    Symptoms:
    $symptoms

    TASK:
    Carefully analyze the symptoms and infer the MOST LIKELY livestock condition.

    Then provide:
    1. Immediate first-aid treatment
    2. Infection control or feeding advice if relevant
    3. Clear danger warning and veterinary escalation advice

    IMPORTANT RULES:
    - NEVER give generic advice
    - NEVER repeat symptoms back
    - NEVER mention unrelated diseases
    - Mention likely disease if symptoms strongly suggest one
    - Advice must be practical for rural farmers
    - Include useful care advice like:
      soft feed,
      cleaning wounds,
      hydration,
      temperature monitoring,
      isolation,
      hoof cleaning,
      oral lesions,
      breathing difficulty,
      dehydration signs,
      etc. when appropriate

    RESPONSE RULES:
    - Give EXACTLY 3 numbered points
    - Each point must be different
    - Keep advice medically useful
    - Keep response under 120 words

    GOOD EXAMPLE:

    1. Clean the cow's mouth gently with clean water and provide soft feed to reduce pain while eating.
    2. Isolate the cow from other livestock because the symptoms may indicate Foot and Mouth Disease infection.
    3. If fever, drooling, or foot wounds worsen, contact a veterinary doctor immediately for treatment.

    START RESPONSE DIRECTLY.
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

                val response =

                    client.newCall(request)
                        .execute()

                // ✅ HANDLE API FAILURES
                if (!response.isSuccessful) {

                    val errorBody =
                        response.body?.string()

                    throw IOException(

                        "API ERROR ${response.code}\n$errorBody"
                    )
                }

                val responseBody =

                    response.body?.string()

                        ?: throw IOException(
                            "Empty response body"
                        )

                Log.d(
                    "GRAMAVAXI_AI",
                    responseBody
                )

                val result =
                    JSONObject(responseBody)

                // ✅ SAFE PARSING
                val choices =
                    result.optJSONArray("choices")

                if (
                    choices != null &&
                    choices.length() > 0
                ) {

                    val firstChoice =
                        choices.getJSONObject(0)

                    val messageObj =
                        firstChoice.optJSONObject("message")

                    val content =
                        messageObj?.optString(
                            "content",
                            ""
                        )

                    if (!content.isNullOrBlank()) {

                        // ✅ CLEAN REPEATED WORDS
                        content
                            .replace(
                                Regex("(\\b\\S+\\b)(\\s+\\1)+"),
                                "$1"
                            )
                            .trim()

                    } else {

                        throw IOException(
                            "Empty AI response"
                        )
                    }

                } else {

                    val errorMessage =

                        result.optJSONObject("error")
                            ?.optString("message")

                            ?: result.toString()

                    throw IOException(
                        "OpenRouter Error: $errorMessage"
                    )
                }

            } catch (e: Exception) {

                Log.e(

                    "GRAMAVAXI_AI",

                    "FULL ERROR: ${e.localizedMessage}"
                )

                e.printStackTrace()

                // ✅ FALLBACK ENGLISH
                """
                1. Isolate the animal from other livestock.
                2. Provide clean water and soft feed.
                3. Contact the nearest veterinary doctor immediately.
                """.trimIndent()
            }
        }
    }
}