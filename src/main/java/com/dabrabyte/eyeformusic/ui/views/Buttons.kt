/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/views/Buttons.kt $
 *
 * Action buttons
 *
 */

package com.dabrabyte.eyeformusic.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dabrabyte.eyeformusic.ui.theme.ConfirmCheck
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Button for turning on/off audio signal accumulation from microphone
@Composable
fun RecordingButton(modifier: Modifier = Modifier, recordingConfirmed: Boolean) {
    val eColors = ExtTheme.colors

    Canvas(modifier) {

        drawCircle(eColors.action, this.size.width * 0.38f, Offset(this.size.width/2.0f, this.size.height/2.0f), 1f, Stroke(this.size.width / 12))

        if (recordingConfirmed) { // two vertical bars for stopping audio signal accumulation
            drawLine(eColors.action, Offset(this.size.width * 0.39f, this.size.height * 0.36f),
                Offset(this.size.width * 0.39f, this.size.height * 0.64f), this.size.width / 12
            )
            drawLine(eColors.action, Offset(this.size.width * 0.61f, this.size.height * 0.36f),
                Offset(this.size.width * 0.61f, this.size.height * 0.64f), this.size.width / 12
            )
        } else { // triangle for starting audio signal accumulation
            val sz = this.size
            val path = Path().apply {
                // Define the path's shape
                moveTo(sz.width * 0.63f, sz.height / 2)
                lineTo(sz.width * 0.44f, sz.height * 0.37f)
                lineTo(sz.width * 0.44f, sz.height * 0.63f)
                close()
            }

            drawPath(path, eColors.action, 1.0f, Stroke(this.size.width / 15))
        }
    }
}

// It draws red cross for canceling operation and closing the active window
@Composable
fun CancelCross(modifier: Modifier = Modifier) {
    val eColors = ExtTheme.colors
    Canvas(modifier) {
        drawLine(
            eColors.action, Offset(this.size.width / 5, this.size.height / 5),
            Offset(this.size.width * 4 / 5, this.size.height * 4 / 5), this.size.width / 10
        )
        drawLine(
            eColors.action, Offset(this.size.width * 4 / 5, this.size.height / 5),
            Offset(this.size.width / 5, this.size.height * 4 / 5), this.size.width / 10
        )
    }
}

// It draws "Ok" check mark for confirming change of a setting
@Composable
fun OkCheckMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sz = this.size
        val path = Path().apply {
            // Define the path's shape
            moveTo(sz.width * 0.2f, sz.height * 0.28f)
            lineTo(sz.width * 0.43f, sz.height * 0.75f)
            lineTo(sz.width * 0.8f, sz.height * 0.2f)
        }
        drawPath(path, ConfirmCheck, 1.0f, Stroke(sz.width / 10))
    }
}

// It draws gear for showing the menu or the red cross for removing currently shown menu
@Composable
fun SettingsButton(modifier: Modifier = Modifier, settingsOn: Boolean) {
    val eColors = ExtTheme.colors
    if (settingsOn) CancelCross(modifier)
    else Canvas(modifier) {
        val radius = this.size.width * 0.29f
        val gearInner = this.size.width * 0.31f
        val gearOuter = this.size.width * 0.37f
        val ang = PI / 38
        val center = this.size.width / 2
        drawCircle(eColors.action, radius, Offset(center, center), 1f, Stroke(this.size.width / 13))

        for (i in 0 until 12) { // draws teeth of gear as rotated trapezoids
            val x = this.size.width / 2 + radius * sin(PI * i / 6)

            val path = Path().apply {
                // Define the path's shape
                moveTo(
                    center + gearInner * sin(PI * i / 6 - ang).toFloat(),
                    center + gearInner * cos(PI * i / 6 - ang).toFloat()
                )
                lineTo(
                    center + gearOuter * sin(PI * i / 6 - ang * 0.7).toFloat(),
                    center + gearOuter * cos(PI * i / 6 - ang * 0.7).toFloat()
                )
                lineTo(
                    center + gearOuter * sin(PI * i / 6 + ang * 0.7).toFloat(),
                    center + gearOuter * cos(PI * i / 6 + ang * 0.7).toFloat()
                )
                lineTo(
                    center + gearInner * sin(PI * i / 6 + ang).toFloat(),
                    center + gearInner * cos(PI * i / 6 + ang).toFloat()
                )
                close()
            }

            drawPath(path, eColors.action, 1.0f, Stroke(this.size.width / 20))
        }
    }
}
