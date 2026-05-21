package com.mindeye.app.feature.community.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindeye.app.core.model.AiQaMessage
import com.mindeye.app.core.model.CommunityPost
import com.mindeye.app.core.model.HelpRequest
import com.mindeye.app.core.model.HelpRequestStatus
import com.mindeye.app.feature.community.viewmodel.CommunityTab
import com.mindeye.app.feature.community.viewmodel.CommunityViewModel
import com.mindeye.app.feature.community.viewmodel.CommunityUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVolunteer: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPostDialog by remember { mutableStateOf(false) }
    var showHelpRequestDialog by remember { mutableStateOf(false) }
    var isSavingHelpRequest by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区与心理支持", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.semantics { contentDescription = "返回" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            CommunityTabBar(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.switchTab(it) },
                onNavigateToVolunteer = onNavigateToVolunteer
            )
        },
        floatingActionButton = {
            when (uiState.currentTab) {
                CommunityTab.POSTS -> ExtendedFloatingActionButton(
                    onClick = { 
                        showPostDialog = true 
                        // 同时打开求助对话框，方便测试
                        showHelpRequestDialog = true
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("发布帖子") },
                    modifier = Modifier.semantics { contentDescription = "发布新帖子" }
                )
                CommunityTab.HELP_REQUESTS -> ExtendedFloatingActionButton(
                    onClick = { showHelpRequestDialog = true },
                    icon = { Icon(Icons.Default.Handshake, contentDescription = null) },
                    text = { Text("发起求助") },
                    modifier = Modifier.semantics { contentDescription = "发起新的求助请求" }
                )
                CommunityTab.ENCOURAGEMENTS -> ExtendedFloatingActionButton(
                    onClick = { viewModel.generateEncouragement() },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("生成鼓励") },
                    modifier = Modifier.semantics { contentDescription = "根据出行记录生成鼓励语" }
                )
                else -> {}
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState.currentTab) {
                CommunityTab.POSTS -> {
                    PostsTabContent(
                        uiState = uiState,
                        _viewModel = viewModel,
                        onLike = { viewModel.likePost(it) }
                    )
                }
                CommunityTab.HELP_REQUESTS -> {
                    HelpRequestsTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigateToVolunteer = onNavigateToVolunteer
                    )
                }
                CommunityTab.VOLUNTEER -> {
                }
                CommunityTab.AI_QA -> {
                    AiQaTabContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
                CommunityTab.ENCOURAGEMENTS -> {
                    EncouragementsTabContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    if (showPostDialog) {
        CreatePostDialog(
            onDismiss = { showPostDialog = false },
            onPost = { content ->
                viewModel.createPost("local_user", "我", content, com.mindeye.app.core.model.PostType.TEXT)
                showPostDialog = false
            }
        )
    }

    if (showHelpRequestDialog) {
        CreateHelpRequestDialog(
            onDismiss = { if (!isSavingHelpRequest) showHelpRequestDialog = false },
            onRequest = { content, location ->
                isSavingHelpRequest = true
                viewModel.createHelpRequest(content, location)
                showHelpRequestDialog = false
                isSavingHelpRequest = false
            }
        )
    }
}

// ==================== Tab 导航栏 ====================

@Composable
private fun CommunityTabBar(
    currentTab: CommunityTab,
    onTabSelected: (CommunityTab) -> Unit,
    onNavigateToVolunteer: (() -> Unit)? = null
) {
    Surface(tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Tab(
                selected = currentTab == CommunityTab.POSTS,
                onClick = { onTabSelected(CommunityTab.POSTS) },
                icon = { Icon(Icons.Default.People, contentDescription = "社区") },
                text = { Text("社区") }
            )
            Tab(
                selected = currentTab == CommunityTab.HELP_REQUESTS,
                onClick = { onTabSelected(CommunityTab.HELP_REQUESTS) },
                icon = { Icon(Icons.Default.Handshake, contentDescription = "求助") },
                text = { Text("求助") }
            )
            Tab(
                selected = currentTab == CommunityTab.VOLUNTEER,
                onClick = { onNavigateToVolunteer?.invoke() },
                icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = "志愿者") },
                text = { Text("志愿者") }
            )
            Tab(
                selected = currentTab == CommunityTab.AI_QA,
                onClick = { onTabSelected(CommunityTab.AI_QA) },
                icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI问答") },
                text = { Text("AI问答") }
            )
            Tab(
                selected = currentTab == CommunityTab.ENCOURAGEMENTS,
                onClick = { onTabSelected(CommunityTab.ENCOURAGEMENTS) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "鼓励") },
                text = { Text("鼓励") }
            )
        }
    }
}

// ==================== 社区帖子 Tab ====================

@Composable
private fun PostsTabContent(
    uiState: CommunityUiState,
    _viewModel: CommunityViewModel,
    onLike: (String) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.posts.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("暂无帖子，点击右下角发布第一篇", style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onLike = { onLike(post.id) },
                        onAddComment = { _viewModel.addComment(post.id, "local_user", "我", it) }
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "帖子：${post.userName}，${post.content}" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = "点赞", tint = MaterialTheme.colorScheme.error)
                    }
                    Text("${post.likes}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.Message, contentDescription = "评论", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("评论 ${post.comments}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                if (post.commentList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(post.commentList) { comment ->
                            CommentItem(comment = comment)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text("暂无评论，快来发表第一条评论吧！", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("写下你的评论...") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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

@Composable
private fun CommentItem(comment: com.mindeye.app.core.model.Comment) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(comment.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(formatTimestamp(comment.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(comment.content, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CreatePostDialog(onDismiss: () -> Unit, onPost: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发布求助") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text("写下你的求助内容...") },
                maxLines = 5
            )
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onPost(content) }, enabled = content.isNotBlank()) {
                Text("发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ==================== 求助与接单 Tab ====================

@Composable
private fun HelpRequestsTabContent(
    uiState: CommunityUiState,
    viewModel: CommunityViewModel,
    onNavigateToVolunteer: (() -> Unit)? = null
) {
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.helpRequests.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("暂无待接单的求助请求", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("点击右下角可以发起新的求助", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onNavigateToVolunteer != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateToVolunteer) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("进入志愿者中心")
                    }
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.helpRequests, key = { it.id }) { request ->
                    HelpRequestCard(
                        request = request,
                        onAccept = { viewModel.acceptHelpRequest(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun HelpRequestCard(request: HelpRequest, onAccept: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "求助：${request.requesterName}，${request.content}" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(request.requesterName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                StatusBadge(status = request.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(request.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            request.location?.let {
                Text("位置：$it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("时间：${formatTimestamp(request.timestamp)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (request.status == HelpRequestStatus.PENDING) {
                    Button(
                        onClick = { onAccept(request.id) },
                        modifier = Modifier.semantics { contentDescription = "接单：帮助${request.requesterName}" }
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("接单")
                    }
                } else if (request.status == HelpRequestStatus.ACCEPTED) {
                    Text("已接单", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: HelpRequestStatus) {
    val (text, color) = when (status) {
        HelpRequestStatus.PENDING -> "待接单" to MaterialTheme.colorScheme.primary
        HelpRequestStatus.ACCEPTED -> "已接单" to MaterialTheme.colorScheme.tertiary
        HelpRequestStatus.COMPLETED -> "已完成" to MaterialTheme.colorScheme.secondary
        HelpRequestStatus.CANCELLED -> "已取消" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun CreateHelpRequestDialog(onDismiss: () -> Unit, onRequest: (String, String?) -> Unit) {
    var content by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发起求助") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("描述你需要帮助的内容...") },
                    maxLines = 4
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("你的位置（可选）") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onRequest(content, location.takeIf { it.isNotBlank() }) }, enabled = content.isNotBlank()) {
                Text("发布求助")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ==================== AI 问答 Tab ====================

@Composable
private fun AiQaTabContent(
    uiState: CommunityUiState,
    viewModel: CommunityViewModel
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.aiQaMessages.size) {
        if (uiState.aiQaMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.aiQaMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.aiQaMessages) { message ->
                AiQaBubble(message)
            }
            if (uiState.isAiTyping) {
                item { AiTypingIndicator() }
            }
        }

        AiQaInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendAiQuestion(inputText)
                    inputText = ""
                }
            }
        )
    }
}

@Composable
private fun AiQaBubble(message: AiQaMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .semantics { contentDescription = if (message.isFromUser) "我：${message.content}" else "AI助手：${message.content}" }
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (message.isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AiTypingIndicator() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("正在回答...", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AiQaInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f).semantics { contentDescription = "输入问题" },
                placeholder = { Text("输入你的问题...") },
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSend,
                modifier = Modifier.size(48.dp).semantics { contentDescription = "发送问题" },
                shape = RoundedCornerShape(24.dp),
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
            }
        }
    }
}

// ==================== 鼓励语 Tab ====================

@Composable
private fun EncouragementsTabContent(
    uiState: CommunityUiState,
    viewModel: CommunityViewModel
) {
    when {
        uiState.encouragements.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("还没有鼓励语", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("点击右下角，根据你的出行记录生成鼓励语", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.generateEncouragement() }) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成鼓励语")
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.encouragements, key = { it.id }) { encouragement ->
                    EncouragementCard(
                        encouragement = encouragement,
                        onRead = { viewModel.markEncouragementAsRead(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EncouragementCard(
    encouragement: com.mindeye.app.core.model.Encouragement,
    onRead: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "鼓励语：${encouragement.content}" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                if (!encouragement.triggerScene.equals("general", ignoreCase = true)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (encouragement.triggerScene) {
                                "indoor_quiet" -> "室内安静"
                                "outdoor_travel" -> "户外出行"
                                "social_interaction" -> "社交互动"
                                else -> encouragement.triggerScene
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(encouragement.content, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(formatTimestamp(encouragement.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!encouragement.triggerScene.equals("read", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onRead(encouragement.id) }) {
                    Text("标记为已读", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ==================== 工具函数 ====================

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        else -> "${diff / (24 * 60 * 60 * 1000)}天前"
    }
}
