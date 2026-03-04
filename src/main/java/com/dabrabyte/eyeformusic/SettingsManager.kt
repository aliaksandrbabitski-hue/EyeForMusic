/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/SettingsManager.kt $
 *
 * Settings of the app which are persisted when app is not running
 *
 */

package com.dabrabyte.eyeformusic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first


// App constants
const val NUM_OCTAVES = 9 // maximal keyboard range of 9 octaves
const val NUM_KEYS = NUM_OCTAVES * 12 // number of keys in 9 octaves
const val HALFTONE_INCR = 1.0594631f // 2^(1/12)  frequency factor between adjacent halftones
const val SAMPLE_RATE = 44100 // 44.1 kHz typical recording rate
const val CHUNK_SIZE = 4096 // recording buffer size
const val DEFAULT_A4_PITCH = 440.0f // Standard pitch
const val DEFAULT_PITCH_ACCURACY = 0.2f // 20 cents (20% of halftone distance to the higher and lower sides)
const val DEFAULT_FUNDAMENTAL_HIGHLIGHT = 0.8f // 80% of amplitudes of harmonics are used for highlighting the main note
const val MAX_VISIBLE_PITCH = DEFAULT_A4_PITCH * 16 * HALFTONE_INCR * HALFTONE_INCR * HALFTONE_INCR // maximal pitch visible on the screen
val MIN_ACTION_DP = 48.dp // guidelines for minimal action button size
val MENU_PADDING = 12.dp // padding around menu buttons

const val A_MASK = 32 // Masks for packing info about note labels in one integer
const val B_MASK  = 64
const val C_MASK  = 1
const val D_MASK  = 2
const val E_MASK  = 4
const val F_MASK  = 8
const val G_MASK  = 16

class SettingsManager {
    val A4_PITCH_KEY = floatPreferencesKey("a4_pitch")
    val PITCH_ACCURACY_KEY = floatPreferencesKey("pitch_accuracy")
    val HIGHLIGHT_FUNDAMENTAL_KEY = floatPreferencesKey("highlight_main_note")
    val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    val NOTE_LABELS_KEY = intPreferencesKey("note_labels")

    var a4Pitch by mutableStateOf(DEFAULT_A4_PITCH)
        private set
    val soundCacheMs: Int = 30000 // Maximal time (ms) for storing processed spectral data, it is planned to have it configurable in the future
    var highlightFundamental by mutableStateOf(DEFAULT_FUNDAMENTAL_HIGHLIGHT)
        private set
    var pitchAccuracy by mutableStateOf(DEFAULT_PITCH_ACCURACY)
        private set
    var darkMode by mutableStateOf(false)
        private set
    var noteLabels by mutableStateOf(A_MASK)
        private set
    private var context: Context? = null

    fun init(cont: Context)
    {
        context = cont
        a4Pitch = runBlocking { context?.dataStore?.data?.first()[A4_PITCH_KEY] } ?: DEFAULT_A4_PITCH
        pitchAccuracy = runBlocking { context?.dataStore?.data?.first()[PITCH_ACCURACY_KEY] } ?: DEFAULT_PITCH_ACCURACY
        highlightFundamental = runBlocking { context?.dataStore?.data?.first()[HIGHLIGHT_FUNDAMENTAL_KEY] } ?: DEFAULT_FUNDAMENTAL_HIGHLIGHT
        darkMode = runBlocking { context?.dataStore?.data?.first()[DARK_MODE_KEY] } ?: false
        noteLabels = runBlocking { context?.dataStore?.data?.first()[NOTE_LABELS_KEY] } ?: A_MASK
    }

    fun saveDarkMode(dm: Boolean) = runBlocking {
        darkMode = dm
        launch {
            context?.dataStore?.edit { preferences -> preferences[DARK_MODE_KEY] = dm }
        }
    }

    fun saveNoteLabels(nl: Int) = runBlocking {
        noteLabels = nl
        launch {
            context?.dataStore?.edit { preferences -> preferences[NOTE_LABELS_KEY] = nl }
        }
    }

    fun saveA4Pitch(ap: Float) = runBlocking {
        a4Pitch = ap
        launch {
            context?.dataStore?.edit { preferences -> preferences[A4_PITCH_KEY] = ap }
        }
    }

    fun savePitchAccuracy(acc: Float) = runBlocking {
        pitchAccuracy = acc
        launch {
            context?.dataStore?.edit { preferences -> preferences[PITCH_ACCURACY_KEY] = acc }
        }
    }

    fun saveHighlightFundamental(highlight: Float) = runBlocking {
        highlightFundamental = highlight
        launch {
            context?.dataStore?.edit { preferences -> preferences[HIGHLIGHT_FUNDAMENTAL_KEY] = highlight }
        }
    }
}

val Context.dataStore by preferencesDataStore("app_settings")
