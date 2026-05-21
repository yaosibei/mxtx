package com.mindeye.app.feature.mindeye.ui

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.amap.api.maps.model.LatLng
import com.mindeye.app.feature.mindeye.helper.TravelStrategyHelper
import com.mindeye.app.feature.mindeye.navigation.AmapNavigationManager
import com.mindeye.app.feature.mindeye.navigation.TravelAmapNaviLauncher
import com.mindeye.app.feature.mindeye.speech.XunfeiSpeechManager
import com.mindeye.app.feature.mindeye.travel.data.TravelSdkValidator
import com.mindeye.app.feature.mindeye.vision.VisionAnalysisHelper
import com.mindeye.app.feature.senseflow.domain.SenseSceneType
import com.mindeye.app.feature.senseflow.service.SceneAnalysisService
import com.mindeye.app.core.location.LocationService

/**
 * 明心之眼 - 目的地询问页面。
 * 这里对应你的想法：点击“开始出行”后，先问用户要去哪里。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DestinationAskScreen(
    onDestinationSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sdkValidation = remember(context) { TravelSdkValidator.validate(context) }
    var isScreenActive by remember { mutableStateOf(true) }
    var destinationInput by rememberSaveable { mutableStateOf("") }
    var pendingDestination by rememberSaveable { mutableStateOf("") }
    var speechMessage by rememberSaveable { mutableStateOf("正在准备出行语音助手。") }
    var hasAutoPrompted by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        XunfeiSpeechManager.stopListening()
        XunfeiSpeechManager.stopSpeaking()
        onNavigateBack()
    }

    fun listenForDestination() {
        speechMessage = "语音助手已唤醒，请直接说出目的地。"
        XunfeiSpeechManager.listenOnce { result ->
            if (!isScreenActive) return@listenOnce
            val normalizedDestination = normalizeTravelDestination(result)
            if (normalizedDestination.isBlank()) {
                speechMessage = "没有听清您的目的地，正在重新询问。"
                XunfeiSpeechManager.speak("没有听清您的目的地，请再说一次你要去哪里") {
                    if (!isScreenActive) return@speak
                    listenForDestination()
                }
            } else {
                pendingDestination = normalizedDestination
                destinationInput = normalizedDestination
                speechMessage = "已识别到目的地：$normalizedDestination。请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak(
                    "您要去的是$normalizedDestination。说确认出发继续，或说重新输入进行纠错，也可以直接说新的目的地"
                ) {
                    if (!isScreenActive) return@speak
                    listenForDestinationConfirmation(
                        currentDestination = normalizedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            if (!isScreenActive) return@listenForDestinationConfirmation
                            destinationInput = confirmedDestination
                            pendingDestination = confirmedDestination
                            speechMessage = "已确认目的地：$confirmedDestination，正在进入环境检测。"
                            XunfeiSpeechManager.speak("已确认目的地$confirmedDestination，现在开始环境检测") {
                                if (isScreenActive) {
                                    onDestinationSelected(confirmedDestination)
                                }
                            }
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            XunfeiSpeechManager.initXunfei(context)
            if (sdkValidation.hasHardBlocker) {
                speechMessage = sdkValidation.summary
                XunfeiSpeechManager.speak(sdkValidation.summary)
            } else {
                val validationHint = if (sdkValidation.canUseFullXunfeiVoiceChain) {
                    "讯飞语音已接管出行交互。"
                } else {
                    "讯飞 SDK 或密钥尚未完整生效，当前将使用系统语音能力兜底。"
                }
                speechMessage = "$validationHint 你要去哪里？"
                XunfeiSpeechManager.speak("你要去哪里？") {
                    listenForDestination()
                }
            }
        } else {
            speechMessage = "未获得麦克风权限，无法进行语音输入，请在系统设置中开启录音权限。"
            XunfeiSpeechManager.speak("未获得麦克风权限，无法进行语音输入")
        }
    }

    fun beginVoiceFlow() {
        XunfeiSpeechManager.initXunfei(context)
        if (sdkValidation.hasHardBlocker) {
            speechMessage = sdkValidation.summary
            XunfeiSpeechManager.speak(sdkValidation.summary)
        } else {
            val validationHint = if (sdkValidation.canUseFullXunfeiVoiceChain) {
                "讯飞语音已接管出行交互。"
            } else {
                "讯飞 SDK 或密钥尚未完整生效，当前将使用系统语音能力兜底。"
            }
            speechMessage = "$validationHint 你要去哪里？"
            XunfeiSpeechManager.speak("你要去哪里？") {
                listenForDestination()
            }
        }
    }

    fun startVoiceInput() {
        if (XunfeiSpeechManager.hasRecordAudioPermission(context)) {
            beginVoiceFlow()
        } else {
            speechMessage = "需要先授权麦克风权限，才能开始语音输入。"
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        XunfeiSpeechManager.initXunfei(context)
        if (!hasAutoPrompted) {
            hasAutoPrompted = true
            startVoiceInput()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isScreenActive = false
            XunfeiSpeechManager.stopListening()
            XunfeiSpeechManager.stopSpeaking()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("你要去哪里？") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "明心之眼会先确认目的地，再检查周围环境，最后生成个性化出行方案。",
                style = MaterialTheme.typography.bodyLarge
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("配置校验", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(sdkValidation.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                text = speechMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = destinationInput,
                onValueChange = { destinationInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("请输入或确认目的地") }
            )
            Button(
                onClick = { startVoiceInput() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .semantics { contentDescription = "语音输入目的地" },
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("语音输入目的地")
            }
            if (destinationInput.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onDoubleClick = { onDestinationSelected(destinationInput.trim()) },
                            onLongClick = { startVoiceInput() }
                        )
                        .semantics {
                            contentDescription = "当前目的地$destinationInput，双击确认前往，长按重新输入"
                        },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("当前识别结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(destinationInput, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                OutlinedButton(
                    onClick = { onDestinationSelected(destinationInput.trim()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("确认前往")
                }
            }
            DestinationButton(Icons.Default.Home, "回家", "常用路线，一键回家") { selectedDestination ->
                pendingDestination = selectedDestination
                destinationInput = selectedDestination
                speechMessage = "已选择$selectedDestination，请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak("已为您选择$selectedDestination。说确认出发继续，或说重新输入") {
                    listenForDestinationConfirmation(
                        currentDestination = selectedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            onDestinationSelected(confirmedDestination)
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
            DestinationButton(Icons.Default.Train, "高铁站", "人流较大，可提前联系工作人员或志愿者") { selectedDestination ->
                pendingDestination = selectedDestination
                destinationInput = selectedDestination
                speechMessage = "已选择$selectedDestination，请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak("已为您选择$selectedDestination。说确认出发继续，或说重新输入") {
                    listenForDestinationConfirmation(
                        currentDestination = selectedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            onDestinationSelected(confirmedDestination)
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
            DestinationButton(Icons.Default.TravelExplore, "机场", "适合提前生成服务提醒") { selectedDestination ->
                pendingDestination = selectedDestination
                destinationInput = selectedDestination
                speechMessage = "已选择$selectedDestination，请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak("已为您选择$selectedDestination。说确认出发继续，或说重新输入") {
                    listenForDestinationConfirmation(
                        currentDestination = selectedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            onDestinationSelected(confirmedDestination)
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
            DestinationButton(Icons.Default.LocalHospital, "医院", "适合提前规划陪同与无障碍入口") { selectedDestination ->
                pendingDestination = selectedDestination
                destinationInput = selectedDestination
                speechMessage = "已选择$selectedDestination，请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak("已为您选择$selectedDestination。说确认出发继续，或说重新输入") {
                    listenForDestinationConfirmation(
                        currentDestination = selectedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            onDestinationSelected(confirmedDestination)
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
            DestinationButton(Icons.Default.Map, "社区服务站", "适合附近路线和志愿者协助") { selectedDestination ->
                pendingDestination = selectedDestination
                destinationInput = selectedDestination
                speechMessage = "已选择$selectedDestination，请说确认出发，或说重新输入进行纠错。"
                XunfeiSpeechManager.speak("已为您选择$selectedDestination。说确认出发继续，或说重新输入") {
                    listenForDestinationConfirmation(
                        currentDestination = selectedDestination,
                        onStateChanged = { speechMessage = it },
                        onDestinationUpdated = {
                            pendingDestination = it
                            destinationInput = it
                        },
                        onConfirmed = { confirmedDestination ->
                            onDestinationSelected(confirmedDestination)
                        },
                        onRetryInput = { listenForDestination() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onDestinationSelected: (String) -> Unit
) {
    Button(
        onClick = { onDestinationSelected(title) },
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .semantics { contentDescription = "选择目的地：$title，$subtitle" },
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * 明心之眼 - 出行前环境检查页面。
 * 真实版本应在这里接入 CameraX、OCR、物体识别；当前保留清晰流程骨架，方便成员 2 接代码。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreTripScanScreen(
    destination: String,
    onScanFinished: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { androidx.camera.view.PreviewView(context) }
    var cameraStatusText by rememberSaveable { mutableStateOf("正在准备环境检测。") }
    var isCameraStarted by rememberSaveable { mutableStateOf(false) }
    var isScanCompleted by rememberSaveable { mutableStateOf(false) }
    var hasAutoRequestedCamera by rememberSaveable { mutableStateOf(false) }
    var hasSubmittedResult by rememberSaveable { mutableStateOf(false) }
    var remainingSeconds by rememberSaveable { mutableStateOf(PRE_TRIP_SCAN_DURATION_SECONDS) }
    var cameraStartToken by rememberSaveable { mutableStateOf(0) }

    // 场景分析服务，与摄像头检测并行运行
    val sceneService = remember { SceneAnalysisService(context, lifecycleOwner, LocationService(context)) }
    var currentSceneName by remember { mutableStateOf("等待分析...") }

    fun startPreTripScan() {
        XunfeiSpeechManager.initXunfei(context)
        cameraStatusText = "环境检测已启动，请将手机朝向正前方，系统会先完成障碍识别再进入导航。"
        isCameraStarted = true
        isScanCompleted = false
        hasSubmittedResult = false
        remainingSeconds = PRE_TRIP_SCAN_DURATION_SECONDS
        cameraStartToken += 1
        // 启动场景分析服务
        sceneService.start()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startPreTripScan()
        } else {
            cameraStatusText = "未获得摄像头权限，无法开始环境识别。"
            XunfeiSpeechManager.speak("未获得摄像头权限，无法开始环境识别")
        }
    }

    LaunchedEffect(cameraStartToken) {
        if (cameraStartToken > 0 && isCameraStarted && !isScanCompleted) {
            VisionAnalysisHelper.stopCamera()
            runCatching {
                VisionAnalysisHelper.startCamera(context, lifecycleOwner, previewView.surfaceProvider)
            }.onFailure {
                cameraStatusText = "摄像头启动失败，请重试或检查系统相机权限。"
                XunfeiSpeechManager.speak("摄像头启动失败，请重试")
            }
        }
    }

    LaunchedEffect(Unit) {
        XunfeiSpeechManager.initXunfei(context)
        if (!hasAutoRequestedCamera) {
            hasAutoRequestedCamera = true
            // 异步播报语音（speak 内部是异步的），不阻塞后续流程
            XunfeiSpeechManager.speak("目的地已确认，现在开始环境实时检测，请将手机朝向前方")
            // 立即检查权限并启动检测，不依赖 TTS 回调
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startPreTripScan()
            } else {
                cameraStatusText = "需要摄像头权限才能继续环境检测。"
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LaunchedEffect(isCameraStarted, isScanCompleted) {
        if (isCameraStarted && !isScanCompleted) {
            for (second in PRE_TRIP_SCAN_DURATION_SECONDS downTo 1) {
                remainingSeconds = second
                kotlinx.coroutines.delay(1000)
            }
            if (!hasSubmittedResult) {
                hasSubmittedResult = true
                isScanCompleted = true
                VisionAnalysisHelper.stopCamera()
                sceneService.stop()
                cameraStatusText = "环境检测完成，系统已完成障碍物识别与风险预警，正在生成出行方案。"
                XunfeiSpeechManager.speak("环境检测完成，如发现风险系统已在检测过程中播报。现在为您生成出行方案")
                onScanFinished(destination)
            }
        }
    }

    // 监听场景分析结果，实时播报场景变化
    LaunchedEffect(sceneService) {
        snapshotFlow { sceneService.getCurrentState() }.collect { state ->
            currentSceneName = when (state.sceneType) {
                SenseSceneType.INDOOR_QUIET -> "安静室内"
                SenseSceneType.INDOOR_NOISY -> "嘈杂室内"
                SenseSceneType.OUTDOOR_QUIET -> "安静户外"
                SenseSceneType.OUTDOOR_NOISY -> "嘈杂户外"
                SenseSceneType.SOCIAL -> "社交场景"
                SenseSceneType.TRAFFIC -> "交通环境"
                SenseSceneType.UNKNOWN -> "未知场景"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            VisionAnalysisHelper.release()
            sceneService.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("出行前检查") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("目的地：$destination", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("请将手机朝向正前方，明心之眼将帮你查看出口、楼梯、车辆、人群和障碍物。")
                    if (isCameraStarted && currentSceneName != "等待分析...") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("当前环境: $currentSceneName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (isCameraStarted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { previewView },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            } else {
                Text(
                    text = "系统将自动开启 CameraX 实时检测，先完成障碍识别和风险预警，再继续导航。",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = cameraStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        startPreTripScan()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .semantics { contentDescription = "开始出行前环境检查" },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(
                    if (isCameraStarted && !isScanCompleted) "正在检查... $remainingSeconds 秒" else "重新开始检查",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            OutlinedButton(
                onClick = {
                    VisionAnalysisHelper.stopCamera()
                    onScanFinished(destination)
                },
                enabled = isScanCompleted,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(if (isScanCompleted) "检查完成，继续出行" else "检测完成后自动继续")
            }
        }
    }
}

/**
 * 明心之眼 - 个性化出行方案页面。
 * 由目的地 + 明心之眼扫描结果 + SenseFlow 状态 + 地图路线共同生成。
 * 在此页面启动 SceneAnalysisService，为后续出行中实时分析做准备。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanScreen(
    destination: String,
    onStartNavigation: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sdkValidation = remember(context) { TravelSdkValidator.validate(context) }
    val needStaffAssist = destination == "高铁站" || destination == "机场" || destination == "医院"
    val planText = remember(destination) { TravelStrategyHelper.generatePlan(destination) }
    val hnustCenterPoint = remember { LatLng(27.904, 112.918) }
    var hasAutoStartedNavigation by rememberSaveable { mutableStateOf(false) }
    var mapPreviewStatus by rememberSaveable { mutableStateOf("正在加载高德地图预览。") }
    val resolvedMapPreviewStatus = if (sdkValidation.canUseAmapNavigation) {
        mapPreviewStatus
    } else {
        "高德地图 Key 未生效，当前无法渲染地图预览。"
    }

    // 在此页面初始化场景分析服务，为出行中实时分析做准备
    val sceneService = remember { SceneAnalysisService(context, lifecycleOwner, LocationService(context)) }
    var currentSceneName by remember { mutableStateOf("等待分析...") }
    var currentFeedbackStrategy by remember { mutableStateOf("语音为主") }

    LaunchedEffect(Unit) {
        sceneService.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            // 出行方案页面退出时不立即停止服务，由导航页面接管
        }
    }

    // 监听场景状态用于展示
    LaunchedEffect(sceneService) {
        snapshotFlow { sceneService.getCurrentState() }.collect { state ->
            currentSceneName = when (state.sceneType) {
                SenseSceneType.INDOOR_QUIET -> "安静室内"
                SenseSceneType.INDOOR_NOISY -> "嘈杂室内"
                SenseSceneType.OUTDOOR_QUIET -> "安静户外"
                SenseSceneType.OUTDOOR_NOISY -> "嘈杂户外"
                SenseSceneType.SOCIAL -> "社交场景"
                SenseSceneType.TRAFFIC -> "交通环境"
                SenseSceneType.UNKNOWN -> "未知场景"
            }
            currentFeedbackStrategy = when (state.feedbackStrategy) {
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.VOICE_MAIN -> "语音为主"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.VIBRATION_MAIN -> "震动为主"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION -> "短语音+震动"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.SILENT_SCREEN -> "静默模式"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.EMERGENCY_INTERRUPT -> "紧急中断"
            }
        }
    }

    LaunchedEffect(destination) {
        XunfeiSpeechManager.initXunfei(context)
        if (!hasAutoStartedNavigation) {
            hasAutoStartedNavigation = true
            val voicePlanText = if (sdkValidation.canUseAmapNavigation) {
                "$planText。随境 SenseFlow 已在后台运行，即将启动高德无障碍导航。"
            } else {
                "$planText。当前未完成高德地图 Key 配置，暂时无法启动原生导航。"
            }
            XunfeiSpeechManager.speak(voicePlanText) {
                if (sdkValidation.canUseAmapNavigation) {
                    onStartNavigation(destination)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个性化出行方案") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlanCard("目的地", destination)
            if (sdkValidation.canUseAmapNavigation) {
                AmapPreviewMap(
                    centerPoint = hnustCenterPoint,
                    title = "湖南科技大学",
                    subtitle = "地图预览已加载，可用于确认高德地图渲染正常。",
                    onMapStatusChanged = { mapPreviewStatus = it }
                )
            }
            PlanCard("地图状态", resolvedMapPreviewStatus)
            PlanCard("个性化建议", planText)
            PlanCard("出行前建议", "先使用明心之眼确认前方环境，离开室内后再开始完整路线导航。")

            // SenseFlow 场景分析实时状态展示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("随境 SenseFlow 状态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("当前场景: $currentSceneName", style = MaterialTheme.typography.bodyLarge)
                    Text("反馈策略: $currentFeedbackStrategy", style = MaterialTheme.typography.bodyMedium)
                }
            }

            PlanCard("提醒方式", "随境 SenseFlow 会根据室内/室外、安静/嘈杂自动切换语音和震动策略。")
            if (needStaffAssist) {
                PlanCard("提前服务", "$destination 人流和流程较复杂，建议提前联系工作人员、家属或志愿者。")
            }
            Button(
                onClick = { onStartNavigation(destination) },
                enabled = sdkValidation.canUseAmapNavigation,
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (sdkValidation.canUseAmapNavigation) "开始出行陪伴" else "高德导航配置未完成",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("重新检查")
            }
        }
    }
}

@Composable
private fun PlanCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * 明心之眼 - 出行中页面。
 * 接入 SceneAnalysisService 实时分析环境场景，自动切换语音/震动策略。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelNavigationScreen(
    destination: String,
    onQuickAsk: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sdkValidation = remember(context) { TravelSdkValidator.validate(context) }
    val hnustCenterPoint = remember { LatLng(27.904, 112.918) }
    val launchState by AmapNavigationManager.launchState.collectAsState()
    var hasRequestedNavigation by rememberSaveable { mutableStateOf(false) }
    var mapPreviewStatus by rememberSaveable { mutableStateOf("正在加载高德地图预览。") }

    // SenseFlow 场景分析服务
    val sceneService = remember { SceneAnalysisService(context, lifecycleOwner, LocationService(context)) }
    var currentSceneName by remember { mutableStateOf("等待分析...") }
    var currentFeedbackStrategy by remember { mutableStateOf("语音为主") }

    LaunchedEffect(destination) {
        XunfeiSpeechManager.initXunfei(context)
        AmapNavigationManager.resetLaunchState()
        if (!sdkValidation.canUseAmapNavigation) {
            hasRequestedNavigation = false
            XunfeiSpeechManager.speak("高德导航配置尚未完成，当前无法启动原生无障碍导航")
        } else if (!hasRequestedNavigation) {
            hasRequestedNavigation = true
            AmapNavigationManager.setPreferredStartPoint(
                com.amap.api.maps.model.LatLng(27.904, 112.918),
                "湖南科技大学"
            )
            XunfeiSpeechManager.speak("环境检测已完成，正在为您启动高德无障碍步行导航") {
                TravelAmapNaviLauncher.launch(context, destination)
            }
        }
    }

    // 启动场景分析服务，在出行中持续运行
    LaunchedEffect(Unit) {
        sceneService.start()
        sceneService.setPreviewView(androidx.camera.view.PreviewView(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            sceneService.stop()
            AmapNavigationManager.stopNavi()
            AmapNavigationManager.release()
        }
    }

    // 监听场景变化
    LaunchedEffect(sceneService) {
        snapshotFlow { sceneService.getCurrentState() }.collect { state ->
            currentSceneName = when (state.sceneType) {
                SenseSceneType.INDOOR_QUIET -> "安静室内"
                SenseSceneType.INDOOR_NOISY -> "嘈杂室内"
                SenseSceneType.OUTDOOR_QUIET -> "安静户外"
                SenseSceneType.OUTDOOR_NOISY -> "嘈杂户外"
                SenseSceneType.SOCIAL -> "社交场景"
                SenseSceneType.TRAFFIC -> "交通环境"
                SenseSceneType.UNKNOWN -> "未知场景"
            }
            currentFeedbackStrategy = when (state.feedbackStrategy) {
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.VOICE_MAIN -> "语音为主"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.VIBRATION_MAIN -> "震动为主"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.SHORT_VOICE_WITH_VIBRATION -> "短语音+震动"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.SILENT_SCREEN -> "静默模式"
                com.mindeye.app.feature.senseflow.domain.FeedbackStrategy.EMERGENCY_INTERRUPT -> "紧急中断"
            }
        }
    }

    val elapsedSeconds by produceState(0L) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            value++
        }
    }
    val elapsedMin = elapsedSeconds / 60
    val elapsedSec = elapsedSeconds % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("出行陪伴") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlanCard("正在前往", destination)
            if (sdkValidation.canUseAmapNavigation) {
                AmapPreviewMap(
                    centerPoint = hnustCenterPoint,
                    title = "湖南科技大学",
                    subtitle = "导航启动前的高德地图预览。",
                    onMapStatusChanged = { mapPreviewStatus = it }
                )
            }
            PlanCard(
                "地图状态",
                if (sdkValidation.canUseAmapNavigation) mapPreviewStatus
                else "高德地图 Key 未生效，无法显示导航地图。"
            )
            PlanCard(
                "导航状态",
                if (sdkValidation.canUseAmapNavigation) launchState.message
                else "未配置有效的高德地图 Key，无法启动原生无障碍导航。请先在 local.properties 中补充 amap.api.key，并在高德控制台完成包名与 SHA1 绑定。"
            )
            PlanCard("出行时长", String.format("%02d:%02d", elapsedMin, elapsedSec))

            // SenseFlow 实时场景分析展示
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("随境 SenseFlow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("当前场景: $currentSceneName", style = MaterialTheme.typography.bodyLarge)
                    Text("反馈策略: $currentFeedbackStrategy", style = MaterialTheme.typography.bodyMedium)
                }
            }

            PlanCard("路线提醒", "保持手机朝前，注意前方障碍物和路口。随境 SenseFlow 正在实时分析环境并自动切换提醒策略。")
            PlanCard("避障提醒", "出行中可按需调用明心之眼短时扫描前方环境。")
            Button(
                onClick = onQuickAsk,
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("看前方 / 问一下", style = MaterialTheme.typography.titleLarge)
            }
            OutlinedButton(
                onClick = {
                    if (sdkValidation.canUseAmapNavigation) {
                        AmapNavigationManager.resetLaunchState()
                        TravelAmapNaviLauncher.launch(context, destination)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("重新唤起高德导航")
            }
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("结束出行，返回首页", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * 明心之眼 - 问一下 / 快速识别页面。
 * 当前先接原 OCR 页面入口；后续成员 2 把 CameraX 实时识别接到这里。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAskScreen(
    onStartOcr: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("问一下") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("快速识别眼前内容", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击后可调用 OCR；后续扩展为文字、物体、障碍物统一识别。")
                }
            }
            Button(
                onClick = onStartOcr,
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("开始识别", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

private fun listenForDestinationConfirmation(
    currentDestination: String,
    onStateChanged: (String) -> Unit,
    onDestinationUpdated: (String) -> Unit,
    onConfirmed: (String) -> Unit,
    onRetryInput: () -> Unit
) {
    onStateChanged("请说确认出发，或说重新输入进行纠错。")
    XunfeiSpeechManager.listenOnce { result ->
        val command = result.trim()
        if (command.isBlank()) {
            onStateChanged("没有听清确认指令，请再说一次。")
            XunfeiSpeechManager.speak("没有听清，请说确认出发，或说重新输入") {
                listenForDestinationConfirmation(
                    currentDestination = currentDestination,
                    onStateChanged = onStateChanged,
                    onDestinationUpdated = onDestinationUpdated,
                    onConfirmed = onConfirmed,
                    onRetryInput = onRetryInput
                )
            }
        } else if (isTravelConfirmCommand(command)) {
            onConfirmed(currentDestination)
        } else if (isTravelRetryCommand(command)) {
            val correctedDestination = extractCorrectedDestination(command)
            if (correctedDestination.isNotBlank()) {
                onDestinationUpdated(correctedDestination)
                onStateChanged("已更新目的地为：$correctedDestination。请说确认出发继续。")
                XunfeiSpeechManager.speak("已将目的地更新为$correctedDestination。说确认出发继续") {
                    listenForDestinationConfirmation(
                        currentDestination = correctedDestination,
                        onStateChanged = onStateChanged,
                        onDestinationUpdated = onDestinationUpdated,
                        onConfirmed = onConfirmed,
                        onRetryInput = onRetryInput
                    )
                }
            } else {
                onStateChanged("请重新说出目的地。")
                XunfeiSpeechManager.speak("好的，请重新说出您的目的地") {
                    onRetryInput()
                }
            }
        } else {
            val correctedDestination = normalizeTravelDestination(command)
            onDestinationUpdated(correctedDestination)
            onStateChanged("已将目的地更新为：$correctedDestination。请说确认出发继续。")
            XunfeiSpeechManager.speak(
                "已将目的地更新为$correctedDestination。说确认出发继续，或继续说新的目的地"
            ) {
                listenForDestinationConfirmation(
                    currentDestination = correctedDestination,
                    onStateChanged = onStateChanged,
                    onDestinationUpdated = onDestinationUpdated,
                    onConfirmed = onConfirmed,
                    onRetryInput = onRetryInput
                )
            }
        }
    }
}

private fun normalizeTravelDestination(rawText: String): String {
    return rawText
        .trim()
        .replace("我要去", "")
        .replace("我想去", "")
        .replace("带我去", "")
        .replace("帮我去", "")
        .replace("导航到", "")
        .replace("去一下", "")
        .removePrefix("去")
        .trim()
        .trim('，', '。', '！', '？', ',', '.', '!')
}

private fun isTravelConfirmCommand(command: String): Boolean {
    val normalized = command.trim()
    return listOf("确认", "确认出发", "出发", "开始导航", "开始出发", "就去这", "没错").any {
        normalized.contains(it)
    }
}

private fun isTravelRetryCommand(command: String): Boolean {
    val normalized = command.trim()
    return listOf("重新输入", "重新识别", "重说", "不对", "错了", "改一下", "改目的地").any {
        normalized.contains(it)
    }
}

private fun extractCorrectedDestination(command: String): String {
    val normalized = command
        .replace("重新输入", "")
        .replace("重新识别", "")
        .replace("重说", "")
        .replace("改一下", "")
        .replace("改目的地", "")
        .replace("不对", "")
        .replace("错了", "")
        .trim()
    return normalizeTravelDestination(normalized)
}

private const val PRE_TRIP_SCAN_DURATION_SECONDS = 8

private tailrec fun Context.findActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
