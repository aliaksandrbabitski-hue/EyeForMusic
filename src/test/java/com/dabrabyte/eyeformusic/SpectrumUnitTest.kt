/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/SpectrumUnitTest.kt $
 *
 * Unit tests for checking generation of spectrum from simulated audio data
 *
 */

package com.dabrabyte.eyeformusic

import org.junit.Test
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.PI
import com.dabrabyte.eyeformusic.audio.*
import kotlin.assert

class SpectrumUnitTest {
    // test FFT algorithm on simple date made of (0, 1, 0, -1)
    @Test
    fun fft_simple() {
        var a8 = ComplexArray(8) { i -> Complex( if (i%2== 0) .0 else (if (i%4==1) 1.0 else -1.0),.0) }
        var t8 = ComplexArray(8) { Complex(.0, .0) }
        fftSimple(a8, 8, t8, 0, true)

        // check for direct transform
        for (i in 0 until 8) {
            assert(abs(a8[i].im - (if (i%8 == 2) 4.0 else (if (i%8 == 6) -4.0 else .0))) < 0.000001)
            assert(abs(a8[i].re) < 0.000001)
        }

        // check for reverse transform
        fftSimple(a8, 8, t8, 0, false)

        for (i in 0 until 8) {
            assert(abs(a8[i].re - (if (i%2== 0) .0 else (if (i%4==1) 1.0 else -1.0))) < 0.000001)
            assert(abs(a8[i].im) < 0.000001)
        }
    }

    // test of spectrum calculation with integer and float-point number of cycles per data chunk
    @Test
    fun spectrum() {
        val a = ComplexArray(256) { i -> Complex(1000 * sin(i/10.0+1) + 2000 * sin(i/3.7) + 3900 * sin(i/2.2), 0.0) }
        val t = ComplexArray(256) { Complex( .0, .0) }

        var s = Spectrum(0, a, t)
        assert(s.frequencies.size == 3)
        assert(abs(s.frequencies[0].pitch-3190) < 20)
        assert(abs(s.frequencies[0].amplitude-3900) < 270)
        assert(abs(s.frequencies[1].pitch-1894) < 20)
        assert(abs(s.frequencies[1].amplitude -2000) < 20)
        assert(abs(s.frequencies[2].pitch-689) < 20)
        assert(abs(s.frequencies[2].amplitude - 1000) < 150)
    }

    // test for note with overtones
    @Test
    fun noteWithOvertones() {
        val note = 387.597
        val a = ComplexArray(4096) { i -> Complex((1000 * sin(i * 2 * PI * note / SAMPLE_RATE +1) + 1800 * sin(i * 4.026 * PI * note / SAMPLE_RATE) + 500 * sin(i * 6.0003 * PI * note / SAMPLE_RATE + 0.24) + 200 * sin(i * 8.0008 * PI * note / SAMPLE_RATE + 3)), .0) }
        val t = ComplexArray(4096) { Complex( .0, .0) }

        val s = Spectrum(0, a, t)
        assert(s.frequencies.size == 4)
        assert(abs(s.frequencies[0].pitch-779) < 3)
        assert(abs(s.frequencies[0].amplitude-1800) < 120)
        assert(s.frequencies[0].overtoneNum == 2)
        assert(abs(s.frequencies[1].pitch-388) < 1)
        assert(abs(s.frequencies[1].amplitude-1000) < 50)
        assert(s.frequencies[1].overtoneNum == 1)
        assert(abs(s.frequencies[2].pitch-1164) < 5)
        assert(abs(s.frequencies[2].amplitude-500) < 20)
        assert(s.frequencies[2].overtoneNum == 3)
        assert(abs(s.frequencies[3].pitch-1550) < 6)
        assert(abs(s.frequencies[3].amplitude-200) < 10)
        assert(s.frequencies[3].overtoneNum == 4)
    }
}