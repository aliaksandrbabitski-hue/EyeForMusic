/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/audio/SpectralSeq.kt $
 *
 * Set of spectra accumulated during some time interval
 *
 */

package com.dabrabyte.eyeformusic.audio

import com.dabrabyte.eyeformusic.SettingsManager
import com.dabrabyte.eyeformusic.CHUNK_SIZE
import com.dabrabyte.eyeformusic.SAMPLE_RATE

// Set of spectra is used for visualization of the audio data from microphone
// and for illustration purposes in dialogs for settings
class SpectralSeq {
    var spectra = mutableListOf<Spectrum>()
    val spectraLock = Any() // mutex is needed for preventing data corruption because of simultaneous access to spectra from different threads
    private var audioBufferComplex = ComplexArray(CHUNK_SIZE) { Complex(.0, .0)} // the buffer used from transformation of audio data into spectrum
    private var tmpBuffer = ComplexArray(CHUNK_SIZE) { Complex(.0, .0)} // the buffer for intermediate calculation, it helps to avoid extra memory allocations/deallocations
    @Volatile var numSamples: Long = 0 // count of obtained audio samples
        private set
    lateinit var settingsManager: SettingsManager

    var maxAmplitude = .0 // maximal recorded amplitude
        private set
    var expAmplitude = .0 // maximal amplitude adjusted by sequential exponential smoothing
        private set

    constructor(s: SettingsManager, maxAmpl: Double = .0) {
        settingsManager = s
        maxAmplitude = maxAmpl
    }

    fun incrNumSamples(br: Int) {
        numSamples += br
    }

    fun moment(l: Int) : Long {
        return (numSamples + l) * 1000 / SAMPLE_RATE
    }

    // produces spectrum from audio data chunk and returns the last processed index
    fun processChunk(ar: ShortArray, lastObtained: Int, lastProcessed: Int ) : Int {
        if (lastObtained >= lastProcessed + CHUNK_SIZE) {
            for (i in 0 until CHUNK_SIZE) {
                audioBufferComplex[i] = Complex(ar[lastProcessed+i].toDouble(), .0)
                tmpBuffer[i] = Complex(.0, .0)
            }
            try {
                val lastMoment = moment(lastProcessed)
                val sp = Spectrum(lastMoment, audioBufferComplex, tmpBuffer)

                if (sp.frequencies.isNotEmpty()) {
                    if (sp.aggrAmplitude > maxAmplitude) maxAmplitude = sp.aggrAmplitude
                    if (expAmplitude > 0) expAmplitude =
                        0.9 * expAmplitude + 0.1 * sp.aggrAmplitude else expAmplitude =
                        sp.aggrAmplitude
                    synchronized(spectraLock) {
                        while (spectra.isNotEmpty() && spectra.first().fromMoment < lastMoment - settingsManager.soundCacheMs) spectra.removeAt(0)
                        spectra.add(sp)
                    }
                }
                return lastProcessed + CHUNK_SIZE
            }
            catch(e: Exception)
            {
            }
        }
        return lastProcessed
    }

    fun lastProcessed() : Long {
        if (spectra.isNotEmpty()) return spectra.last().fromMoment
        return 0
    }

    fun clear() {
        synchronized(spectraLock) {
            numSamples = 0
            spectra.clear()
        }
    }
}