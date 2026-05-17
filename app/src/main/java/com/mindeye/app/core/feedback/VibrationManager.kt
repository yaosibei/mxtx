package com.mindeye.app.core.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * 震动管理器
 * 负责不同场景下的震动反馈
 */
class VibrationManager(private val context: Context) {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    /**
     * 正常震动模式
     * 用于一般场景提醒
     */
    fun vibrateNormal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
        Log.d(TAG, "正常震动")
    }

    /**
     * 强震动模式
     * 用于嘈杂环境或重要提醒
     */
    fun vibrateStrong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 300, 100, 300),
                    0
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 300, 100, 300), 0)
        }
        Log.d(TAG, "强震动")
    }

    /**
     * 紧急震动模式
     * 用于危险情况或紧急求助
     */
    fun vibrateEmergency() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500, 200, 500),
                    0
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), 0)
        }
        Log.d(TAG, "紧急震动")
    }

    /**
     * 路口提醒震动
     * 用于通行场景的路口提醒
     */
    fun vibrateForIntersection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100),
                    0
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), 0)
        }
        Log.d(TAG, "路口提醒震动")
    }

    /**
     * 取消所有震动
     */
    fun cancel() {
        vibrator.cancel()
        Log.d(TAG, "取消震动")
    }

    companion object {
        private const val TAG = "VibrationManager"
    }
}
