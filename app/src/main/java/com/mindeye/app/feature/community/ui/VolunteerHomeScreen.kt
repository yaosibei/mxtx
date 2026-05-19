package com.mindeye.app.feature.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mindeye.app.core.model.*
import com.mindeye.app.feature.community.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(
    viewModel: CommunityViewModel,
    onNavigateToRoleSelection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("志愿者中心", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    TextButton(onClick = onNavigateToRoleSelection, colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("切换为求助用户")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                VolunteerHeaderCard(uiState)
            }
            item {
                VolunteerStatsGrid(uiState.volunteerStats)
            }
            item {
                VolunteerLevelProgress(uiState)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.togglePendingOrders() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("查看接单队列")
                    }
                    OutlinedButton(
                        onClick = { viewModel.toggleVolunteerRanking() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("查看排行榜")
                    }
                }
            }
            item {
                VolunteerBadgesSection(uiState.badges)
            }
            item {
                VolunteerActionCards(
                    onHelpRequest = { viewModel.switchTab(CommunityTab.HELP_REQUESTS) },
                    onViewHistory = { viewModel.toggleHelpHistory() }
                )
            }
        }
    }

    VolunteerPendingOrdersDialog(
        show = uiState.showPendingOrders,
        orders = uiState.pendingOrders,
        onDismiss = { viewModel.togglePendingOrders() }
    )

    VolunteerRankingDialog(
        show = uiState.showVolunteerRanking,
        topVolunteers = uiState.topVolunteers,
        onDismiss = { viewModel.toggleVolunteerRanking() }
    )
}

@Composable
private fun VolunteerHeaderCard(uiState: CommunityUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        uiState.volunteerLevel.level.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                uiState.volunteerLevel.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "已完成 ${uiState.serviceCount} 次志愿服务",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VolunteerStatsGrid(stats: VolunteerStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("服务统计", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    value = stats.completedOrders.toString(),
                    label = "已完成",
                    color = MaterialTheme.colorScheme.primary
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    value = "${stats.totalHours}h",
                    label = "总时长",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    value = String.format("%.1f", stats.rating),
                    label = "评分",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Pending,
                    value = stats.pendingOrders.toString(),
                    label = "待处理",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier
            .background(
                color.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VolunteerLevelProgress(uiState: CommunityUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("等级进度", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            val currentLevel = uiState.volunteerLevel
            val nextLevel = VolunteerLevel.values().firstOrNull { it.level > currentLevel.level }

            if (nextLevel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lv.${currentLevel.level} ${currentLevel.label}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("→ Lv.${nextLevel.level} ${nextLevel.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                val progressValue = if (nextLevel.requiredCount > currentLevel.requiredCount) {
                        ((uiState.serviceCount - currentLevel.requiredCount).toFloat() /
                                (nextLevel.requiredCount - currentLevel.requiredCount)).coerceIn(0f, 1f)
                    } else 1f
                LinearProgressIndicator(
                    progress = progressValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "已完成 ${uiState.serviceCount}/${nextLevel.requiredCount} 次可升级",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("🏆 恭喜！你已达到最高等级！", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun VolunteerBadgesSection(badges: List<Badge>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("获得徽章", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${badges.size} 枚", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (badges.isEmpty()) {
                Text(
                    "完成志愿服务可获得徽章",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(badges) { badge ->
                        BadgeCard(badge = badge)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when (badge.id) {
                    "first_help" -> "🌟"
                    "helper" -> "💪"
                    "expert" -> "🏅"
                    else -> "⭐"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(badge.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(badge.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun VolunteerActionCards(
    onHelpRequest: () -> Unit,
    onViewHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Assignment,
            title = "接单帮助",
            description = "查看并接取求助订单",
            onClick = onHelpRequest,
            color = MaterialTheme.colorScheme.primary
        )
        ActionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.History,
            title = "服务历史",
            description = "查看志愿服务记录",
            onClick = onViewHistory,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolunteerPendingOrdersDialog(
    show: Boolean,
    orders: List<VolunteerOrderItem>,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("接单队列 (${orders.size})") },
            text = {
                if (orders.isEmpty()) {
                    Text("暂无待处理订单", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        orders.forEach { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(order.requesterName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(order.content, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(order.helpCategory, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        Text(order.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("关闭") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolunteerRankingDialog(
    show: Boolean,
    topVolunteers: List<com.mindeye.app.core.database.entity.VolunteerProfileEntity>,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("🏆 志愿者排行榜") },
            text = {
                if (topVolunteers.isEmpty()) {
                    Text("暂无排行榜数据", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        topVolunteers.forEachIndexed { index, profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(profile.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("服务 ${profile.serviceCount} 次", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("⭐ ${String.format("%.1f", profile.rating)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("关闭") }
            }
        )
    }
}