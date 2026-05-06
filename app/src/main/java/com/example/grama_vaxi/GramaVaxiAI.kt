package com.example.grama_vaxi

import androidx.compose.runtime.*
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

object GramaVaxiAI {
    private const val API_KEY = "AIzaSyDSRfY1GPzL5r3ezvNIH2BNAFuJkNI-V88"

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun translate(text: String, toKannada: Boolean): String {
        if (!toKannada) return text
        return try {
            val response = model.generateContent(
                content {
                    text("Translate to simple Kannada for a farmer: $text. Return only the translated text.")
                }
            )
            response.text ?: text
        } catch (e: Exception) {
            text
        }
    }
}

/**
 * ADD THIS HELPER: Use this inside any page to translate strings
 */
@Composable
fun rememberI18n(text: String, isKannada: Boolean): String {
    var translatedText by remember { mutableStateOf(text) }

    LaunchedEffect(isKannada, text) {
        translatedText = if (isKannada) {
            GramaVaxiAI.translate(text, true)
        } else {
            text
        }
    }
    return translatedText
}