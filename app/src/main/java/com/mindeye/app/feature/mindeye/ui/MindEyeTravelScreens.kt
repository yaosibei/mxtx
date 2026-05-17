package com.mindeye.app.feature.mindeye.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 明心之眼 - 目的地询问页面。
 * 这里对应你的想法：点击“开始出行”后，先问用户要去哪里。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationAskScreen(
    onDestinationSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
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

            Text(
                text = "需要接入的能力：CameraX 摄像头、OCR 文字识别、物体识别、障碍风险判断。",
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = { onScanFinished(destination) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .semantics { contentDescription = "开始出行前环境检查" },
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("开始检查", style = MaterialTheme.typography.titleLarge)
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
    val needStaffAssist = destination == "高铁站" || destination == "机场" || destination == "医院"

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
