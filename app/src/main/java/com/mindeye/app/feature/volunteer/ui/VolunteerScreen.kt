package com.mindeye.app.feature.volunteer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindeye.app.core.model.HelpRequest
import com.mindeye.app.core.model.HelpRequestStatus
import com.mindeye.app.feature.volunteer.viewmodel.VolunteerTab
import com.mindeye.app.feature.volunteer.viewmodel.VolunteerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerScreen(
    viewModel: VolunteerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingCount = uiState.value.pendingRequests.size
    
    // 每次界面组合时都刷新数据
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    // 显示待接单数量调试信息
    LaunchedEffect(pendingCount) {
        if (pendingCount > 0) {
            snackbarHostState.showSnackbar("数据库中有 $pendingCount 条待接单求助")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("志愿者中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // 调试信息：显示当前数据量
            Text(
                text = "调试: 待接单=${uiState.value.pendingRequests.size}, 我的服务=${uiState.value.myRequests.size}",
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            StatCard(stats = uiState.value.stats)
            
            TabRow(
                selectedTabIndex = when (uiState.value.currentTab) {
                    VolunteerTab.PENDING -> 0
                    VolunteerTab.MY_SERVICES -> 1
                }
            ) {
                Tab(
                    selected = uiState.value.currentTab == VolunteerTab.PENDING,
                    onClick = { viewModel.switchTab(VolunteerTab.PENDING) },
                    text = { Text("待接单") },
                    icon = { Icon(Icons.Default.Inbox, contentDescription = null) }
                )
                Tab(
                    selected = uiState.value.currentTab == VolunteerTab.MY_SERVICES,
                    onClick = { viewModel.switchTab(VolunteerTab.MY_SERVICES) },
                    text = { Text("我的服务") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
            }

            if (uiState.value.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.value.currentTab) {
                    VolunteerTab.PENDING -> PendingRequestsContent(
                        requests = uiState.value.pendingRequests,
                        onAccept = { viewModel.acceptRequest(it) },
                        onRefresh = { viewModel.loadData() }
                    )
                    VolunteerTab.MY_SERVICES -> MyServicesContent(
                        requests = uiState.value.myRequests,
                        onComplete = { viewModel.completeRequest(it) }
                    )
                }
            }

            uiState.value.errorMessage?.let { error ->
                Snackbar {
                    Text(error)
                }
            }
        }
    }
}

@Composable
fun StatCard(stats: com.mindeye.app.feature.volunteer.viewmodel.VolunteerStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "我的服务统计",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("总服务", stats.totalServed.toString())
                StatItem("已完成", stats.completedCount.toString())
                StatItem("进行中", stats.pendingCount.toString())
                StatItem("评分", stats.rating.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun PendingRequestsContent(
    requests: List<HelpRequest>,
    onAccept: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (requests.isEmpty()) {
            item {
                EmptyStateWithRefresh(
                    icon = Icons.Default.Inbox,
                    title = "暂无待接单求助",
                    description = "目前没有需要帮助的求助请求",
                    onRefresh = onRefresh
                )
            }
        } else {
            items(requests) { request ->
                HelpRequestCard(
                    request = request,
                    onAccept = { onAccept(request.id) },
                    showCompleteButton = false
                )
            }
        }
    }
}

@Composable
fun MyServicesContent(
    requests: List<HelpRequest>,
    onComplete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (requests.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = "暂无服务记录",
                    description = "还没有接受过求助请求"
                )
            }
        } else {
            items(requests) { request ->
                HelpRequestCard(
                    request = request,
                    onAccept = {},
                    showCompleteButton = request.status == HelpRequestStatus.ACCEPTED,
                    onComplete = { onComplete(request.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EmptyStateWithRefresh(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = title,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("手动刷新")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpRequestCard(
    request: HelpRequest,
    onAccept: () -> Unit,
    showCompleteButton: Boolean,
    onComplete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = request.requesterName, fontWeight = FontWeight.Bold)
                        Text(
                            text = "求助者",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                Badge(
                    containerColor = when (request.status) {
                        HelpRequestStatus.PENDING -> MaterialTheme.colorScheme.errorContainer
                        HelpRequestStatus.ACCEPTED -> MaterialTheme.colorScheme.primaryContainer
                        HelpRequestStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (request.status) {
                            HelpRequestStatus.PENDING -> "待接单"
                            HelpRequestStatus.ACCEPTED -> "进行中"
                            HelpRequestStatus.COMPLETED -> "已完成"
                            else -> "已取消"
                        },
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = request.content,
                modifier = Modifier.padding(top = 12.dp),
                lineHeight = 1.5.sp
            )

            if (!request.location.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = request.location,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (!request.volunteerName.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "志愿者: ${request.volunteerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                if (request.status == HelpRequestStatus.PENDING) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("接单帮助")
                    }
                } else if (showCompleteButton) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("完成服务")
                    }
                }
            }
        }
    }
}