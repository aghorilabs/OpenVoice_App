package com.example.openvoice

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class OpenRouterClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun transcribeAudio(apiKey: String, audioFile: File): String {
        val mediaType = "audio/m4a".toMediaType()
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, audioFile.asRequestBody(mediaType))
            .addFormDataPart("model", "openai/whisper") 
            .build()

        // Using standard OpenAI endpoint as OpenRouter often proxies or uses same structure
        // Verify this endpoint if using specific OpenRouter models
        val url = "https://openrouter.ai/api/v1/audio/transcriptions"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://github.com/YourUser/OpenVoice") 
            .addHeader("X-Title", "OpenVoice Android")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        
        if (!response.isSuccessful) {
            throw Exception("API Error ${response.code}: $responseBody")
        }

        val json = JSONObject(responseBody)
        return if (json.has("text")) {
            json.getString("text")
        } else {
            "Error: No text in response"
        }
    }
}
