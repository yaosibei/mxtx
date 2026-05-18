package com.mindeye.app.feature.mindeye.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import com.mindeye.app.BuildConfig
import org.json.JSONObject
import java.lang.reflect.Method
import java.util.Locale
import java.util.UUID
import java.lang.reflect.Proxy

object XunfeiSpeechManager {

    private const val TAG = "XunfeiSpeechManager"

    private var appContext: Context? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var xunfeiRecognizer: Any? = null
    private var isTtsReady = false
    private var hasInit = false
    private val xunfeiResultSegments = linkedMapOf<String, String>()

    fun initXunfei(context: Context) {
        if (hasInit) return

        appContext = context.applicationContext
        initSystemTts(context.applicationContext)
        initSystemStt(context.applicationContext)
        tryInitXunfeiSdk(context.applicationContext)
        tryInitXunfeiRecognizer(context.applicationContext)
        hasInit = true
    }

    fun hasRecordAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(callback: (String) -> Unit) {
        val context = appContext
        if (context == null) {
            Log.e(TAG, "startListening 调用失败，未完成初始化")
            callback("")
            return
        }

        if (!hasRecordAudioPermission(context)) {
            Log.e(TAG, "缺少录音权限，无法启动语音识别")
            callback("")
            return
        }

        speak("请说出您要去哪里") {
            // 每次启动识别前都清理一次分段结果，确保识别内容不串
            xunfeiResultSegments.clear()
            
            if (xunfeiRecognizer != null) {
                Log.d(TAG, "优先启动讯飞 SDK 语音识别")
                startXunfeiListening(callback)
            } else {
                Log.w(TAG, "讯飞 SDK 不可用，回退系统语音识别")
                startSystemListening(callback)
            }
        }
    }

    private fun startSystemListening(callback: (String) -> Unit) {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            val errorMsg = "当前设备系统语音服务不可用，且讯飞 SDK 未就绪"
            Log.e(TAG, errorMsg)
            XunfeiSpeechManager.speak("语音识别服务暂时不可用，请稍后再试")
            callback("")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.CHINESE.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer.cancel()
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "系统识别器已就绪")
            }

            override fun onBeginningOfSpeech() = Unit

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NETWORK -> "网络连接失败"
                    SpeechRecognizer.ERROR_NO_MATCH -> "未能识别到声音"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "语音服务忙"
                    else -> "错误代码 $error"
                }
                Log.e(TAG, "系统语音识别失败: $message")
                callback("")
            }

            override fun onResults(results: Bundle?) {
                val texts = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    .orEmpty()
                val result = texts.firstOrNull().orEmpty().trim()
                Log.d(TAG, "系统识别结果: $result")
                callback(result)
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "启动系统语音识别异常: ${e.message}")
            callback("")
        }
    }

    private fun startXunfeiListening(callback: (String) -> Unit) {
        val recognizer = xunfeiRecognizer ?: run {
            startSystemListening(callback)
            return
        }

        try {
            xunfeiResultSegments.clear()
            configureXunfeiRecognizer(recognizer)
            val listenerClass = Class.forName("com.iflytek.cloud.RecognizerListener")
            val listener = Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, args ->
                when (method.name) {
                    "onResult" -> {
                        val recognizerResult = args?.getOrNull(0)
                        val isLast = args?.getOrNull(1) as? Boolean ?: false
                        val json = recognizerResult
                            ?.javaClass
                            ?.getMethod("getResultString")
                            ?.invoke(recognizerResult) as? String
                        if (!json.isNullOrBlank()) {
                            mergeXunfeiResult(json)
                        }
                        if (isLast) {
                            callback(buildXunfeiResult())
                        }
                    }

                    "onError" -> {
                        val error = args?.getOrNull(0)
                        val message = error
                            ?.javaClass
                            ?.methods
                            ?.firstOrNull { it.name == "getPlainDescription" && it.parameterTypes.size == 1 }
                            ?.invoke(error, true) as? String
                        Log.e(TAG, "讯飞语音识别失败: $message")
                        callback("")
                    }
                }
                null
            }

            recognizer.javaClass.getMethod("cancel").invoke(recognizer)
            recognizer.javaClass
                .getMethod("startListening", listenerClass)
                .invoke(recognizer, listener)
        } catch (throwable: Throwable) {
            Log.e(TAG, "启动讯飞识别失败，回退系统识别: ${throwable.message}", throwable)
            startSystemListening(callback)
        }
    }

    fun speak(text: String, onComplete: () -> Unit = {}) {
        val tts = textToSpeech
        if (!isTtsReady || tts == null) {
            Log.w(TAG, "TTS 尚未就绪，直接跳过播报: $text")
            onComplete()
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        completionActions[utteranceId] = onComplete
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        xunfeiRecognizer?.javaClass?.methods?.firstOrNull { it.name == "stopListening" }?.invoke(xunfeiRecognizer)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun release() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        xunfeiRecognizer?.javaClass?.methods?.firstOrNull { it.name == "cancel" }?.invoke(xunfeiRecognizer)
        xunfeiRecognizer?.javaClass?.methods?.firstOrNull { it.name == "destroy" }?.invoke(xunfeiRecognizer)
        xunfeiRecognizer = null

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsReady = false
        hasInit = false
        completionActions.clear()
    }

    private fun initSystemTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                isTtsReady = false
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    utteranceId?.let { id ->
                        completionActions.remove(id)?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    utteranceId?.let { id ->
                        completionActions.remove(id)?.invoke()
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    utteranceId?.let { id ->
                        completionActions.remove(id)?.invoke()
                    }
                }
            })
        }
    }

    private fun initSystemStt(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            Log.w(TAG, "当前设备系统语音识别服务不可用，将优先依赖讯飞 SDK")
        }
    }

    private fun tryInitXunfeiSdk(context: Context) {
        if (BuildConfig.XUNFEI_APP_ID.isBlank()) {
            Log.w(TAG, "未配置讯飞 APPID，跳过讯飞 SDK 初始化")
            return
        }

        try {
            val speechUtilityClass = Class.forName("com.iflytek.cloud.SpeechUtility")
            val createUtility: Method = speechUtilityClass.getMethod(
                "createUtility",
                Context::class.java,
                String::class.java
            )
            createUtility.invoke(
                null,
                context,
                "appid=${BuildConfig.XUNFEI_APP_ID}"
            )
            Log.d(TAG, "讯飞 SDK 初始化完成")
        } catch (throwable: Throwable) {
            // 工程当前未内置讯飞 AAR/JAR 时，使用系统 STT/TTS 保持流程可运行
            Log.w(TAG, "未检测到讯飞 SDK，本次使用系统语音能力兜底: ${throwable.message}")
        }
    }

    private fun tryInitXunfeiRecognizer(context: Context) {
        if (BuildConfig.XUNFEI_APP_ID.isBlank()) return
        try {
            val initListenerClass = Class.forName("com.iflytek.cloud.InitListener")
            val speechRecognizerClass = Class.forName("com.iflytek.cloud.SpeechRecognizer")
            val initListener = Proxy.newProxyInstance(
                initListenerClass.classLoader,
                arrayOf(initListenerClass)
            ) { _, method, args ->
                if (method.name == "onInit") {
                    Log.d(TAG, "讯飞识别器初始化结果: ${args?.firstOrNull()}")
                }
                null
            }
            val createRecognizer = speechRecognizerClass.getMethod(
                "createRecognizer",
                Context::class.java,
                initListenerClass
            )
            xunfeiRecognizer = createRecognizer.invoke(null, context, initListener)
            xunfeiRecognizer?.let { configureXunfeiRecognizer(it) }
        } catch (throwable: Throwable) {
            xunfeiRecognizer = null
            Log.w(TAG, "讯飞识别器初始化失败，将回退到系统识别: ${throwable.message}")
        }
    }

    private fun configureXunfeiRecognizer(recognizer: Any) {
        val setParameter = recognizer.javaClass.getMethod("setParameter", String::class.java, String::class.java)
        setParameter.invoke(recognizer, "params", null)
        setParameter.invoke(recognizer, speechConstant("ENGINE_TYPE", "engine_type"), speechConstant("TYPE_CLOUD", "cloud"))
        setParameter.invoke(recognizer, speechConstant("DOMAIN", "domain"), "iat")
        setParameter.invoke(recognizer, speechConstant("RESULT_TYPE", "result_type"), "json")
        setParameter.invoke(recognizer, speechConstant("LANGUAGE", "language"), "zh_cn")
        setParameter.invoke(recognizer, speechConstant("ACCENT", "accent"), "mandarin")
        setParameter.invoke(recognizer, speechConstant("VAD_BOS", "vad_bos"), "4000")
        setParameter.invoke(recognizer, speechConstant("VAD_EOS", "vad_eos"), "1500")
        setParameter.invoke(recognizer, speechConstant("ASR_PTT", "asr_ptt"), "0")
    }

    private fun speechConstant(fieldName: String, fallback: String): String {
        return try {
            val speechConstantClass = Class.forName("com.iflytek.cloud.SpeechConstant")
            speechConstantClass.getField(fieldName).get(null) as? String ?: fallback
        } catch (_: Throwable) {
            fallback
        }
    }

    private fun mergeXunfeiResult(json: String) {
        val root = JSONObject(json)
        val sn = root.optString("sn", xunfeiResultSegments.size.toString())
        val words = buildString {
            val wsArray = root.optJSONArray("ws") ?: return@buildString
            for (i in 0 until wsArray.length()) {
                val wsItem = wsArray.optJSONObject(i) ?: continue
                val cwArray = wsItem.optJSONArray("cw") ?: continue
                val candidate = cwArray.optJSONObject(0)?.optString("w").orEmpty()
                append(candidate)
            }
        }
        if (words.isNotBlank()) {
            xunfeiResultSegments[sn] = words
        }
    }

    private fun buildXunfeiResult(): String {
        return xunfeiResultSegments
            .toSortedMap(compareBy { it.toIntOrNull() ?: Int.MAX_VALUE })
            .values
            .joinToString(separator = "")
            .trim()
    }

    private val completionActions = mutableMapOf<String, () -> Unit>()
}
