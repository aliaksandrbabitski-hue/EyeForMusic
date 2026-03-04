/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/views/KeyboardView.kt $
 *
 * Right side keyboard
 *
 */

package com.dabrabyte.eyeformusic.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.dabrabyte.eyeformusic.NUM_OCTAVES
import com.dabrabyte.eyeformusic.ui.theme.DarkestShadow
import com.dabrabyte.eyeformusic.ui.theme.ExtTheme

@Composable
fun KeyboardView(noteLabels: Int,
                 persistScaling: PersistScaling,
                 modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val halfToneStepPx = persistScaling.halfToneStepPx
    val eColors = ExtTheme.colors

    Canvas(modifier) {
        val whiteKeyStep = halfToneStepPx / 7 * 12
        val shadowWidth = halfToneStepPx / 20

        // black keys
        drawRect(eColors.greyL, Offset(0.0f, 0.0f),  this.size)
        drawLine(eColors.greyLL, Offset(this.size.width - shadowWidth/2, 0f),  Offset(this.size.width-shadowWidth/2, this.size.height), shadowWidth)

        // shadows
        for (i in  0 .. (this.size.height/whiteKeyStep).toInt()) {
            drawLine(eColors.grey, Offset(0.0f, i * whiteKeyStep+0.5f*shadowWidth), Offset(this.size.width, i * whiteKeyStep+0.5f*shadowWidth), shadowWidth)
            drawLine(eColors.greyD, Offset(0.0f, i * whiteKeyStep+1.5f*shadowWidth), Offset(this.size.width, i * whiteKeyStep + 1.5f*shadowWidth), shadowWidth)
            drawLine(eColors.greyLL, Offset(0.0f, i * whiteKeyStep+2.5f*shadowWidth), Offset(this.size.width, i * whiteKeyStep + 2.5f*shadowWidth), shadowWidth)
        }

        for (i in 0 .. ((this.size.height/whiteKeyStep-1)*5/7).toInt()) {
            val blackKeyOffset = i * 7.0f / 5 * whiteKeyStep + (i % 5 * 2 + (if(i % 5 > 2)  2 else 1)) * halfToneStepPx - i % 5 * 7 * whiteKeyStep / 5;
            //          drawRect(colorLightGrey, Offset(0.0f, blackKeyOffset), Size(this.size.width-keyLength, halfToneStep))
            val blackKeyRight = this.size.width*0.55f;
            drawRect(eColors.greyDD, Offset(0f, blackKeyOffset), Size(blackKeyRight, halfToneStepPx))
            drawLine(eColors.grey, Offset(0f, blackKeyOffset+shadowWidth), Offset(blackKeyRight-shadowWidth/2, blackKeyOffset+shadowWidth), shadowWidth)
            drawLine(eColors.greyD, Offset(blackKeyRight-shadowWidth, blackKeyOffset+shadowWidth/2), Offset(blackKeyRight-shadowWidth, blackKeyOffset+halfToneStepPx-shadowWidth/2), shadowWidth)
            drawLine(DarkestShadow,Offset(0f, blackKeyOffset+halfToneStepPx-shadowWidth/2), Offset(blackKeyRight, blackKeyOffset+halfToneStepPx-shadowWidth/2), shadowWidth)
        }

        // Note labels on white keys
        var textX = this.size.width * 0.6f
        var textYPixels = this.size.width * 0.3f
        val whiteSpace = whiteKeyStep * 0.86f
        if (textYPixels > whiteSpace) {
            textX += (textYPixels-whiteSpace)/2
            textYPixels = whiteSpace
        }
        val spFontSize = with(density) { textYPixels.toDp().toSp() }
        val notes = "CDEFGAB"
        var noteMask = 1
        for (n in 1 .. 7) {
            for (i in 0 until NUM_OCTAVES) {
                if (noteLabels and noteMask != 0) {
                    var note = notes.substring(n - 1, n)
                    note += i.toString()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = note,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = spFontSize,
                            color = DarkestShadow
                        ),
                        topLeft = Offset(
                            textX,
                            ((NUM_OCTAVES - i) * 7 + 0.5f - n) * whiteKeyStep - textYPixels / 2
                        )
                    )
                }
            }
            noteMask = noteMask shl 1
        }
    }
}