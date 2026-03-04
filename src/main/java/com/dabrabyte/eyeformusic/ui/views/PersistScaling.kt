/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/PersistScaling.kt $
 *
 * Class to keep scaling info in-between life cycles of views
 *
 */

package com.dabrabyte.eyeformusic.ui.views

import androidx.compose.ui.geometry.Offset
import com.dabrabyte.eyeformusic.NUM_KEYS
import com.dabrabyte.eyeformusic.SettingsManager

// Values for preserving scaling and scrolling positions
class PersistScaling {
    val speedFactorTimesRange = 7f // magnitude of horizontal scaling
    val scaleYTimesRange = 6f // magnitude of vertical scaling

    var halfToneStepPx: Float = 1f // Number of vertical pixels per half tone as per current scale
        private set
    var speedFactor = 1f // sound duration in ms that makes one horizontal pixel
        private set
    var maxSpeedFactor = 1f // maximal speed for horizontal movement
        private set
    var minHalfToneStepPx = 1f // minimal height between halftones for the full range of keys on screen
        private set
    var lastVRelativePos = 0.25f // the last vertical scrolling position during dynamic scaling
    var lastHRelativePos = 1f // the last horizontal scrolling position during dynamic scaling
    var lastCenter = Offset(0f, 0f) // scaling center
    var startSpeedFactor = speedFactor // speedFactor value before dynamic scaling
    var startScaleY = 1f // Vertical scale factor before dynamic scaling
    lateinit var settingsManager: SettingsManager
    var menuHeight = 100 // Action menu height used for calculation of vertical menu position

    fun init(minStepPx: Float, scale_Y: Float, maxSF: Float, s: SettingsManager) {
        settingsManager = s
        minHalfToneStepPx = minStepPx
        maxSpeedFactor = maxSF
        halfToneStepPx = minHalfToneStepPx * scale_Y
        if (halfToneStepPx < minHalfToneStepPx) halfToneStepPx = minHalfToneStepPx
        speedFactor = if (speedFactor == 1.0f) maxSpeedFactor / 4 else speedFactor.coerceIn(
            maxSpeedFactor / speedFactorTimesRange,  maxSpeedFactor) // ensure speedFactor is within allowed range
    }

    fun begin(centroid: Offset, hScroll: Int, vScroll: Int, scale_Y: Float) {
        lastCenter = centroid
        lastVRelativePos =(vScroll + centroid.y) / halfToneStepPx / NUM_KEYS
        lastHRelativePos = (hScroll + centroid.x) * speedFactor / settingsManager.soundCacheMs
        startSpeedFactor = speedFactor
        startScaleY = scale_Y
    }

    fun dynamicX(centroid: Offset, factorX: Float): Float {
        lastCenter = centroid
        return factorX.coerceIn(startSpeedFactor / maxSpeedFactor, speedFactorTimesRange * startSpeedFactor / maxSpeedFactor)
    }

    fun end(factorX: Float, factorY: Float, scale_Y: Float): Float {
        halfToneStepPx = minHalfToneStepPx * scale_Y
        speedFactor = (startSpeedFactor / factorX).coerceIn(maxSpeedFactor / speedFactorTimesRange, maxSpeedFactor)
        return startSpeedFactor / speedFactor
    }
}