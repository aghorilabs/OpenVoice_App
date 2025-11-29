package com.example.openvoice

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class VoiceInputService : RecognitionService() {

    private lateinit var audioRecorder: AudioRecorder
    private val apiClient = OpenRouterClient()
    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        audioRecorder = AudioRecorder(this)
    }

    override fun onStartListening(intent: Intent?, callback: Callback?) {
        Log.d("VoiceService", "onStartListening triggered")
        callback?.readyForSpeech(Bundle())
        try {
            audioRecorder.startRecording()
            callback?.beginningOfSpeech()
        } catch (e: Exception) {
            callback?.error(SpeechRecognizer.ERROR_AUDIO)
        }
    }

    override fun onStopListening(callback: Callback?) {
        handleEndRecording(callback)
    }

    override fun onCancel(callback: Callback?) {
        audioRecorder.stopRecording()
        serviceJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder.stopRecording()
        serviceJob?.cancel()
    }

    private fun handleEndRecording(callback: Callback?) {
        val audioFile: File? = audioRecorder.stopRecording()
        callback?.endOfSpeech()

        if (audioFile == null || !audioFile.exists()) {
            callback?.error(SpeechRecognizer.ERROR_NO_MATCH)
            return
        }

        serviceJob = scope.launch {
            try {
                val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val apiKey = prefs.getString("api_key", "") ?: ""

                if (apiKey.isEmpty()) {
                    sendResults("Error: Please set OpenRouter API Key in OpenVoice settings app.", callback)
                    return@launch
                }

                val transcribedText = apiClient.transcribeAudio(apiKey, audioFile)
                sendResults(transcribedText, callback)

            } catch (e: Exception) {
                e.printStackTrace()
                // Send the error as text so the user sees what happened
                sendResults("Error: ${e.message}", callback)
            } finally {
                try { audioFile.delete() } catch (_: Exception) {}
            }
        }
    }

    private fun sendResults(text: String, callback: Callback?) {
        val bundle = Bundle().apply {
            putStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION,
                arrayListOf(text)
            )
        }
        callback?.results(bundle)
    }
}
