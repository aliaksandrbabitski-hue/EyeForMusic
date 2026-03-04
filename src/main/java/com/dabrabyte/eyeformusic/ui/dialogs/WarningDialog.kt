/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/dialogs/WarningDialog.kt $
 *
 * This dialog displays warning information
 *
 */

package com.dabrabyte.eyeformusic.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.dabrabyte.eyeformusic.ui.views.CancelCross

@Composable
fun WarningDialog(actionButtonSize: Dp,
                  text: String,
                  onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Warning")
        },
        text = { Text(text) },
        dismissButton = {
            Box(Modifier
                .width(actionButtonSize)
                .height(actionButtonSize)
                .clickable { onDismissRequest() })
            {
                CancelCross(Modifier.fillMaxSize())
            }
        },
        confirmButton = {}
    )
}