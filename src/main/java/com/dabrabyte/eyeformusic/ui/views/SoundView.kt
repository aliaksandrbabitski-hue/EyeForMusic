/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/views/SoundView.kt $
 *
 * This view shows the accumulated spectra of sounds in correspondence to the frequencies of notes
 *
 */

package com.dabrabyte.eyeformusic.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.dabrabyte.eyeformusic.DEFAULT_A4_PITCH
import kotlin.math.log2
import com.dabrabyte.eyeformusic.HALFTONE_INCR
import com.dabrabyte.eyeformusic.NUM_OCTAVES
import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme
import com.dabrabyte.eyeformusic.audio.SpectralSeq
import kotlin.math.abs

@Composable
fun SoundView(lastProcessed: Long,
              a4Pitch: Float,
              pitchAccuracy: Float,
              highlightFundamental: Float,
              darkMode: Boolean,
              spectralSeq: SpectralSeq,
              persistScaling: PersistScaling,
              modifier: Modifier = Modifier) {
    val halfToneStepPx = persistScaling.halfToneStepPx
    val eColors = ExtTheme.colors

    Canvas(modifier) {
        val highSoundLg = 12.989692 + log2(a4Pitch) - log2(DEFAULT_A4_PITCH) // log2 of the highest visualized sound B8(+50cents)
        val whiteKeyStep = halfToneStepPx / 7 * 12
        // draw black keys
        for (i in 0 .. ((this.size.height/whiteKeyStep-1)*5/7).toInt()) {
            val blackKeyOffset = i * 7.0f / 5 * whiteKeyStep + (i % 5 * 2 + (if(i % 5 > 2)  2 else 1)) * halfToneStepPx - i % 5 * 7 * whiteKeyStep / 5;
            drawRect(eColors.whiteKeySeparator, Offset(0.0f, blackKeyOffset), Size(this.size.width, halfToneStepPx))
        }
        // draw separation lines between adjacent white keys
        for (i in 0 until NUM_OCTAVES) {
            var y = i * 12 * halfToneStepPx
            drawLine(eColors.whiteKeySeparator,Offset(0.0f, y), Offset(this.size.width, y))
            y += 7 * halfToneStepPx - 1
            drawLine(eColors.whiteKeySeparator,Offset(0.0f, y), Offset(this.size.width, y))
        }

        synchronized(spectralSeq.spectraLock) {
            for (s in spectralSeq.spectra) {
                if (0L == lastProcessed) continue

                val x = this.size.width - (spectralSeq.moment(0)-s.fromMoment + 150f) / persistScaling.speedFactor
                for (j in 0 until s.frequencies.size) {
                    val y = this.size.height * (highSoundLg - log2(s.frequencies[j].pitch)) / NUM_OCTAVES
                    var amplPart = s.frequencies[j].amplitude
                    // draw fundamental with amplitude increased by amplitude of overtones in accordance with the chosen setting
                    if (highlightFundamental > 0) {
                        if (s.frequencies[j].overtoneNum == 1) {
                            for (k in 0 until s.frequencies.size) {
                                if (s.frequencies[k].overtoneNum > 1 && abs(s.frequencies[k].pitch / s.frequencies[k].overtoneNum - s.frequencies[j].pitch) < s.frequencies[j].pitch * 0.01)
                                    amplPart += s.frequencies[k].amplitude * highlightFundamental
                            }
                        } else amplPart *= 1 - highlightFundamental
                    }
                    if (spectralSeq.maxAmplitude > 0) amplPart /= spectralSeq.maxAmplitude
                    if (amplPart > 1) amplPart = 1.0
                    // determine the adjacent higher and lower notes for the current frequency
                    var higherNotePitch = a4Pitch
                    var lowerNotePitch = higherNotePitch
                    if (s.frequencies[j].pitch > higherNotePitch) {
                        while (s.frequencies[j].pitch > higherNotePitch) {
                            lowerNotePitch = higherNotePitch
                            higherNotePitch *= HALFTONE_INCR
                        }
                    } else {
                        while (s.frequencies[j].pitch < lowerNotePitch) {
                            higherNotePitch = lowerNotePitch
                            lowerNotePitch /= HALFTONE_INCR
                        }
                    }
                    // determine deviation from the standard pitch (center of the note on screen)
                    var deviation = 0.0
                    if (higherNotePitch > lowerNotePitch) deviation = 12 * log2(s.frequencies[j].pitch / lowerNotePitch)
                    if (deviation > 0.5) deviation -= 1.0
                    var otherColorComp = if (darkMode) 55 + 200 * amplPart else 220 * (1 - amplPart)
                    var redComponent = otherColorComp
                    var blueComponent = otherColorComp
                    // adjust red and blue shades for sounds out of the accuracy setting
                    if (darkMode) {
                        if (deviation > pitchAccuracy) {
                            otherColorComp *= 0.7 * (1 - (deviation - pitchAccuracy) / (0.51 - pitchAccuracy))
                            blueComponent = otherColorComp
                        }
                        if (deviation < -pitchAccuracy) {
                            otherColorComp *= 0.7 * (1 + (deviation + pitchAccuracy) / (0.51 - pitchAccuracy))
                            redComponent = otherColorComp
                        }
                    } else {
                        if (deviation > pitchAccuracy) redComponent =
                            otherColorComp + (255 - otherColorComp) * (0.5 + (deviation - pitchAccuracy) / (0.51 - pitchAccuracy) * 0.5)
                        if (deviation < -pitchAccuracy) blueComponent =
                            otherColorComp + (255 - otherColorComp) * (0.5 + (pitchAccuracy + deviation) / (pitchAccuracy - 0.51) * 0.5)
                    }

                    drawLine(
                        Color(
                            redComponent.toInt(),
                            otherColorComp.toInt(),
                            blueComponent.toInt(),
                            255
                        ),
                        Offset(x, y.toFloat()),
                        Offset(x + s.duration / persistScaling.speedFactor+1, y.toFloat()),
                        5f + (s.frequencies[j].variance * 3).toFloat()
                    )
                }
            }
        }
    }
}

