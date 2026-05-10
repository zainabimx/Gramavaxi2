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
        "TRANS"

    private val client =
        OkHttpClient()

    suspend fun translateToKannada(
        text: String
    ): String {

        return withContext(Dispatchers.IO) {

            try {

                val json = JSONObject()

                json.put(
                    "q",
                    text
                )

                json.put(
                    "target",
                    "kn"
                )

                json.put(
                    "source",
                    "en"
                )

                json.put(
                    "format",
                    "text"
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
                            "https://translation.googleapis.com/language/translate/v2?key=$API_KEY"
                        )

                        .post(body)

                        .addHeader(
                            "Content-Type",
                            "application/json"
                        )

                        .build()

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val responseBody =
                            response.body?.string()
                                ?: ""

                        val result =
                            JSONObject(responseBody)

                        result
                            .getJSONObject("data")
                            .getJSONArray("translations")
                            .getJSONObject(0)
                            .getString("translatedText")
                    }

            } catch (e: Exception) {

                e.printStackTrace()

                text
            }
        }
    }
}