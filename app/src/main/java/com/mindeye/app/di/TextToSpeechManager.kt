package com.mindeye.app.di

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var initialized = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS 语言不支持")
            } else {
                initialized = true
                Log.d(TAG, "TTS 初始化成功")
            }
        } else {
            Log.e(TAG, "TTS 初始化失败")
        }
    }

    fun speak(text: String) {
        if (initialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w(TAG, "TTS 未初始化")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }

    companion object {
        private const val TAG = "TextToSpeechManager"
    }
}
