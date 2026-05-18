package com.mindeye.app.feature.mindeye.ui

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
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
import com.mindeye.app.feature.mindeye.helper.TravelStrategyHelper
import com.mindeye.app.feature.mindeye.navigation.AmapNavigationManager
import com.mindeye.app.feature.mindeye.speech.XunfeiSpeechManager
import com.mindeye.app.feature.mindeye.vision.VisionAnalysisHelper

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
    var destinationInput by rememberSaveable { mutableStateOf("") }
    var speechMessage by rememberSaveable { mutableStateOf("点击按钮后可通过语音输入目的地") }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceInputInternal(
                context = context,
                onListeningStateChanged = { speechMessage = it },
                onResult = { destination ->
                    destinationInput = destination
                }
            )
        } else {
            speechMessage = "未获得麦克风权限，无法进行语音输入，请在系统设置中开启录音权限。"
            XunfeiSpeechManager.speak("未获得麦克风权限，无法进行语音输入")
        }
    }

    fun startVoiceInput() {
        if (XunfeiSpeechManager.hasRecordAudioPermission(context)) {
            startVoiceInputInternal(
                context = context,
                onListeningStateChanged = { speechMessage = it },
                onResult = { destination ->
                    destinationInput = destination
                }
            )
        } else {
            speechMessage = "需要先授权麦克风权限，才能开始语音输入。"
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            XunfeiSpeechManager.stopListening()
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
            DestinationButton(Icons.Default.Home, "回家", "常用路线，一键回家", onDestinationSelected)
            DestinationButton(Icons.Default.Train, "高铁站", "人流较大，可提前联系工作人员或志愿者", onDestinationSelected)
            DestinationButton(Icons.Default.TravelExplore, "机场", "适合提前生成服务提醒", onDestinationSelected)
            DestinationButton(Icons.Default.LocalHospital, "医院", "适合提前规划陪同与无障碍入口", onDestinationSelected)
            DestinationButton(Icons.Default.Map, "社区服务站", "适合附近路线和志愿者协助", onDestinationSelected)
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
    var cameraStatusText by rememberSaveable { mutableStateOf("点击开始检查后将启用摄像头识别前方环境。") }
    var isCameraStarted by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            XunfeiSpeechManager.initXunfei(context)
            VisionAnalysisHelper.startCamera(context, lifecycleOwner, previewView.surfaceProvider)
            cameraStatusText = "摄像头已启动，请将手机朝向正前方。"
            isCameraStarted = true
        } else {
            cameraStatusText = "未获得摄像头权限，无法开始环境识别。"
            XunfeiSpeechManager.speak("未获得摄像头权限，无法开始环境识别")
        }
    }

    LaunchedEffect(Unit) {
        XunfeiSpeechManager.initXunfei(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            VisionAnalysisHelper.release()
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
                    text = "需要接入的能力：CameraX 摄像头、OCR 文字识别、物体识别、障碍风险判断。",
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
                        VisionAnalysisHelper.startCamera(context, lifecycleOwner, previewView.surfaceProvider)
                        cameraStatusText = "摄像头已启动，请保持手机稳定，系统正在持续识别。"
                        isCameraStarted = true
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
                Text(if (isCameraStarted) "正在检查..." else "开始检查", style = MaterialTheme.typography.titleLarge)
            }
            OutlinedButton(
                onClick = {
                    VisionAnalysisHelper.stopCamera()
                    onScanFinished(destination)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("检查完成，继续出行")
            }
        }
    }
}

/**
 * 明心之眼 - 个性化出行方案页面。
 * 真实版本由目的地 + 明心之眼扫描结果 + SenseFlow 状态 + 地图路线共同生成。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanScreen(
    destination: String,
    onStartNavigation: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val needStaffAssist = destination == "高铁站" || destination == "机场" || destination == "医院"
    val planText = remember(destination) { TravelStrategyHelper.generatePlan(destination) }

    LaunchedEffect(destination) {
        // TODO: Trae 新增逻辑
        XunfeiSpeechManager.initXunfei(context)
        XunfeiSpeechManager.speak("$planText。路线已规划完毕，接下来由导航助手为您带路")
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
            PlanCard("个性化建议", planText)
            PlanCard("出行前建议", "先使用明心之眼确认前方环境，离开室内后再开始完整路线导航。")
            PlanCard("提醒方式", "随境 SenseFlow 会根据室内/室外、安静/嘈杂自动切换语音和震动策略。")
            if (needStaffAssist) {
                PlanCard("提前服务", "$destination 人流和流程较复杂，建议提前联系工作人员、家属或志愿者。")
            }
            Button(
                onClick = { onStartNavigation(destination) },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("开始出行陪伴", style = MaterialTheme.typography.titleLarge)
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
 * 成员 4 后续在这里接地图 SDK、路线点、转弯提醒、偏航提醒。
 * 成员 2 后续在“看前方”按钮里接短时摄像头避障。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelNavigationScreen(
    destination: String,
    onQuickAsk: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var navigationStatus by rememberSaveable { mutableStateOf("正在准备导航...") }
    val elapsedSeconds by produceState(0L) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            value++
        }
    }
    val elapsedMin = elapsedSeconds / 60
    val elapsedSec = elapsedSeconds % 60

    LaunchedEffect(destination) {
        AmapNavigationManager.initNavi(context)
        AmapNavigationManager.startWalkNavi(destination) { status ->
            navigationStatus = status
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AmapNavigationManager.stopNavi()
            AmapNavigationManager.release()
        }
    }

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
            PlanCard("导航状态", navigationStatus)
            PlanCard("出行时长", String.format("%02d:%02d", elapsedMin, elapsedSec))
            PlanCard("路线提醒", "保持手机朝前，注意前方障碍物和路口。地图 SDK 接入后将提供精确转弯和偏航提醒。")
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

private fun startVoiceInputInternal(
    context: Context,
    onListeningStateChanged: (String) -> Unit,
    onResult: (String) -> Unit
) {
    XunfeiSpeechManager.initXunfei(context)
    onListeningStateChanged("正在请求语音识别，请根据提示说出目的地。")
    XunfeiSpeechManager.startListening { destination ->
        if (destination.isBlank()) {
            onListeningStateChanged("没有听清您的目的地，请点击按钮重试。")
            XunfeiSpeechManager.speak("没有听清您的目的地，请再试一次")
        } else {
            onResult(destination)
            onListeningStateChanged("识别成功：$destination。可点击确认前往，或再次语音输入。")
            XunfeiSpeechManager.speak(
                "为您识别到目的地是$destination，确认前往请双击屏幕，重新输入请长按"
            )
        }
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? {
    return when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
