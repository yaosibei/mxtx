package com.mindeye.app.feature.mindeye.travel.data

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Base64
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mindeye.app.BuildConfig
import okhttp3.*
import okio.ByteString
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XunfeiTtsManager @Inject constructor(
    private val client: OkHttpClient
) {
    private var audioTrack: AudioTrack? = null
    private var webSocket: WebSocket? = null

    companion object {
        private const val TAG = "XunfeiTtsManager"
        private const val HOST = "tts-api.xfyun.cn"
        private const val URL_PATH = "/v2/tts"
    }

    fun speak(text: String) {
        stop()
        val authUrl = getAuthUrl()
        val request = Request.Builder().url(authUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                sendTtsRequest(webSocket, text)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Websocket failure: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }
        })
    }

    fun stop() {
        webSocket?.close(1000, "User stop")
        webSocket = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    private fun sendTtsRequest(webSocket: WebSocket, text: String) {
        val requestJson = JsonObject().apply {
            add("common", JsonObject().apply {
                addProperty("app_id", BuildConfig.XUNFEI_APP_ID)
            })
            add("business", JsonObject().apply {
                addProperty("aue", "raw") // raw for pcm
                addProperty("auf", "audio/L16;rate=16000")
                addProperty("vcn", "xiaoyan") // default voice
                addProperty("pitch", 50)
                addProperty("speed", 50)
                addProperty("tte", "UTF8")
            })
            add("data", JsonObject().apply {
                addProperty("status", 2)
                addProperty("text", Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
            })
        }
        webSocket.send(requestJson.toString())
    }

    private fun handleMessage(message: String) {
        try {
            val jsonResponse = JsonParser.parseString(message).asJsonObject
            val code = jsonResponse.get("code").asInt
            if (code != 0) {
                Log.e(TAG, "TTS Error: $code")
                return
            }
            val data = jsonResponse.getAsJsonObject("data")
            val status = data.get("status").asInt
            val audioBase64 = data.get("audio").asString
            val audioData = Base64.decode(audioBase64, Base64.DEFAULT)

            playAudio(audioData)

            if (status == 2) { // Final frame
                webSocket?.close(1000, "Done")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message", e)
        }
    }

    private fun playAudio(audioData: ByteArray) {
        if (audioTrack == null) {
            val minBufferSize = AudioTrack.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBufferSize, audioData.size),
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        }
        audioTrack?.write(audioData, 0, audioData.size)
    }

    private fun getAuthUrl(): String {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val date = sdf.format(Date())

        val signatureOrigin = "host: $HOST\ndate: $date\nGET $URL_PATH HTTP/1.1"
        val mac = Mac.getInstance("HmacSHA256")
        val spec = SecretKeySpec(BuildConfig.XUNFEI_API_SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(spec)
        val signatureSha = mac.doFinal(signatureOrigin.toByteArray(Charsets.UTF_8))
        val signature = Base64.encodeToString(signatureSha, Base64.NO_WRAP)

        val authorizationOrigin = "api_key=\"${BuildConfig.XUNFEI_API_KEY}\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$signature\""
        val authorization = Base64.encodeToString(authorizationOrigin.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        val httpUrl = HttpUrl.Builder()
            .scheme("https")
            .host(HOST)
            .addPathSegment("v2")
            .addPathSegment("tts")
            .addQueryParameter("authorization", authorization)
            .addQueryParameter("date", date)
            .addQueryParameter("host", HOST)
            .build()

        return httpUrl.toString().replace("https://", "wss://")
    }
}
