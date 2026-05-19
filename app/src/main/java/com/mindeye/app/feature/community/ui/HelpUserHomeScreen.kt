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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpUserHomeScreen(
    viewModel: CommunityViewModel,
    onNavigateToRoleSelection: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var helpContent by remember { mutableStateOf("") }
    var helpLocation by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HelpCategory.OTHER) }
    var selectedUrgency by remember { mutableStateOf(UrgencyLevel.LOW) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区求助", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    TextButton(onClick = onNavigateToRoleSelection) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("切换身份")
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
                HelpUserWelcomeCard()
            }
            item {
                QuickHelpButtons(
                    onHelpRequest = { viewModel.showHelpRequestDialog(true) },
                    onQuickTemplate = { viewModel.toggleQuickTemplates() },
                    onViewMyRequests = { viewModel.loadMyRequests() }
                )
            }
            if (uiState.showQuickTemplates) {
                item {
                    QuickTemplatesGrid(
                        templates = uiState.quickTemplates,
                        onTemplateClick = { viewModel.useTemplate(it) }
                    )
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("我的求助", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "已发布 ${uiState.myRequestCount} 个求助请求",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "志愿者可以看到并接单帮助",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                ActiveHelpRequestsCard(
                    helpRequests = uiState.helpRequests,
                    onToggleComments = { viewModel.toggleComments(it) },
                    onAddComment = { postId, text -> viewModel.addComment(postId, "我", text) },
                    showCommentsFor = uiState.showCommentsFor
                )
            }
            item {
                HelpUserTipsCard()
            }
        }
    }

    HelpRequestDialog(
        uiState = uiState,
        content = helpContent,
        location = helpLocation,
        category = selectedCategory,
        urgency = selectedUrgency,
        onContentChange = { helpContent = it },
        onLocationChange = { helpLocation = it },
        onCategoryChange = { selectedCategory = it },
        onUrgencyChange = { selectedUrgency = it },
        onPublish = {
            viewModel.createHelpRequest(
                userId = "local_user",
                userName = "我",
                content = helpContent,
                helpCategory = selectedCategory,
                urgencyLevel = selectedUrgency,
                locationName = if (helpLocation.isNotBlank()) helpLocation else null
            )
            helpContent = ""
            helpLocation = ""
        },
        onDismiss = { viewModel.showHelpRequestDialog(false) }
    )
}

@Composable
private fun HelpUserWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Handshake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "需要帮助？",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "发布求助信息，志愿者会来帮助你\n你可以随时切换为志愿者身份去帮助他人",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickHelpButtons(
    onHelpRequest: () -> Unit,
    onQuickTemplate: () -> Unit,
    onViewMyRequests: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onHelpRequest,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("发布求助", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onQuickTemplate,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("快捷模板")
            }
            OutlinedButton(
                onClick = onViewMyRequests,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("我的求助")
            }
        }
    }
}

@Composable
private fun QuickTemplatesGrid(
    templates: List<QuickTemplate>,
    onTemplateClick: (QuickTemplate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("快捷模板", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(templates) { template ->
                    QuickTemplateCard(template = template, onClick = { onTemplateClick(template) })
                }
            }
        }
    }
}

@Composable
private fun ActiveHelpRequestsCard(
    helpRequests: List<CommunityPost>,
    onToggleComments: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    showCommentsFor: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("求助列表", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (helpRequests.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "暂无求助信息\n点击上方'发布求助'开始",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    helpRequests.forEach { post ->
                        SimpleHelpRequestCard(
                            post = post,
                            onToggleComments = { onToggleComments(post.id) },
                            onAddComment = { text -> onAddComment(post.id, text) },
                            isExpanded = showCommentsFor == post.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleHelpRequestCard(
    post: CommunityPost,
    onToggleComments: () -> Unit,
    onAddComment: (String) -> Unit,
    isExpanded: Boolean
) {
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (post.urgencyLevel) {
                UrgencyLevel.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                UrgencyLevel.LOW -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        post.helpCategory.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        post.urgencyLevel.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = when (post.urgencyLevel) {
                            UrgencyLevel.HIGH -> MaterialTheme.colorScheme.error
                            UrgencyLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
                            UrgencyLevel.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(post.userName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(post.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (post.volunteerName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("志愿者：${post.volunteerName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onToggleComments) {
                    Text("评论 (${post.comments})", style = MaterialTheme.typography.labelSmall)
                }
            }
            if (isExpanded) {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    post.commentList.forEach { comment ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (comment.isFromVolunteer) "🧑‍🤝‍🧑" else "👤", style = MaterialTheme.typography.labelSmall)
                            Column {
                                Text(comment.authorName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(comment.content, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f).height(48.dp),
                            placeholder = { Text("回复...", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onAddComment(commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Text("发送")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpUserTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("求助小贴士", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("• 描述清楚你需要什么帮助", style = MaterialTheme.typography.bodySmall)
                Text("• 提供你的位置信息，方便志愿者找到你", style = MaterialTheme.typography.bodySmall)
                Text("• 选择合适的紧急程度，紧急情况用红色", style = MaterialTheme.typography.bodySmall)
                Text("• 使用快捷模板快速发布常见求助", style = MaterialTheme.typography.bodySmall)
                Text("• 切换到志愿者身份，你也可以帮助别人！", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpRequestDialog(
    uiState: CommunityUiState,
    content: String,
    location: String,
    category: HelpCategory,
    urgency: UrgencyLevel,
    onContentChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onCategoryChange: (HelpCategory) -> Unit,
    onUrgencyChange: (UrgencyLevel) -> Unit,
    onPublish: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!uiState.showHelpRequestDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发布求助") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    label = { Text("求助内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("位置（选填）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("求助分类:", style = MaterialTheme.typography.labelLarge)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(HelpCategory.values()) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat.label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text("紧急程度:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UrgencyLevel.values().forEach { level ->
                        FilterChip(
                            selected = urgency == level,
                            onClick = { onUrgencyChange(level) },
                            label = { Text(level.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPublish,
                enabled = content.isNotBlank()
            ) {
                Text("发布求助")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun QuickTemplateCard(
    template: QuickTemplate,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(template.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                template.content,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}