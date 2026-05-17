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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindeye.app.core.model.CommunityPost
import com.mindeye.app.feature.community.viewmodel.CommunityViewModel
import com.mindeye.app.feature.community.viewmodel.CommunityUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("社区互助", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.semantics { contentDescription = "返回" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showPostDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "发布新帖子")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPostDialog = true },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                text = { Text("发布求助") },
                modifier = Modifier.semantics { contentDescription = "发布新求助帖子" }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("加载失败：${uiState.errorMessage}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadPosts() }) { Text("重试") }
                }
            }
            uiState.posts.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        PostCard(post, onLike = { viewModel.likePost(post.id) })
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        CreatePostDialog(
            onDismiss = { showPostDialog = false },
            onPost = { title, content ->
                viewModel.createPost("local_user", "我", content, com.mindeye.app.core.model.PostType.TEXT)
                showPostDialog = false
            }
        )
    }
}

@Composable
fun PostCard(post: CommunityPost, onLike: () -> Unit) {
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
                Text("评论 ${post.comments}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CreatePostDialog(onDismiss: () -> Unit, onPost: (String, String) -> Unit) {
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
            Button(onClick = { if (content.isNotBlank()) onPost("", content) }, enabled = content.isNotBlank()) {
                Text("发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
