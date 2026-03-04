/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/views/MainView.kt $
 *
 * App container of views
 *
 */

package com.dabrabyte.eyeformusic.ui.views

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.core.content.ContextCompat
import com.dabrabyte.eyeformusic.A_MASK
import com.dabrabyte.eyeformusic.B_MASK
import com.dabrabyte.eyeformusic.C_MASK
import com.dabrabyte.eyeformusic.D_MASK
import com.dabrabyte.eyeformusic.E_MASK
import com.dabrabyte.eyeformusic.F_MASK
import com.dabrabyte.eyeformusic.G_MASK
import com.dabrabyte.eyeformusic.MENU_PADDING
import com.dabrabyte.eyeformusic.NUM_KEYS
import com.dabrabyte.eyeformusic.MIN_ACTION_DP
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.with
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.dabrabyte.eyeformusic.audio.MicData
import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.audio.SpectralSeq
import com.dabrabyte.eyeformusic.ui.dialogs.*
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme

// It is used for dynamic scaling by gestures
fun Modifier.independentScaleDetector(
    onScaling: ((Float, Float, Offset) -> Unit)? = null,
    onStartScaling: ((Offset) -> Unit)? = null,
    onEndScaling: ((Float, Float) -> Unit)? = null
): Modifier = pointerInput(Unit) {
    var isScaling = false
    var initialDistanceX = 0f
    var initialDistanceY = 0f
    var lastScaleX = 1f
    var lastScaleY = 1f

    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val pointers = event.changes

            when {
                pointers.count() == 2 -> {
                    val pointer1 = pointers[0].position
                    val pointer2 = pointers[1].position
                    // Calculate center point for scaling operation
                    val centroid = Offset(
                        (pointer1.x + pointer2.x) / 2,
                        (pointer1.y + pointer2.y) / 2
                    )

                    if (!isScaling) {
                        // Initialize scaling
                        initialDistanceX = abs(pointer1.x - pointer2.x)
                        initialDistanceY = abs(pointer1.y - pointer2.y)
                        isScaling = true
                        onStartScaling?.invoke(centroid)
                    } else {
                        // Calculate current distances
                        val currentDistanceX = abs(pointer1.x - pointer2.x)
                        val currentDistanceY = abs(pointer1.y - pointer2.y)

                        // Calculate independent scale factors
                        val scaleFactorX = if (initialDistanceX > 0)
                            currentDistanceX / initialDistanceX else 1f
                        val scaleFactorY = if (initialDistanceY > 0)
                            currentDistanceY / initialDistanceY else 1f

                        // Only trigger scaling if there's significant change
                        if (abs(scaleFactorX - 1f) > 0.03f || abs(scaleFactorY - 1f) > 0.03f) {
                            if (onScaling != null) {
                                if (abs(currentDistanceX+initialDistanceX) > abs(currentDistanceY+initialDistanceY)) {
                                    onScaling(scaleFactorX, 1f, centroid)
                                    lastScaleX = scaleFactorX
                                    lastScaleY = 1f
                                } else {
                                    onScaling(1f, scaleFactorY, centroid)
                                    lastScaleX = 1f
                                    lastScaleY = scaleFactorY
                                }
                            }
                        }
                    }

                    // Consume the pointers to prevent other gestures
                    pointers.forEach { it.consume() }
                }
                else -> {
                    if (isScaling){
                        onEndScaling?.invoke(lastScaleX, lastScaleY)
                    }
                    isScaling = false
                }
            }
        }
    }
}

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun MainView(settingsManager: SettingsManager,
             spectralSeq: SpectralSeq,
             persistScaling: PersistScaling,
             context: Context,
             onDarkModeChange: () -> Unit,
             modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    )
    {
        val eColors = ExtTheme.colors
        val fullWidthPx = constraints.maxWidth
        val fullHeightPx = constraints.maxHeight

        var settings by remember {mutableStateOf(settingsManager) }
        var showWarning by remember {mutableStateOf(false)}
        var warningText by remember {mutableStateOf("")}
        val horizontalScrollState = rememberScrollState()
        val horizontalScope = rememberCoroutineScope()
        val verticalScrollState = rememberScrollState()
        val verticalScope = rememberCoroutineScope()
        var dynamicScaleX by remember { mutableFloatStateOf(1f) } // dynamic scale factors are used only during smooth scaling by touch gestures
        var dynamicScaleY by remember { mutableFloatStateOf(1f) }
        var scale_Y by remember { mutableFloatStateOf(2.5f) } // vertical scaling factor that is effective when there is no scaling by touch gestures

        // calculation of visual and scrollable SoundView and Keyboard sizes
        val density = LocalDensity.current
        val fullWidthDp = with(density) { fullWidthPx.toDp() }
        val fullHeightDp = with(density) { fullHeightPx.toDp() }
        val minHalfToneStepPx = (with(density) { (fullHeightDp / NUM_KEYS).toPx() }).roundToInt()
            .toFloat() // this pixel indent between halftones ensures visualization of almost entire keyboard on the screen without upper and lower blank space
        val maxSpeedFactor =
            with(density) { settings.soundCacheMs.toDp() } / fullWidthDp // the slowest sound progress speed when the sound view area covers almost entire available horizontal space
        persistScaling.init(minHalfToneStepPx, scale_Y, maxSpeedFactor, settings)
        var soundViewWidthDp by remember { mutableStateOf((with(density) { settings.soundCacheMs.toDp() }) / persistScaling.speedFactor) } // adjust sound view width to the current speed
        var keyboardWidthDp = fullHeightDp / 14.5f
        if (keyboardWidthDp < fullWidthDp / 29f) keyboardWidthDp = fullWidthDp / 29f
        if (keyboardWidthDp < MIN_ACTION_DP) keyboardWidthDp = MIN_ACTION_DP
        val actionButtonWidthDp = keyboardWidthDp
        var lastProcessed by remember { mutableStateOf(0L) }
        var recordingConfirmed by remember { mutableStateOf(false) }
        var settingsOn by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .independentScaleDetector(
                    onStartScaling = { centroid ->
                            persistScaling.begin(centroid, horizontalScrollState.value, verticalScrollState.value, scale_Y)
                    },
                    onScaling = { factorX, factorY, centroid ->
                        dynamicScaleY = factorY.coerceIn(1 / scale_Y, persistScaling.scaleYTimesRange / scale_Y)
                        if (!recordingConfirmed) dynamicScaleX = persistScaling.dynamicX(centroid, factorX)
                    },
                    onEndScaling = { factorX, factorY ->
                        dynamicScaleY = 1f
                        dynamicScaleX = 1f
                        scale_Y = persistScaling.startScaleY * factorY
                        scale_Y = scale_Y.coerceIn(1f, persistScaling.scaleYTimesRange)
                        if (!recordingConfirmed) soundViewWidthDp *= persistScaling.end(factorX, factorY, scale_Y)
                    }
                ),
        )
        {
            val h = with(density) { (persistScaling.halfToneStepPx * NUM_KEYS).toDp() }
            // calculation of menu size
            val buttonWidth = (fullWidthDp - keyboardWidthDp).times(0.65f)
            val menuHeightDp = with(density) { persistScaling.menuHeight.toDp() }
            val menuY = fullHeightDp - menuHeightDp - MENU_PADDING
            val px =
                (with(density) { ((fullWidthDp - buttonWidth) / 2 - MENU_PADDING).toPx() }).toInt()
            val menuOffset by animateIntOffsetAsState(
                targetValue = if (settingsOn) IntOffset(
                    px,
                    (with(density) { menuY.toPx() }).toInt()
                ) else IntOffset(
                    px,
                    (with(density) { (fullHeightDp + menuHeightDp).toPx() }).toInt()
                ),
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "menu_offset_animation"
            )
            var showNotesDialog by remember { mutableStateOf(false) }
            var showAccuracyDialog by remember { mutableStateOf(false) }
            var showA4PitchDialog by remember { mutableStateOf(false) }
            var showFundamDialog by remember { mutableStateOf(false) }
            val noteChecks = listOf(
                NoteCheck(
                    A_MASK,
                    "A",
                    (settings.noteLabels and A_MASK) != 0
                ),
                NoteCheck(
                    B_MASK,
                    "B",
                    (settings.noteLabels and B_MASK) != 0
                ),
                NoteCheck(
                    C_MASK,
                    "C",
                    (settings.noteLabels and C_MASK) != 0
                ),
                NoteCheck(
                    D_MASK,
                    "D",
                    (settings.noteLabels and D_MASK) != 0
                ),
                NoteCheck(
                    E_MASK,
                    "E",
                    (settings.noteLabels and E_MASK) != 0
                ),
                NoteCheck(
                    F_MASK,
                    "F",
                    (settings.noteLabels and F_MASK) != 0
                ),
                NoteCheck(
                    G_MASK,
                    "G",
                    (settings.noteLabels and G_MASK) != 0
                )
            )

            LaunchedEffect(key1 = recordingConfirmed) {
                while (MicData.isRecording || !recordingConfirmed) {
                    delay(100L)
                    if (MicData.isRecording) recordingConfirmed = true
                    if (recordingConfirmed) lastProcessed = spectralSeq.lastProcessed()
                }
                recordingConfirmed = false // Stop the timer when it reaches zero
            }

            var hModifier = if (recordingConfirmed) {
                Modifier.height(h).width(fullWidthDp - keyboardWidthDp)
            } else {
                Modifier.height(h).width(fullWidthDp - keyboardWidthDp).horizontalScroll(horizontalScrollState)
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(verticalScrollState)
            ) {
                Row(
                    modifier = Modifier
                        .height(h)
                        .onSizeChanged { newSize ->
                            verticalScope.launch {
                                verticalScrollState.animateScrollTo((persistScaling.halfToneStepPx * NUM_KEYS * persistScaling.lastVRelativePos - persistScaling.lastCenter.y).toInt())
                            }
                        }
                ) {
                    Box(hModifier) {
                        SoundView(
                            lastProcessed,
                            settings.a4Pitch,
                            settings.pitchAccuracy,
                            settings.highlightFundamental,
                            settings.darkMode,
                            spectralSeq,
                            persistScaling,
                            Modifier
                                .width(soundViewWidthDp)
                                .height(h)
                                .background(eColors.whiteKeyField)
                                .graphicsLayer {
                                    scaleX = dynamicScaleX
                                    scaleY = dynamicScaleY
                                    transformOrigin =
                                        TransformOrigin(persistScaling.lastHRelativePos, persistScaling.lastVRelativePos)
                                }
                                .onSizeChanged { newSize ->
                                    horizontalScope.launch {
                                        horizontalScrollState.scrollTo((settings.soundCacheMs / persistScaling.speedFactor * persistScaling.lastHRelativePos - persistScaling.lastCenter.x).toInt())
                                    }
                                }
                        )
                    }
                    KeyboardView(
                        settings.noteLabels,
                        persistScaling,
                        Modifier
                            .width(keyboardWidthDp)
                            .height(h)
                            .graphicsLayer {
                                scaleY = dynamicScaleY
                                transformOrigin =
                                    TransformOrigin(persistScaling.lastHRelativePos, persistScaling.lastVRelativePos)
                            }
                    )
                }
            }

            if (!settingsOn) {// recording is allowed when settings are finalized
                Box(
                    modifier = Modifier
                        .absoluteOffset(
                            x = fullWidthDp - actionButtonWidthDp,
                            y = 0.dp
                        )
                        .width(actionButtonWidthDp)
                        .height(actionButtonWidthDp)
                        .zIndex(1f)
                        .clickable {
                            if (MicData.isRecording) {
                                MicData.stopRecording()
                            }
                            else {
                                if (ContextCompat.checkSelfPermission(context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) != PackageManager.PERMISSION_GRANTED) {
                                    warningText = "Permission for audio recording was not granted"
                                    showWarning = true
                                }
                                else { // scroll to the end of SoundView, so that the newly accumulated sound can be visible
                                    horizontalScope.launch {
                                        horizontalScrollState.scrollTo((settings.soundCacheMs / persistScaling.speedFactor).toInt())
                                    }
                                    MicData.startRecording()
                                }
                            }
                        }) {
                    RecordingButton(
                        Modifier.fillMaxSize(),
                        recordingConfirmed
                    )
                }
            }

            if (!recordingConfirmed) { // show settings if recording is not in progress
                Box(
                    modifier = Modifier
                        .absoluteOffset(
                            x = fullWidthDp - actionButtonWidthDp,
                            y = fullHeightDp - actionButtonWidthDp,
                        )
                        .width(actionButtonWidthDp)
                        .height(actionButtonWidthDp)
                        .zIndex(1f)
                        .clickable {
                            if (!MicData.isRecording) {
                                settingsOn = !settingsOn
                            }
                        }) {
                    SettingsButton(
                        Modifier.fillMaxSize(),
                        settingsOn
                    )
                }
            }
            Box(
                modifier = Modifier
                    .offset { menuOffset }
                    .background(eColors.greyL)
                    .zIndex(1f)
                    .padding(top=MENU_PADDING, start=MENU_PADDING, end=MENU_PADDING, bottom=MENU_PADDING/2)
                    .onSizeChanged { sz ->
                        persistScaling.menuHeight = sz.height
                    }
            )
            {
                Column()
                {
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = { showA4PitchDialog = true }
                    ) {
                        Text("Pitch of A4")
                    }
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = { showAccuracyDialog = true }
                        ) {
                        Text("Accuracy")
                    }
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = { showFundamDialog = true }
                        ) {
                        Text("Fundamentals")
                    }
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = { showNotesDialog = true }) {
                        Text("Note labels")
                    }
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = {
                            settings.saveDarkMode(!settings.darkMode)
                            onDarkModeChange()
                        }) {
                        Text(if (settings.darkMode) "Light" else "Dark")

                    }
                    Button(
                        modifier = Modifier
                            .width(buttonWidth),
                        onClick = {
                            spectralSeq.clear()
                            lastProcessed = 0
                        }) {
                        Text("Erase")
                    }
                    Text(
                        text = "Created by Aliaksandr Babitski",
                        modifier = Modifier.height(11.dp).width(buttonWidth),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = eColors.greyDD
                    )
                }
            }

            if (showNotesDialog) {
                NotesDialog(
                    actionButtonWidthDp,
                    onDismissRequest = { showNotesDialog = false },
                    onConfirmButtonClicked = { selectedOptions ->
                        var checked = 0
                        selectedOptions.forEach { c ->
                            checked = checked or c.id
                        }
                        settings.saveNoteLabels(checked)
                    },
                    optionsList = noteChecks
                )
            }

            if (showA4PitchDialog) {
                A4PitchDialog(
                    settings.a4Pitch,
                    actionButtonWidthDp,
                    onDismissRequest = { showA4PitchDialog = false },
                    onConfirmButtonClicked = { pitch ->
                        settings.saveA4Pitch(pitch)
                    }
                )
            }

            if (showAccuracyDialog) {
                AccuracyDialog(
                    settings,
                    actionButtonWidthDp,
                    onDismissRequest = { showAccuracyDialog = false },
                    onConfirmButtonClicked = { acc ->
                        settings.savePitchAccuracy(acc)
                    }
                )
            }

            if (showFundamDialog) {
                FundamDialog(
                    settings,
                    actionButtonWidthDp,
                    onDismissRequest = { showFundamDialog = false },
                    onConfirmButtonClicked = { h ->
                        settings.saveHighlightFundamental(h)
                    }
                )
            }

            if (showWarning) {
                WarningDialog(actionButtonWidthDp, warningText, onDismissRequest = {showWarning = false})
            }
        }
    }
}



