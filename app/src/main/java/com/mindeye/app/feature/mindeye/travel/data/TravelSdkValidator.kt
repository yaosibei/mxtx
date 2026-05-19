package com.mindeye.app.feature.mindeye.travel.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mindeye.app.BuildConfig

data class TravelSdkValidationResult(
    val amapKeyConfigured: Boolean,
    val xunfeiAppIdConfigured: Boolean,
    val xunfeiApiKeyConfigured: Boolean,
    val xunfeiApiSecretConfigured: Boolean,
    val xunfeiSdkAvailable: Boolean,
    val xunfeiRecognizerAvailable: Boolean,
    val microphonePermissionDeclared: Boolean,
    val cameraPermissionDeclared: Boolean,
    val networkPermissionDeclared: Boolean
) {
    val canUseAmapNavigation: Boolean
        get() = amapKeyConfigured

    val canUseFullXunfeiVoiceChain: Boolean
        get() = xunfeiAppIdConfigured &&
            xunfeiApiKeyConfigured &&
            xunfeiApiSecretConfigured &&
            xunfeiSdkAvailable &&
            xunfeiRecognizerAvailable

    val hasHardBlocker: Boolean
        get() = !microphonePermissionDeclared || !cameraPermissionDeclared || !networkPermissionDeclared

    val summary: String
        get() {
            val issues = mutableListOf<String>()
            if (!amapKeyConfigured) {
                issues += "未配置高德地图 Key"
            }
            if (!xunfeiAppIdConfigured || !xunfeiApiKeyConfigured || !xunfeiApiSecretConfigured) {
                issues += "讯飞语音密钥未配置完整"
            }
            if (!xunfeiSdkAvailable || !xunfeiRecognizerAvailable) {
                issues += "未检测到完整讯飞语音 SDK"
            }
            if (!cameraPermissionDeclared) {
                issues += "缺少摄像头权限声明"
            }
            if (!microphonePermissionDeclared) {
                issues += "缺少录音权限声明"
            }
            if (!networkPermissionDeclared) {
                issues += "缺少网络权限声明"
            }
            return if (issues.isEmpty()) {
                "高德地图与讯飞语音配置检查通过，可进入完整出行链路。"
            } else {
                issues.joinToString(separator = "；")
            }
        }
}

object TravelSdkValidator {

    fun validate(context: Context): TravelSdkValidationResult {
        val amapKeyConfigured = readAmapKey(context).isNotBlank()
        val requestedPermissions = readRequestedPermissions(context)

        return TravelSdkValidationResult(
            amapKeyConfigured = amapKeyConfigured,
            xunfeiAppIdConfigured = BuildConfig.XUNFEI_APP_ID.isNotBlank(),
            xunfeiApiKeyConfigured = BuildConfig.XUNFEI_API_KEY.isNotBlank(),
            xunfeiApiSecretConfigured = BuildConfig.XUNFEI_API_SECRET.isNotBlank(),
            xunfeiSdkAvailable = isClassAvailable("com.iflytek.cloud.SpeechUtility"),
            xunfeiRecognizerAvailable = isClassAvailable("com.iflytek.cloud.SpeechRecognizer"),
            microphonePermissionDeclared = android.Manifest.permission.RECORD_AUDIO in requestedPermissions,
            cameraPermissionDeclared = android.Manifest.permission.CAMERA in requestedPermissions,
            networkPermissionDeclared = android.Manifest.permission.INTERNET in requestedPermissions
        )
    }

    private fun readAmapKey(context: Context): String {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        return appInfo.metaData?.getString("com.amap.api.v2.apikey").orEmpty()
    }

    private fun readRequestedPermissions(context: Context): Set<String> {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
        }
        return packageInfo.requestedPermissions?.toSet().orEmpty()
    }

    private fun isClassAvailable(className: String): Boolean {
        return runCatching {
            Class.forName(className)
            true
        }.getOrDefault(false)
    }
}
