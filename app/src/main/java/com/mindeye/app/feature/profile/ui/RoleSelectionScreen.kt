package com.mindeye.app.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mindeye.app.core.model.UserRoleManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    roleManager: UserRoleManager,
    currentRole: String,
    onRoleChanged: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("角色设置") },
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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("选择你的身份", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("根据你的需求选择合适的身份，你可以随时切换", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Text("求助用户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedRole = UserRoleManager.ROLE_USER }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("普通用户", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("发布求助、浏览社区帖子、使用心理支持等功能", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("可以发布求助让志愿者看到", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RadioButton(
                        selected = selectedRole == UserRoleManager.ROLE_USER,
                        onClick = { selectedRole = UserRoleManager.ROLE_USER }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("志愿者", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { selectedRole = UserRoleManager.ROLE_VOLUNTEER }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("志愿者", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("可以查看求助、接单帮助他人、积累服务记录", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("可以获得徽章和志愿服务记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RadioButton(
                        selected = selectedRole == UserRoleManager.ROLE_VOLUNTEER,
                        onClick = { selectedRole = UserRoleManager.ROLE_VOLUNTEER }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onRoleChanged(selectedRole)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                if (selectedRole == UserRoleManager.ROLE_VOLUNTEER) {
                    Text("确认成为志愿者")
                } else {
                    Text("确认成为普通用户")
                }
            }
        }
    }
}
