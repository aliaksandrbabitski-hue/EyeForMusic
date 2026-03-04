/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/dialogs/FundamDialog.kt $
 *
 * This dialog allows to modify the coefficient for highlighting fundamentals
 *
 */

package com.dabrabyte.eyeformusic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dabrabyte.eyeformusic.DEFAULT_A4_PITCH
import com.dabrabyte.eyeformusic.NUM_KEYS
import com.dabrabyte.eyeformusic.SAMPLE_RATE
import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.audio.Frequency
import com.dabrabyte.eyeformusic.audio.SpectralSeq
import com.dabrabyte.eyeformusic.audio.Spectrum
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme
import com.dabrabyte.eyeformusic.ui.views.PersistScaling
import com.dabrabyte.eyeformusic.ui.views.SoundView
import com.dabrabyte.eyeformusic.ui.views.OkCheckMark
import com.dabrabyte.eyeformusic.ui.views.CancelCross

@Composable
fun FundamDialog(
    settingsManager: SettingsManager,
    actionButtonSize: Dp,
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: (Float) -> Unit // returns the final changed fundamental coefficient value
) {
    val exampleDurationMs = 300 // Duration (ms) of the set of sounds for illustration
    val exampleSamples = SAMPLE_RATE * exampleDurationMs / 1000 // Number of samples that corresponds to duration of the example sounds
    var highlightFundam by remember {mutableFloatStateOf(settingsManager.highlightFundamental) }
    val density = LocalDensity.current
    val fragmentHeight = 300.dp // Values for ensuring that pro
    val soundHeight = fragmentHeight * 4.32f
    val soundWidth = 90.dp
    val spectralSeq =
        SpectralSeq(settingsManager, 2000.0) //example of sounds within A4 note for illustration on the impact of chaning accuracy value
    val persistScaling = PersistScaling()
    remember {
        var s = Spectrum(132.0, 1000.0, 200, 50, 1)
        s.frequencies.add(Frequency(264.5, 0.0, 1.0, 1800.0, 2))
        s.frequencies.add(Frequency(398.0, 0.0, 1.0, 800.0, 3))
        spectralSeq.spectra.add(s)
        s = Spectrum(130.8, 1200.0, 250, 50, 1)
        s.frequencies.add(Frequency(262.8, 0.0, 1.0, 2100.0, 2))
        s.frequencies.add(Frequency(395.0, 0.0, 1.0, 900.0, 3))
        spectralSeq.spectra.add(s)
        s = Spectrum(131.2, 1100.0, 300, 50, 1)
        s.frequencies.add(Frequency(263.0, 0.0, 1.0, 1900.0, 2))
        s.frequencies.add(Frequency(395.5, 0.0, 1.0, 850.0, 3))
        spectralSeq.spectra.add(s)
        s = Spectrum(131.0, 1000.0, 350, 50, 1)
        s.frequencies.add(Frequency(262.4, 0.0, 1.0, 1700.0, 2))
        s.frequencies.add(Frequency(395.2, 0.0, 1.0, 750.0, 3))
        spectralSeq.spectra.add(s)
        s = Spectrum(130.5, 900.0, 350, 50, 1)
        s.frequencies.add(Frequency(261.5, 0.0, 1.0, 1600.0, 2))
        s.frequencies.add(Frequency(393.2, 0.0, 1.0, 700.0, 3))
        spectralSeq.spectra.add(s)
        spectralSeq.incrNumSamples(exampleSamples)
        persistScaling.init(
            with(density) { soundHeight.toPx() } / NUM_KEYS,
            1.0f,
            exampleDurationMs / with(density) { soundWidth.toPx() } * 4,
            settingsManager)
    }
    val verticalAccScrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Highlighting of fundamentals")
        },
        text = {
            Column {
                Row() {
                    Column() {
                        Text("Original")
                        Text("spectrum")
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Column() {
                        Text("Highlighted", textAlign = TextAlign.Right)
                        Text("fundamental", textAlign = TextAlign.Right)
                    }
                }
                Slider(
                    value = highlightFundam,
                    onValueChange = { newValue -> highlightFundam = newValue },
                    valueRange = 0f..0.9f,
                    steps = 89,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                Row() {
                    Box(modifier = Modifier.weight(0.35f).align(Alignment.CenterVertically)) {
                        Text("This example shows impact of the setting on highlighting the fundamental C3 and its overtones C4 and G4")
                    }
                    Spacer(Modifier.weight(0.1f))
                    Box(
                        Modifier.height(fragmentHeight)
                            .verticalScroll(verticalAccScrollState, enabled = false)
                            .width(soundWidth)
                    ) {
                        Box(
                            Modifier.height(soundHeight).width(soundWidth)
                                .absoluteOffset(0.dp, -soundHeight * 0.44f)
                        ) {
                            SoundView(
                                exampleSamples.toLong(),
                                440f,
                                settingsManager.pitchAccuracy,
                                highlightFundam,
                                settingsManager.darkMode,
                                spectralSeq,
                                persistScaling,
                                Modifier
                                    .width(soundWidth)
                                    .height(soundHeight)
                                    .background(ExtTheme.colors.whiteKeyField)

                            )
                        }
                    }
                    Spacer(Modifier.weight(0.1f))
                }
            }
        },
        confirmButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable {
                    onConfirmButtonClicked(highlightFundam)
                    onDismissRequest()
                })
            {
                OkCheckMark(Modifier.fillMaxSize())
            }
        },
        dismissButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable { onDismissRequest() })
            {
                CancelCross(Modifier.fillMaxSize())
            }
        }
    )
}