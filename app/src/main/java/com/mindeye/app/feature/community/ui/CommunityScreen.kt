package com.mindeye.app.feature.community.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRoleSelection: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isVolunteer) {
        VolunteerHomeScreen(
            viewModel = viewModel,
            onNavigateToRoleSelection = onNavigateToRoleSelection
        )
    } else {
        HelpUserHomeScreen(
            viewModel = viewModel,
            onNavigateToRoleSelection = onNavigateToRoleSelection
        )
    }

    if (uiState.showAchievementDialog && uiState.newlyUnlockedBadge != null) {
        AchievementDialog(
            badge = uiState.newlyUnlockedBadge!!,
            onDismiss = { viewModel.dismissAchievementDialog() }
        )
    }

    if (uiState.showHelpHistory) {
        HelpHistoryDialog(
            history = uiState.helpHistory,
            onDismiss = { viewModel.toggleHelpHistory() }
        )
    }
}

@Composable
private fun AchievementDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏅 解锁新徽章！") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (badge.id) {
                        "first_help" -> "🌟"
                        "helper" -> "💪"
                        "expert" -> ""
                        else -> "⭐"
                    },
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(badge.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(badge.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("太棒了！")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpHistoryDialog(
    history: List<HelpRecord>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("服务历史 (${history.size})") },
        text = {
            if (history.isEmpty()) {
                Text("暂无服务记录", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("服务 ${record.requesterName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(record.createdAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(record.content, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("状态: ${record.status}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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