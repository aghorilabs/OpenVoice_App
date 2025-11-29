package com.example.openvoice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val apiKeyInput = findViewById<EditText>(R.id.apiKeyInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val permissionButton = findViewById<Button>(R.id.permissionButton)
        val setServiceButton = findViewById<Button>(R.id.setServiceButton)
        val instructionsText = findViewById<TextView>(R.id.instructionsText)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        apiKeyInput.setText(prefs.getString("api_key", ""))

        // --- DETECT MANUFACTURER ---
        val manufacturer = android.os.Build.MANUFACTURER.lowercase(Locale.ROOT)
        val brandName = manufacturer.replaceFirstChar { it.uppercase() }
        
        val instructions = when {
            manufacturer.contains("samsung") -> 
                "Samsung Steps:\n1. Settings > General management\n2. Keyboard list and default\n3. Default digital assistant app\n4. Voice input app > OpenVoice"
            
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                "Xiaomi/Redmi Steps:\n1. Settings > Additional settings\n2. Languages & input\n3. Manage keyboards (or Current Keyboard)\n4. Enable OpenVoice"

            manufacturer.contains("google") -> 
                "Pixel Steps:\n1. Settings > Apps > Default apps\n2. Digital assistant app\n3. Voice input > OpenVoice"

            manufacturer.contains("oneplus") ->
                "OnePlus Steps:\n1. Settings > System settings\n2. Keyboard & input method\n3. Manage Keyboards > Enable OpenVoice"

            else -> 
                "Generic Android Steps:\n1. Open Settings\n2. Search for 'Digital assistant' or 'Voice input'\n3. Select OpenVoice"
        }
        
        instructionsText.text = "Device Detected: $brandName\n\n$instructions"

        saveButton.setOnClickListener {
            prefs.edit().putString("api_key", apiKeyInput.text.toString().trim()).apply()
            Toast.makeText(this, "API Key Saved", Toast.LENGTH_SHORT).show()
        }

        permissionButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
            }
        }

        setServiceButton.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
                Toast.makeText(this, "Follow instructions below!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
