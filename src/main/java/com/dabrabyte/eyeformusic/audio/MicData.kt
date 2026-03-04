/*
 * (c) Aliaksandr Babitski, Dabrabyte Corp. 2026
 *
 * $Archive: ?/com/dabrabyte/eyeformusic/audio/MicData.kt $
 *
 * Audio data obtained from the microphone
 *
 */

package com.dabrabyte.eyeformusic.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.dabrabyte.eyeformusic.SAMPLE_RATE

// There is only one object for obtaining data from microphone per app
object MicData
{
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO // Audio format supported on the most gadgets
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null // System object for interacting with microphone
    private var recordingThread: Thread? = null // Separate thread (not coroutine) for obtaining audio data
    @Volatile var isRecording: Boolean = false // keeps information if recording audio is in progress
        private set

    private lateinit var audioBufferShort: ShortArray // small array for one audio data chunk

    private var audioBuffer = ShortArray(SAMPLE_RATE) // bigger cyclical array for recent audio data
    @Volatile private var lastAccumIndex = 0 // recent audio data is available in audioBuffer up to this index
    @Volatile private var lastProcessedIndex = 0 // audio data was processed and placed in SpectralSeq up to this index in audioBuffer
    private lateinit var spectralSeq: SpectralSeq // reference to the object that will keep spectra obtained from audio data

    fun init(s: SpectralSeq)
    {
        spectralSeq = s
    }

    fun startRecording() { // prepares buffers and starts the thread for audio recording
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            channelConfig,
            audioFormat
        )

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return
        }

        audioBufferShort = ShortArray(bufferSize)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, // Audio source (microphone)
            SAMPLE_RATE,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread { writeAudioToBuffer() }.also { it.start() }
    }

    private fun writeAudioToBuffer() { // is called in separate thread for obtaining audio data
        while (isRecording) {
            // Read data into the small buffer
            var bytesRead = audioRecord?.read(audioBufferShort, 0, audioBufferShort.size) ?: 0

            if (bytesRead > SAMPLE_RATE)
                bytesRead = SAMPLE_RATE

            if (lastAccumIndex+bytesRead > SAMPLE_RATE) { // new chunk cannot fit in the end of cyclical buffer
                var shiftNum = lastProcessedIndex // some part will be written till the end of audioBuffer
                if (lastAccumIndex+bytesRead - lastProcessedIndex > SAMPLE_RATE) {
                    shiftNum = lastAccumIndex+bytesRead - SAMPLE_RATE
                    lastProcessedIndex = shiftNum
                }

                for (i in lastProcessedIndex until lastAccumIndex) {
                    audioBuffer[i-lastProcessedIndex] = audioBuffer[lastProcessedIndex]
                }
                lastProcessedIndex -= shiftNum
                lastAccumIndex -= shiftNum // adjustment for writing the second part of audio chunk in the beginning of audioBuffer
                spectralSeq.incrNumSamples(shiftNum/2)
            }
            for (i in 0 until bytesRead) {
                audioBuffer[lastAccumIndex+i] = audioBufferShort[i]
            }
            lastAccumIndex += bytesRead;
            // process the new audio data right away
            lastProcessedIndex = spectralSeq.processChunk(audioBuffer, lastAccumIndex, lastProcessedIndex)
        }
    }

    fun stopRecording() {
        isRecording = false
        recordingThread?.join() // Wait for the recording thread to finish
        lastAccumIndex = 0
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
    }
}