package com.mindeye.app.feature.mindeye.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AmapNavigationManager {

    private const val TAG = "AmapNavigationManager"

    private const val ENABLE_MOCK_LOCATION_FALLBACK = true

    private val hnustCenterPoint = LatLng(27.904, 112.918)
    private const val hnustCenterName = "湖南科技大学"

    private var preferredStartPoint: LatLng = hnustCenterPoint
    private var preferredStartPointName: String = hnustCenterName
    private var appContext: Context? = null
    private var hostActivityRef: WeakReference<Activity>? = null
    private var scope: CoroutineScope? = null
    private var launchTimeoutJob: Job? = null
    private var useMockStartPoint = false
    private var internalRoutePageOpened = false
    private var fallbackTriggered = false
    private var currentDestinationCandidate: DestinationCandidate? = null
    private var currentStartPoint: LatLng? = null

    private val _launchState = MutableStateFlow(
        TravelNaviLaunchState(
            stage = TravelNaviLaunchStage.IDLE,
            message = "等待启动导航。"
        )
    )
    val launchState: StateFlow<TravelNaviLaunchState> = _launchState.asStateFlow()

    fun setPreferredStartPoint(latLng: LatLng, name: String = hnustCenterName) {
        preferredStartPoint = latLng
        preferredStartPointName = name
    }

    fun resetLaunchState() {
        launchTimeoutJob?.cancel()
        internalRoutePageOpened = false
        fallbackTriggered = false
        currentDestinationCandidate = null
        currentStartPoint = null
        _launchState.value = TravelNaviLaunchState(
            stage = TravelNaviLaunchStage.IDLE,
            message = "等待启动导航。"
        )
    }

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

        val apiKey = readAmapApiKey(context)
        if (apiKey.isBlank()) {
            Log.e(TAG, "未检测到高德 API Key，请检查 local.properties 的 amap.api.key 配置与 Manifest 注入")
        }

        if (scope == null) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }

        // TODO: Trae 新增逻辑
        // 高德 Key 请在 AndroidManifest.xml 中通过 manifestPlaceholders 注入：
        // <meta-data android:name="com.amap.api.v2.apikey" android:value="${AMAP_API_KEY}" />
    }

    fun launchFromActivity(activity: Activity, destinationName: String) {
        initNavi(activity)
        hostActivityRef = WeakReference(activity)
        startWalkNavi(destinationName)
    }

    fun onInternalRoutePageOpened() {
        internalRoutePageOpened = true
        launchTimeoutJob?.cancel()
        updateLaunchState(
            TravelNaviLaunchStage.INTERNAL_ROUTE_PAGE_OPENED,
            "高德无障碍导航页已打开，正在准备步行导航。"
        )
    }

    fun onInternalRoutePageClosed() {
        if (_launchState.value.stage == TravelNaviLaunchStage.INTERNAL_ROUTE_PAGE_OPENED ||
            _launchState.value.stage == TravelNaviLaunchStage.INTERNAL_NAVI_STARTED
        ) {
            updateLaunchState(TravelNaviLaunchStage.IDLE, "已退出高德导航页。")
        }
    }

    fun startWalkNavi(destinationName: String) {
        val context = appContext
        val activity = hostActivityRef?.get()
        val currentScope = scope

        if (context == null || activity == null || currentScope == null) {
            val message = "导航页面上下文无效，请返回后重试。"
            Log.e(TAG, "startWalkNavi 调用失败，请先在 Activity Context 中执行 initNavi(context)")
            updateLaunchState(TravelNaviLaunchStage.FAILED, message)
            return
        }

        if (readAmapApiKey(context).isBlank()) {
            val message = "高德地图 Key 未配置或未生效，无法启动导航。请检查包名与 SHA1 是否匹配。"
            updateLaunchState(TravelNaviLaunchStage.FAILED, message)
            XunfeiSpeechManager.speak(message)
            return
        }

        val trimmedDestination = destinationName.trim()
        if (trimmedDestination.isBlank()) {
            val message = "目的地为空，无法启动导航。"
            updateLaunchState(TravelNaviLaunchStage.FAILED, message)
            XunfeiSpeechManager.speak(message)
            return
        }

        internalRoutePageOpened = false
        fallbackTriggered = false
        currentDestinationCandidate = null
        currentStartPoint = null

        currentScope.launch {
            try {
                updateLaunchState(TravelNaviLaunchStage.PREPARING, "正在获取当前位置。")
                val startPoint = resolveStartPoint(context)
                currentStartPoint = startPoint
                updateLaunchState(TravelNaviLaunchStage.RESOLVING_DESTINATION, "正在解析目的地。")
                val destinationCandidate = resolveDestination(context, trimmedDestination, startPoint)

                if (destinationCandidate == null) {
                    val message = "没有找到“$trimmedDestination”，请尝试输入更完整的楼名或地点名。"
                    Log.e(TAG, "目的地解析失败: $trimmedDestination")
                    updateLaunchState(TravelNaviLaunchStage.FAILED, message)
                    XunfeiSpeechManager.speak(message)
                    return@launch
                }
                currentDestinationCandidate = destinationCandidate

                val distance = distanceInMeters(startPoint, destinationCandidate.latLng)
                Log.d(TAG, "起点与终点距离: ${distance}米")

                if (distance < 50f) {
                    val message = "你已经在${destinationCandidate.displayName}附近了（距离约${distance.toInt()}米），无需再开启步行导航。"
                    updateLaunchState(TravelNaviLaunchStage.FAILED, message)
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

                // 在切到高德原生导航前主动停止讯飞播报，避免两套语音重叠。
                XunfeiSpeechManager.stopSpeaking()

                // 导航阶段完全交给高德默认语音播报，保证转弯提示连续稳定。
                params.setUseInnerVoice(true)
                updateLaunchState(
                    TravelNaviLaunchStage.INTERNAL_LAUNCHING,
                    "已找到${destinationCandidate.displayName}，正在请求打开高德步行导航。"
                )
                scheduleLaunchTimeout()
                AmapNaviPage.getInstance().showRouteActivity(
                    activity,
                    params,
                    createNaviInfoCallback(),
                    TravelAmapRouteActivity::class.java
                )
            } catch (e: Exception) {
                Log.e(TAG, "启动步行导航失败: ${e.message}", e)
                tryLaunchExternalFallback("内置高德导航启动失败：${e.message ?: "未知错误"}")
            }
        }
    }

    fun stopNavi() {
        launchTimeoutJob?.cancel()
    }

    fun release() {
        launchTimeoutJob?.cancel()
        scope?.cancel()
        scope = null
        hostActivityRef = null
        resetLaunchState()
    }

    private fun createNaviInfoCallback(): INaviInfoCallback {
        return object : INaviInfoCallback {
            override fun onInitNaviFailure() {
                Log.e(TAG, "高德导航初始化失败")
                tryLaunchExternalFallback("高德导航初始化失败。")
            }

            override fun onGetNavigationText(text: String?) {
                if (!text.isNullOrBlank()) {
                    Log.d(TAG, "高德导航播报：$text")
                }
            }

            override fun onLocationChange(location: AMapNaviLocation?) = Unit

            override fun onArriveDestination(isArriveDestination: Boolean) {
                if (isArriveDestination) {
                    updateLaunchState(TravelNaviLaunchStage.INTERNAL_NAVI_STARTED, "已接近目的地。")
                }
            }

            override fun onStartNavi(type: Int) {
                launchTimeoutJob?.cancel()
                updateLaunchState(
                    TravelNaviLaunchStage.INTERNAL_NAVI_STARTED,
                    "高德步行导航已开始。"
                )
            }

            override fun onCalculateRouteSuccess(routeIds: IntArray?) {
                updateLaunchState(
                    TravelNaviLaunchStage.ROUTE_READY,
                    "路径规划成功，正在进入高德导航页。"
                )
            }

            override fun onCalculateRouteFailure(errorCode: Int) {
                Log.e(TAG, "高德路径规划失败，错误码=$errorCode")
                tryLaunchExternalFallback("高德路径规划失败，错误码 $errorCode。")
            }

            override fun onStopSpeaking() = Unit

            override fun onReCalculateRoute(type: Int) {
                updateLaunchState(
                    TravelNaviLaunchStage.ROUTE_READY,
                    "路线已重新规划。"
                )
            }

            override fun onExitPage(type: Int) {
                updateLaunchState(TravelNaviLaunchStage.IDLE, "已退出高德导航页。")
            }

            override fun onStrategyChanged(strategy: Int) = Unit

            override fun onArrivedWayPoint(wayID: Int) = Unit

            override fun onMapTypeChanged(type: Int) = Unit

            override fun onNaviDirectionChanged(naviDirection: Int) = Unit

            override fun onDayAndNightModeChanged(dayAndNightMode: Int) = Unit

            override fun onBroadcastModeChanged(mode: Int) = Unit

            override fun onScaleAutoChanged(auto: Boolean) = Unit

            override fun getCustomMiddleView() = null

            override fun getCustomNaviView() = null

            override fun getCustomNaviBottomView() = null
        }
    }

    private fun scheduleLaunchTimeout() {
        launchTimeoutJob?.cancel()
        val currentScope = scope ?: return
        launchTimeoutJob = currentScope.launch {
            delay(5000)
            if (!internalRoutePageOpened && !fallbackTriggered) {
                Log.e(TAG, "内置高德导航启动超时，尝试降级到外部高德地图 App")
                tryLaunchExternalFallback("内置高德导航页启动超时。")
            }
        }
    }

    private fun tryLaunchExternalFallback(reason: String) {
        if (fallbackTriggered) return
        fallbackTriggered = true
        launchTimeoutJob?.cancel()

        val context = hostActivityRef?.get() ?: appContext
        val destinationCandidate = currentDestinationCandidate
        val startPoint = currentStartPoint

        if (context == null || destinationCandidate == null || startPoint == null) {
            val message = "$reason 当前无法切换外部高德导航。"
            updateLaunchState(TravelNaviLaunchStage.FAILED, message)
            XunfeiSpeechManager.speak("高德导航启动失败，请检查定位权限和网络连接")
            return
        }

        val launched = TravelAmapExternalFallback.launch(
            context = context,
            startName = getStartPointName(),
            startPoint = startPoint,
            destinationName = destinationCandidate.displayName,
            destinationPoint = destinationCandidate.latLng
        )

        if (launched) {
            updateLaunchState(
                TravelNaviLaunchStage.EXTERNAL_APP_LAUNCHED,
                "内置高德导航未成功启动，已切换到高德地图 App 继续导航。"
            )
            XunfeiSpeechManager.speak("已切换到高德地图应用继续为您导航")
        } else {
            val message = "$reason 请确认手机已安装高德地图，或稍后重试。"
            updateLaunchState(TravelNaviLaunchStage.FAILED, message)
            XunfeiSpeechManager.speak("高德导航启动失败，请确认已安装高德地图应用")
        }
    }

    private fun updateLaunchState(stage: TravelNaviLaunchStage, message: String) {
        _launchState.value = TravelNaviLaunchState(stage = stage, message = message)
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
            return preferredStartPoint
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
            preferredStartPointName
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

    private fun readAmapApiKey(context: Context): String {
        return runCatching {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.amap.api.v2.apikey").orEmpty()
        }.getOrDefault("")
    }

    data class TravelNaviLaunchState(
        val stage: TravelNaviLaunchStage,
        val message: String
    )

    enum class TravelNaviLaunchStage {
        IDLE,
        PREPARING,
        RESOLVING_DESTINATION,
        INTERNAL_LAUNCHING,
        ROUTE_READY,
        INTERNAL_ROUTE_PAGE_OPENED,
        INTERNAL_NAVI_STARTED,
        EXTERNAL_APP_LAUNCHED,
        FAILED
    }

    private data class DestinationCandidate(
        val displayName: String,
        val latLng: LatLng,
        val poiId: String = ""
    )
}
