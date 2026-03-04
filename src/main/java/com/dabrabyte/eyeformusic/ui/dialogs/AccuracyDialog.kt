/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/dialogs/AccuracyDialog.kt $
 *
 * This dialog allows to modify the accuracy setting
 *
 */

package com.dabrabyte.eyeformusic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dabrabyte.eyeformusic.DEFAULT_A4_PITCH
import com.dabrabyte.eyeformusic.NUM_KEYS
import com.dabrabyte.eyeformusic.SAMPLE_RATE
import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.audio.SpectralSeq
import com.dabrabyte.eyeformusic.audio.Spectrum
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme
import com.dabrabyte.eyeformusic.ui.views.PersistScaling
import com.dabrabyte.eyeformusic.ui.views.SoundView
import com.dabrabyte.eyeformusic.ui.views.OkCheckMark
import com.dabrabyte.eyeformusic.ui.views.CancelCross

@Composable
fun AccuracyDialog(
    settingsManager: SettingsManager,
    actionButtonSize: Dp,
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: (Float) -> Unit // returns the final changed accuracy value
) {
    val exampleDurationMs = 1400 // Duration (ms) of the set of sounds for illustration
    val exampleSamples = SAMPLE_RATE * exampleDurationMs / 1000 // Number of samples that corresponds to duration of the example sounds
    var accuracy by remember {mutableFloatStateOf(settingsManager.pitchAccuracy) }
    val spectralSeq = SpectralSeq(settingsManager,2000.0) //example of sounds within A4 note for illustration on the impact of chaning accuracy value
    val fragmentHeight = 120.dp // Setup of SoundView fragment
    val soundHeight = fragmentHeight * 30
    val soundWidth = 230.dp
    val persistScaling = PersistScaling()
    val density = LocalDensity.current
    remember {
        spectralSeq.spectra.add(Spectrum(428.0, 2000.0, 200, 50))
        spectralSeq.spectra.add(Spectrum(429.0, 1700.0, 250, 50))
        spectralSeq.spectra.add(Spectrum(431.0, 1400.0, 300, 50))
        spectralSeq.spectra.add(Spectrum(430.0, 1000.0, 350, 50))
        spectralSeq.spectra.add(Spectrum(432.0, 1500.0, 400, 50))
        spectralSeq.spectra.add(Spectrum(431.0, 1700.0, 450, 50))
        spectralSeq.spectra.add(Spectrum(434.0, 1800.0, 500, 50))
        spectralSeq.spectra.add(Spectrum(435.0, 1100.0, 550, 50))
        spectralSeq.spectra.add(Spectrum(433.0, 2000.0, 600, 50))
        spectralSeq.spectra.add(Spectrum(436.0, 1700.0, 650, 50))
        spectralSeq.spectra.add(Spectrum(435.0, 1500.0, 700, 50))
        spectralSeq.spectra.add(Spectrum(437.0, 1300.0, 750, 50))
        spectralSeq.spectra.add(Spectrum(438.0, 2000.0, 800, 50))
        spectralSeq.spectra.add(Spectrum(440.0, 2000.0, 850, 50))
        spectralSeq.spectra.add(Spectrum(441.0, 1500.0, 900, 50))
        spectralSeq.spectra.add(Spectrum(439.0, 2000.0, 950, 50))
        spectralSeq.spectra.add(Spectrum(440.5, 1200.0, 1000, 50))
        spectralSeq.spectra.add(Spectrum(442.0, 2000.0, 1050, 50))
        spectralSeq.spectra.add(Spectrum(444.0, 1500.0, 1100, 50))
        spectralSeq.spectra.add(Spectrum(443.0, 1000.0, 1150, 50))
        spectralSeq.spectra.add(Spectrum(446.0, 1600.0, 1200, 50))
        spectralSeq.spectra.add(Spectrum(448.0, 1800.0, 1250, 50))
        spectralSeq.spectra.add(Spectrum(452.0, 2000.0, 1300, 50))
        spectralSeq.spectra.add(Spectrum(451.0, 1700.0, 1350, 50))
        spectralSeq.spectra.add(Spectrum(449.0, 2000.0, 1400, 50))
        spectralSeq.incrNumSamples(exampleSamples)
        persistScaling.init(
            with(density) { soundHeight.toPx() } / NUM_KEYS,
            1.0f,
            exampleDurationMs / with(density) { soundWidth.toPx() } * 4,
            settingsManager)
    }
    val verticalAccScrollState = rememberScrollState(0)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Pitch Accuracy (cents)")
        },
        text = {
            Column { // displaying current pitch value with one decimal
                Text(
                    "%.0f".format(accuracy*100),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                Slider(
                    value = accuracy,
                    onValueChange = { newValue -> accuracy = newValue },
                    valueRange = 0f..0.5f,
                    steps = 49,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                Text("In the example of sounds below deviations from standard pitch higher than accuracy are colored in shades of red, lower - blue. More saturated colors correspond to bigger deviations. Brighter colors correspond to louder sounds.")

                Box(Modifier
                    .height(fragmentHeight)
                    .padding(top=10.dp)
                    .verticalScroll(verticalAccScrollState, enabled = false)
                    .align(Alignment.CenterHorizontally)) {
                    SoundView(
                        exampleSamples.toLong(),
                        440f,
                        accuracy,
                        settingsManager.highlightFundamental,
                        settingsManager.darkMode,
                        spectralSeq,
                        persistScaling,
                        Modifier
                            .width(soundWidth)
                            .height(soundHeight)
                            .absoluteOffset(0.dp, -soundHeight * 0.4524f)
                            .background(ExtTheme.colors.whiteKeyField)
                    )
                }
            }
        },
        confirmButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable {
                    onConfirmButtonClicked(accuracy)
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