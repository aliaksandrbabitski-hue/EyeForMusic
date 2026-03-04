/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/dialogs/NotesDialog.kt $
 *
 * This dialog handles labels on white keys of keyboard
 *
 */

package com.dabrabyte.eyeformusic.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dabrabyte.eyeformusic.ui.views.OkCheckMark
import com.dabrabyte.eyeformusic.ui.views.CancelCross

data class NoteCheck(val id: Int, val name: String, var isChecked: Boolean = false)

@Composable
fun NotesDialog(
    actionButtonSize: Dp,
    onDismissRequest: () -> Unit,
    onConfirmButtonClicked: (List<NoteCheck>) -> Unit,
    optionsList: List<NoteCheck> // the set of check marks for 7 notes
) {
    var options by remember { mutableStateOf(optionsList) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Note Labels")
        },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .width(actionButtonSize*4)
                            .padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = option.isChecked,
                            onCheckedChange = { isChecked ->
                                // Update the state when a checkbox is clicked
                                options = options.toMutableList().apply {
                                    this[index] = this[index].copy(isChecked = isChecked)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.name)
                    }
                }
            }
        },
        confirmButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable {
                    onConfirmButtonClicked(options.filter { it.isChecked })
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


