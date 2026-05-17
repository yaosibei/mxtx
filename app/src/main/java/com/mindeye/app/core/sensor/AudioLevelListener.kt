package com.mindeye.app.core.sensor

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.log10

/**
 * 音频监听器 — 带正确清理机制。
 */
class AudioLevelListener(
    private val context: Context
) {
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var currentAudioLevel = 0.0
    private val monitorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    fun startListening() {
        if (isListening) return
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord 初始化失败")
                return
            }
            audioRecord?.startRecording()
            isListening = true
            monitorJob = monitorScope.launch {
                while (isListening) {
                    val level = calculateAudioLevel()
                    currentAudioLevel = level
                    delay(500)
                }
            }
            Log.d(TAG, "音频监听已启动")
        } catch (e: Exception) {
            Log.e(TAG, "启动音频监听失败", e)
        }
    }

    fun stopListening() {
        isListening = false
        monitorJob?.cancel()
        monitorJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "音频监听已停止")
        } catch (e: Exception) {
            Log.e(TAG, "停止音频监听失败", e)
        }
    }

    /**
     * 完全释放资源，应在不再使用时调用。
     */
    fun release() {
        stopListening()
        monitorScope.cancel()
    }

    fun getCurrentAudioLevel(): Double = currentAudioLevel

    private fun calculateAudioLevel(): Double {
        val buffer = ShortArray(bufferSize / 2)
        val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
        if (readSize <= 0) return 0.0
        var sum = 0.0
        for (i in 0 until readSize) {
            val sample = buffer[i].toDouble()
            sum += sample * sample
        }
        val rms = kotlin.math.sqrt(sum / readSize)
        val db = if (rms > 0) 20 * log10(rms / 32767.0) else -Double.MAX_VALUE
        return max(0.0, db + 100)
    }

    companion object {
        private const val TAG = "AudioLevelListener"
    }
}
