/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/ui/theme/Color.kt $
 *
 * Sets of colors used in app
 *
 */

package com.dabrabyte.eyeformusic.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val TerraD = Color(0xFFDDAEA0)
val TerraGD = Color(0xFFCCAA90)
val TerraND = Color(0xFFEFD0D8)
val DialogBackD = Color(0xFF102030)


val TerraL = Color(0xFFC07060)
val TerraGL = Color(0xFFC0B0A0)
val TerraNL = Color(0xFFDDA0B0)


val ConfirmCheck = Color(0xFF6EAA14)
val DarkestShadow = Color.Black

@Immutable
data class ExtendedColors(
    val action: Color,
    val greyLL: Color,
    val greyL: Color,
    val grey: Color,
    val greyD: Color,
    val greyDD: Color,
    val whiteKeySeparator: Color,
    val whiteKeyField: Color
)

val AppExtendedColors = staticCompositionLocalOf {
    ExtendedColors(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)
}

val lightExtendedColors = ExtendedColors(
    Color(0xFFDD4030),
    Color.White,
    Color(0xFFE6E6E6),
    Color(0xFF969696),
    Color(0xFF505050),
    Color(0xFF282828),
    Color(0xFFF0EAE5),
    Color.White
)

val darkExtendedColors = ExtendedColors(
    Color(0xFFFF5040),
    Color(0xFFAAAAAA),
    Color(0xFF939393),
    Color(0xFF757565),
    Color(0xFF323232),
    Color(0xFF191919),
    Color.Black,
    Color(0xFF162008)
)