/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/MainActivity.kt $
 *
 * The start point of the app
 *
 */

package com.dabrabyte.eyeformusic

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.dabrabyte.eyeformusic.audio.MicData
import com.dabrabyte.eyeformusic.audio.SpectralSeq
import com.dabrabyte.eyeformusic.ui.theme.EyeForMusicTheme
import com.dabrabyte.eyeformusic.ui.views.MainView
import com.dabrabyte.eyeformusic.ui.views.PersistScaling

// The main app activity
class MainActivity : ComponentActivity() {
    // Register for the permission request callback
    private val requestRecordPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, audio recording can be started
                Toast.makeText(this, "RECORD_AUDIO permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "RECORD_AUDIO permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "Microphone access is needed for recording audio", Toast.LENGTH_LONG).show()
                // Request the permission after showing the rationale
                requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                // Directly request the permission
                requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // initialization of objects for obtaining microphone data, processing of spectra and for visualization
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioPermission()
        var settingsManager = SettingsManager()
        settingsManager.init(applicationContext)
        val persistScaling = PersistScaling()
        val spectralSeq = SpectralSeq(settingsManager)
        MicData.init(spectralSeq)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enableEdgeToEdge()
        setContent {
            var darkMode by remember { mutableStateOf(settingsManager.darkMode) }
            EyeForMusicTheme(darkMode, false) {
                MainView(settingsManager, spectralSeq, persistScaling, applicationContext, onDarkModeChange = {
                    darkMode = !darkMode
                })
            }
        }
    }
}
