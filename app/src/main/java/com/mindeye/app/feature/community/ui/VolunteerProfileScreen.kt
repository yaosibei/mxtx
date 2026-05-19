package com.mindeye.app.feature.community.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindeye.app.feature.community.viewmodel.VolunteerLevel

data class VolunteerOrderRecord(
    val orderId: String,
    val requesterName: String,
    val content: String,
    val category: String,
    val status: String,
    val rating: Int,
    val createdAt: String,
    val completedAt: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerProfileScreen(
    serviceCount: Int,
    volunteerLevel: VolunteerLevel,
    badgeCount: Int,
    completedOrders: Int,
    pendingOrders: Int,
    rating: Float,
    totalHours: Float,
    orders: List<VolunteerOrderRecord>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("志愿者主页", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VolunteerHeader(
                    serviceCount = serviceCount,
                    level = volunteerLevel,
                    badgeCount = badgeCount
                )
            }

            item {
                VolunteerStatsGrid(
                    completedOrders = completedOrders,
                    pendingOrders = pendingOrders,
                    rating = rating,
                    totalHours = totalHours
                )
            }

            item {
                Text("接单历史", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("暂无接单记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("去社区互助接单帮助他人吧！", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(orders) { order ->
                    OrderRecordCard(order)
                }
            }
        }
    }
}

@Composable
private fun VolunteerHeader(serviceCount: Int, level: VolunteerLevel, badgeCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("志愿者", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(12.dp)) {
                Text(
                    "Lv.${level.level} ${level.label}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("已服务 $serviceCount 次 · $badgeCount 个徽章", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VolunteerStatsGrid(
    completedOrders: Int,
    pendingOrders: Int,
    rating: Float,
    totalHours: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "已完成",
                    value = "$completedOrders",
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                StatItem(
                    icon = Icons.Default.Pending,
                    label = "进行中",
                    value = "$pendingOrders",
                    color = Color(0xFFFF9800)
                )
            }
            item {
                StatItem(
                    icon = Icons.Default.Star,
                    label = "评分",
                    value = String.format("%.1f", rating),
                    color = Color(0xFFFFC107)
                )
            }
            item {
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "总时长",
                    value = "${totalHours}h",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OrderRecordCard(order: VolunteerOrderRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(order.requesterName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(order.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    when (order.status) {
                        "completed" -> "已完成"
                        "pending" -> "进行中"
                        else -> order.status
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (order.status == "completed") Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(order.content, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(order.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                if (order.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text("${order.rating}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFC107))
                    }
                }
            }
        }
    }
}
