/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/dialogs/A4PitchDialog.kt $
 *
 * This dialog allows to modify the base pitch of A4 note
 *
 */

package com.dabrabyte.eyeformusic.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dabrabyte.eyeformusic.DEFAULT_A4_PITCH
import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.ui.views.OkCheckMark
import com.dabrabyte.eyeformusic.ui.views.CancelCross

const val MIN_PITCH = 420.0f // Constraints on the pitch of A4 to control reasonable adjustments
const val MAX_PITCH = 460.0f

@Composable
fun A4PitchDialog(
    startingPitch: Float,
    actionButtonSize: Dp,
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: (Float) -> Unit // returns the final changed A4 pitch value
) {
    var pitch by remember { mutableFloatStateOf(startingPitch) } // for dynamic update of the value in text box

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Pitch of A4")
        },
        text = {
            Column(Modifier.padding(top=20.dp)) {
                Row {
                    Button(onClick ={ if (pitch > MIN_PITCH) pitch -= 0.1f })
                    { // For fine decrease of pitch
                        Text("-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.weight(1f))
                    Text( // Displaying current pitch value with one decimal
                        "%.1f".format(pitch) + " Hz",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Button(onClick ={ if (pitch < MAX_PITCH) pitch += 0.1f })
                    { // For fine increase of pitch
                        Text("+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black)
                    }
                }
                Slider( // For quick change of pitch and range control
                    value = pitch,
                    onValueChange = { newValue -> pitch = newValue },
                    valueRange = MIN_PITCH..MAX_PITCH,
                    steps = 399,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom=12.dp, top=12.dp)
                )
                Box( modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center)
                {
                    Button( // this is the convenience button for resetting to standard pitch
                        onClick = { pitch = DEFAULT_A4_PITCH }
                    ) {
                        Text("Set to 440 Hz")
                    }
                }
            }
        },
        confirmButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable {
                    onConfirmButtonClicked(pitch)
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


