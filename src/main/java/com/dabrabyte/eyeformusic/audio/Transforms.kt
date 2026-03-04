/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/audio/Transforms.kt $
 *
 * Audio data processing algorithms
 *
 */

package com.dabrabyte.eyeformusic.audio

import kotlin.math.*

// utility functions for working with complex numbers
data class Complex(val re: Double, val im: Double) {
    operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
    operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
    operator fun times(other: Complex) = Complex(re * other.re - im * other.im, re * other.im + im * other.re)
    fun amplitude() : Double { return sqrt(re * re + im * im) }
    fun angle() : Double { return atan2(im, re) }
    fun angleDiff(x: Complex) : Double { var diff = abs(angle() - x.angle()); return (if (diff>PI) 2*PI-diff else diff) }
}

typealias ComplexArray = Array<Complex>

// Fast Fourier transform
fun fftSimple( a: ComplexArray, n: Int, tmp: ComplexArray, of: Int, direct: Boolean)
{
    if(n<=1) return
    val m = n/2
    for(i in 0 until m) {
        tmp[of+i] = a[2*i+of]
        tmp[of+i+n/2] = a[2*i+1+of]
    }
    fftSimple( tmp, m, a, of, direct)		// FFT on even-indexed elements of v[]
    fftSimple( tmp, m, a, of+m, direct )		// FFT on odd-indexed elements of v[]

    val ang = 2 * PI / n * (if (direct) 1.0 else -1.0)
    var w = Complex(1.0, 0.0)
    val wn = Complex(cos(ang), sin(ang))
    for (i in 0 until m) {
        a[i+of] = tmp[i+of].plus(w.times(tmp[i+of+m]))
        a[i+m+of] = tmp[i+of].minus(w.times(tmp[i+of+m]))
        if (!direct) {
            a[i+of] *= Complex(0.5, .0)
            a[i+m+of] *= Complex(0.5, .0)
        }
        w *= wn;
    }
}
