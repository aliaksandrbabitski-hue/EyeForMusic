/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/audio/Spectrum.kt $
 *
 * Spectrum represents a set of frequencies audible during some time interval
 *
 */

package com.dabrabyte.eyeformusic.audio

import com.dabrabyte.eyeformusic.MAX_VISIBLE_PITCH
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.sin
import com.dabrabyte.eyeformusic.SAMPLE_RATE

// This data structure holds parameters of one essential frequency of a sound
class Frequency ( val pitch : Double, // Hz
                  val angle: Double, // Radians
    val variance: Double, // Hz
    val amplitude: Double, // Scaled to amplitude of the original signal
    val overtoneNum: Int = 1 // Number of the overtone
    ) { // Scaled to amplitude of the original signal
}

// Audio data representation in the form of the essential sound frequencies
class Spectrum {
    val maxFreq  = 30 // maximal number of significant frequencies saved per spectrum
    val amplCutOff = 0.15 // part of the average amplitude used for discarding quiet sounds

    var fromMoment: Long = 0 // start moment of the audio data chunk used for calculation of this spectrum
        private set
    var duration: Int = 0 // audio data chunk duration
        private set
    var aggrAmplitude: Double = 0.0 // aggregated amplitude of the spectrum
        private set
    var frequencies: MutableList<Frequency> = mutableListOf() // the set of the main frequencies

    // Spectrum is created from fragment of audio data from microphone
    constructor(beginMoment: Long, // begin moment of audio data in ms
                complexAr: ComplexArray, // array with audio data in real parts and 0 in imaginary parts
                tmpAr: ComplexArray ) { // temporary array for quick swaps and avoiding memory allocation/deallocation
        fromMoment = beginMoment
        duration = complexAr.size * 1000 / SAMPLE_RATE
        aggrAmplitude = 0.0

        for (i in 0 until complexAr.size) {
            aggrAmplitude += abs(complexAr[i].re)
        }
        if (complexAr.isNotEmpty()) {
            aggrAmplitude /= complexAr.size
        }

        // Fast Fourier transform is used for converting audio data into spectral data
        fftSimple( complexAr, complexAr.size, tmpAr, 0,true)
        val threshold = aggrAmplitude * amplCutOff
        val spectrumLen = complexAr.size/2
        val maxVisiblePitch = MAX_VISIBLE_PITCH / SAMPLE_RATE * complexAr.size
        var prevAmpl = complexAr[0].amplitude() / spectrumLen
        var ampl = complexAr[1].amplitude() / spectrumLen
        var prevAngleDiff = complexAr[0].angleDiff(complexAr[1])
        for (i in 1 until maxVisiblePitch.toInt()) {
            val nextAmpl = complexAr[i+1].amplitude() / spectrumLen
            val nextAngleDiff = complexAr[i].angleDiff(complexAr[i+1])
            var pitch = 0.0
            var combinedAmpl = ampl
            var variance = 1.0
            if (ampl > threshold) { // determines spectral peak for detecting essential frequency
                var smallerAmpl = prevAmpl
                var biggerAmpl = nextAmpl
                var amplAdj = 1
                if (prevAmpl > nextAmpl) {
                    smallerAmpl = nextAmpl
                    biggerAmpl = prevAmpl
                    amplAdj = -1
                }
                val diffNeighbAngle = if (prevAmpl > nextAmpl) prevAngleDiff else nextAngleDiff
                val angleSwitch = abs(diffNeighbAngle - PI) < 0.1
                if (biggerAmpl < ampl && (angleSwitch || ampl > biggerAmpl * 2)) {
                    pitch = SAMPLE_RATE * i.toDouble() / complexAr.size
                    if (angleSwitch) {
                        pitch += SAMPLE_RATE * atan((biggerAmpl-smallerAmpl)/(ampl-biggerAmpl))/PI * amplAdj / complexAr.size
                        combinedAmpl = ampl + biggerAmpl - smallerAmpl
                    }
                    else {
                        variance += (abs(sin(prevAngleDiff))*prevAmpl + abs(sin(nextAngleDiff))*nextAmpl) / ampl * SAMPLE_RATE / complexAr.size / 2
                    }
                }
            }

            if (pitch > 0 && pitch < MAX_VISIBLE_PITCH && (frequencies.size < maxFreq || frequencies.isNotEmpty() && frequencies.last().amplitude < combinedAmpl)) {
                // removing quieter sound if it necessary allocate a louder sound
                if (frequencies.size >= maxFreq) frequencies.removeAt(frequencies.lastIndex)
                var base = 0.0
                var overCnt = 0
                var accAmpl = 0.0

                for (f in frequencies) { // search for the fundamental sound for the current frequency
                    val overtoneCand = pitch / f.pitch * f.overtoneNum
                    val overtoneDev = overtoneCand - (overtoneCand + 0.5).toInt()
                    if (abs(overtoneDev) > 0.05 * overtoneCand) continue // it is not from this set of overtones
                    if (base > 0 && abs(pitch / overtoneCand - base) > base * 0.05) {
                        if (accAmpl > combinedAmpl + f.amplitude) continue // don't switch to quieter base
                        base = f.pitch / f.overtoneNum // switch to louder base
                        overCnt = 0
                        accAmpl = 0.0
                    }
                    overCnt++ //found overtone from the same set
                    base = (base * accAmpl + f.pitch / f.overtoneNum * f.amplitude) / (accAmpl + f.amplitude)
                    accAmpl += f.amplitude
                }
                var overNum = 1
                if (base > 0) {
                    val n = (pitch/base+0.5).toInt()
                    if (n-1 <= overCnt * 2) overNum = n // assign overtone number only if at least half of the previous overtones are already accumulated
                }
                var pos = frequencies.binarySearch { if (it.amplitude > combinedAmpl) -1 else 1 }
                frequencies.add(if (pos < 0) -pos-1 else pos, Frequency(pitch, complexAr[i].angle(), variance, combinedAmpl, overNum))
            }
            prevAmpl = ampl
            ampl = nextAmpl
            prevAngleDiff = nextAngleDiff
        }
        if (frequencies.isNotEmpty()) aggrAmplitude = frequencies[0].amplitude
    }

    // It is used for fragments in dialogs for settings and for unit tests
    constructor(pitch : Double, ampl: Double, frMoment: Long = 0, dur: Int = 1, overNum: Int = 1) {
        val f = Frequency(pitch ,  0.0, 1.0, ampl, overNum)
        fromMoment = frMoment
        duration = dur
        frequencies.add(f)
        aggrAmplitude = ampl
    }
}