package com.mindeye.app.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindeye.app.feature.common.ui.isTouchExplorationEnabled
import com.mindeye.app.feature.home.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMindEyeTravel: () -> Unit,
    onNavigateToQuickAsk: () -> Unit,
    onNavigateToEmergency: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToPsychology: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val talkBackEnabled = remember(context) { context.isTouchExplorationEnabled() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("明心同行", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 欢迎卡片
            WelcomeCard()

            Spacer(modifier = Modifier.height(24.dp))

            // SOS 紧急求助 - 最大最突出
            SosButton(onClick = onNavigateToEmergency)

            Spacer(modifier = Modifier.height(24.dp))

            // 开始出行
            LargeTravelButton(onClick = onNavigateToMindEyeTravel)

            Spacer(modifier = Modifier.height(24.dp))

            if (talkBackEnabled) {
                FeatureList(
                    onNavigateToCommunity = onNavigateToCommunity,
                    onNavigateToPsychology = onNavigateToPsychology,
                    onNavigateToSettings = onNavigateToSettings
                )
            } else {
                FeatureGrid(
                    onNavigateToCommunity = onNavigateToCommunity,
                    onNavigateToPsychology = onNavigateToPsychology,
                    onNavigateToSettings = onNavigateToSettings
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "温暖的视障出行互助平台",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "先看清环境，再规划路线；出行中持续陪伴，必要时一键求助。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SosButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .semantics { contentDescription = "SOS紧急求助，点击发送求助信息并拨打紧急电话" },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SOS 紧急求助",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "发送位置信息给紧急联系人",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun LargeTravelButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .semantics { contentDescription = "开始出行，进入明心之眼出行流程" },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsWalk,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "开始出行",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "出行检查 · 方案规划 · 实时陪伴",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun FeatureGrid(
    onNavigateToCommunity: () -> Unit,
    onNavigateToPsychology: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 第一行：社区互助、心理支持
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(
                icon = Icons.Default.People,
                title = "社区互助",
                description = "发布求助、志愿者接单",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToCommunity,
                backgroundColor = Color(0xFFE3F2FD),
                iconColor = Color(0xFF1565C0),
                textColor = Color(0xFF0D47A1)
            )
            FeatureCard(
                icon = Icons.Default.Psychology,
                title = "心理支持",
                description = "AI陪伴、温暖问答",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPsychology,
                backgroundColor = Color(0xFFF3E5F5),
                iconColor = Color(0xFF7B1FA2),
                textColor = Color(0xFF4A148C)
            )
        }

        // 第二行：长辈模式、系统设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureCard(
                icon = Icons.Default.AccessibilityNew,
                title = "长辈模式",
                description = "大字体、高对比、语音播报",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings,
                backgroundColor = Color(0xFFFFF3E0),
                iconColor = Color(0xFFE65100),
                textColor = Color(0xFFBF360C)
            )
            FeatureCard(
                icon = Icons.Default.Settings,
                title = "系统设置",
                description = "提醒方式、隐私设置",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings,
                backgroundColor = Color(0xFFE8F5E9),
                iconColor = Color(0xFF2E7D32),
                textColor = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
private fun FeatureList(
    onNavigateToCommunity: () -> Unit,
    onNavigateToPsychology: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FeatureListButton(
            icon = Icons.Default.People,
            title = "社区互助",
            description = "发布求助、志愿者接单",
            onClick = onNavigateToCommunity
        )
        FeatureListButton(
            icon = Icons.Default.Psychology,
            title = "心理支持",
            description = "AI陪伴、温暖问答",
            onClick = onNavigateToPsychology
        )
        FeatureListButton(
            icon = Icons.Default.Settings,
            title = "设置",
            description = "提醒方式、隐私与长辈模式",
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun FeatureListButton(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .semantics { contentDescription = "$title，$description" },
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(34.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color,
    iconColor: Color,
    textColor: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(120.dp)
            .semantics { contentDescription = "$title，$description" },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
