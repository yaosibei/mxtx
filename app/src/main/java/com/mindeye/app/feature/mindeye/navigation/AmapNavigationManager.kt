package com.mindeye.app.feature.mindeye.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.location.Location
import android.util.Log
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.core.ServiceSettings
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.mindeye.app.core.location.LocationService
import com.mindeye.app.feature.mindeye.speech.XunfeiSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AmapNavigationManager {

    private const val TAG = "AmapNavigationManager"

    private const val ENABLE_MOCK_LOCATION_FALLBACK = true

    private val mockStartPoint = LatLng(27.904, 112.918)
    private var appContext: Context? = null
    private var hostActivityRef: WeakReference<Activity>? = null
    private var scope: CoroutineScope? = null
    private var useMockStartPoint = false

    fun initNavi(context: Context) {
        appContext = context.applicationContext
        context.findActivity()?.let { activity ->
            hostActivityRef = WeakReference(activity)
        }

        // 高德 SDK 在调用任何地图/搜索/导航接口前都要求先完成隐私合规声明。
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        ServiceSettings.updatePrivacyShow(context, true, true)
        ServiceSettings.updatePrivacyAgree(context, true)

        if (scope == null) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }

        // TODO: Trae 新增逻辑
        // 高德 Key 请在 AndroidManifest.xml 中通过 manifestPlaceholders 注入：
        // <meta-data android:name="com.amap.api.v2.apikey" android:value="${AMAP_API_KEY}" />
    }

    fun startWalkNavi(destinationName: String, onStatusChanged: (String) -> Unit = {}) {
        val context = appContext
        val activity = hostActivityRef?.get()
        val currentScope = scope

        if (context == null || activity == null || currentScope == null) {
            val message = "导航页面上下文无效，请返回后重试。"
            Log.e(TAG, "startWalkNavi 调用失败，请先在 Activity Context 中执行 initNavi(context)")
            onStatusChanged(message)
            return
        }

        currentScope.launch {
            try {
                onStatusChanged("正在获取当前位置并解析目的地。")
                val startPoint = resolveStartPoint(context)
                val destinationCandidate = resolveDestination(context, destinationName, startPoint)

                if (destinationCandidate == null) {
                    val message = "没有找到“$destinationName”，请尝试输入更完整的楼名或地点名。"
                    Log.e(TAG, "目的地解析失败: $destinationName")
                    onStatusChanged(message)
                    XunfeiSpeechManager.speak(message)
                    return@launch
                }

                val distance = distanceInMeters(startPoint, destinationCandidate.latLng)
                Log.d(TAG, "起点与终点距离: ${distance}米")

                if (distance < 50f) {
                    val message = "你已经在${destinationCandidate.displayName}附近了（距离约${distance.toInt()}米），无需再开启步行导航。"
                    onStatusChanged(message)
                    XunfeiSpeechManager.speak(message)
                    return@launch
                }

                val params = AmapNaviParams(
                    Poi(getStartPointName(), startPoint, ""),
                    null,
                    Poi(
                        destinationCandidate.displayName,
                        destinationCandidate.latLng,
                        destinationCandidate.poiId
                    ),
                    AmapNaviType.WALK
                )

                // 导航阶段完全交给高德默认语音播报，避免与讯飞交互语音混播。
                params.setUseInnerVoice(true)
                onStatusChanged("已找到${destinationCandidate.displayName}，正在打开高德步行导航。")
                AmapNaviPage.getInstance().showRouteActivity(activity, params, null)
            } catch (e: Exception) {
                onStatusChanged("启动导航失败：${e.message ?: "未知错误"}")
                Log.e(TAG, "启动步行导航失败: ${e.message}", e)
            }
        }
    }

    fun stopNavi() {
        // 高德原生导航页自行管理导航生命周期，这里保留空实现，便于后续扩展。
    }

    fun release() {
        scope?.cancel()
        scope = null
        hostActivityRef = null
    }

    private suspend fun resolveStartPoint(context: Context): LatLng {
        val location = LocationService(context).getCurrentLocation()
        if (location != null) {
            useMockStartPoint = false
            return location.toLatLng()
        }

        if (ENABLE_MOCK_LOCATION_FALLBACK) {
            useMockStartPoint = true
            Log.w(TAG, "真实定位失败，回退到模拟起点")
            return mockStartPoint
        }

        throw IllegalStateException("真实定位失败，请确认定位权限和 GPS 状态")
    }

    private suspend fun resolveDestination(
        context: Context,
        destinationName: String,
        startPoint: LatLng
    ): DestinationCandidate? {
        val geocodeResult = geocodeDestination(context, destinationName)
        if (geocodeResult != null) {
            return geocodeResult
        }

        Log.w(TAG, "地理编码未命中，开始使用 POI 搜索兜底: $destinationName")
        return searchDestinationPoi(context, destinationName, startPoint)
    }

    private suspend fun geocodeDestination(
        context: Context,
        destinationName: String
    ): DestinationCandidate? {
        return suspendCoroutine { continuation ->
            val geocodeSearch = GeocodeSearch(context)
            geocodeSearch.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
                override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
                    if (rCode != AMapException.CODE_AMAP_SUCCESS) {
                        Log.e(TAG, "地理编码失败，错误码=$rCode")
                        continuation.resume(null)
                        return
                    }

                    val point = result
                        ?.geocodeAddressList
                        ?.firstOrNull()
                        ?.latLonPoint

                    continuation.resume(
                        point?.let {
                            DestinationCandidate(
                                displayName = destinationName,
                                latLng = LatLng(it.latitude, it.longitude)
                            )
                        }
                    )
                }

                override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) = Unit
            })

            val cityHint = if (useMockStartPoint) "湘潭" else ""
            geocodeSearch.getFromLocationNameAsyn(GeocodeQuery(destinationName, cityHint))
        }
    }

    private suspend fun searchDestinationPoi(
        context: Context,
        destinationName: String,
        startPoint: LatLng
    ): DestinationCandidate? {
        return suspendCoroutine { continuation ->
            val cityHint = if (useMockStartPoint) "湘潭" else ""
            val query = PoiSearch.Query(destinationName, "", cityHint).apply {
                pageSize = 10
                pageNum = 1
            }
            val poiSearch = PoiSearch(context, query)
            poiSearch.setBound(PoiSearch.SearchBound(startPoint.toLatLonPoint(), 5000))
            poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                    if (rCode != AMapException.CODE_AMAP_SUCCESS) {
                        Log.e(TAG, "POI 搜索失败，错误码=$rCode")
                        continuation.resume(null)
                        return
                    }

                    val bestPoi = result
                        ?.pois
                        ?.firstOrNull { it.latLonPoint != null }

                    continuation.resume(bestPoi?.toDestinationCandidate())
                }

                override fun onPoiItemSearched(item: PoiItem?, rCode: Int) = Unit
            })
            poiSearch.searchPOIAsyn()
        }
    }

    private fun getStartPointName(): String {
        return if (useMockStartPoint) {
            "湖南科技大学"
        } else {
            "我的位置"
        }
    }

    private fun Location.toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    private fun LatLng.toLatLonPoint(): LatLonPoint {
        return LatLonPoint(latitude, longitude)
    }

    private fun PoiItem.toDestinationCandidate(): DestinationCandidate? {
        val point = latLonPoint ?: return null
        return DestinationCandidate(
            displayName = title.takeIf { it.isNotBlank() } ?: snippet.takeIf { it.isNotBlank() } ?: "目标地点",
            latLng = LatLng(point.getLatitude(), point.getLongitude()),
            poiId = poiId.orEmpty()
        )
    }

    private fun distanceInMeters(start: LatLng, end: LatLng): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude,
            start.longitude,
            end.latitude,
            end.longitude,
            result
        )
        return result[0]
    }

    private tailrec fun Context.findActivity(): Activity? {
        return when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
    }

    private data class DestinationCandidate(
        val displayName: String,
        val latLng: LatLng,
        val poiId: String = ""
    )
}
