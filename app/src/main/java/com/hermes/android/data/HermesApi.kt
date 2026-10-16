package com.hermes.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HermesApi(
    private var baseUrl: String = "https://openrouter.ai/api/v1",
    private var apiKey: String = ""
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun configure(url: String, key: String) {
        baseUrl = url.trimEnd('/')
        apiKey = key
    }

    suspend fun chat(conversation: Conversation): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            conversation.messages.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
        }

        val body = JSONObject().apply {
            put("model", "google/gemini-2.0-flash-exp:free")
            put("messages", messages)
            put("stream", false)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: $responseBody")
        }

        val json = JSONObject(responseBody)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
