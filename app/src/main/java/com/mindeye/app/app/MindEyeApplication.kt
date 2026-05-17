package com.mindeye.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MindEyeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sceneChannel = NotificationChannel(
                CHANNEL_SCENE_ANALYSIS,
                "场景分析服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于环境场景分析的后台服务通知"
                enableVibration(false)
                enableLights(false)
            }

            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "紧急求助",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "一键求助功能的紧急通知"
                enableVibration(true)
                enableLights(true)
            }

            val communityChannel = NotificationChannel(
                CHANNEL_COMMUNITY,
                "社区消息",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "盲人社区互动消息通知"
            }

            val ocrChannel = NotificationChannel(
                CHANNEL_OCR,
                "文字识别",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "OCR 文字识别结果播报通知"
            }

            manager.createNotificationChannels(
                listOf(sceneChannel, emergencyChannel, communityChannel, ocrChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_SCENE_ANALYSIS = "scene_analysis"
        const val CHANNEL_EMERGENCY = "emergency"
        const val CHANNEL_COMMUNITY = "community"
        const val CHANNEL_OCR = "ocr"
    }
}
