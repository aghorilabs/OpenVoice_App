package com.example.openvoice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val apiKeyInput = findViewById<EditText>(R.id.apiKeyInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val permissionButton = findViewById<Button>(R.id.permissionButton)
        val setServiceButton = findViewById<Button>(R.id.setServiceButton)
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        apiKeyInput.setText(prefs.getString("api_key", ""))

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
                Toast.makeText(this, "Search for 'Digital Assistant' in settings", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
