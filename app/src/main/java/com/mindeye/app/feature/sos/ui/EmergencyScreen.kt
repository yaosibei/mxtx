package com.mindeye.app.feature.sos.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindeye.app.feature.sos.viewmodel.EmergencyContactUiState
import com.mindeye.app.feature.sos.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddContactDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }
    var newContactRelationship by remember { mutableStateOf("") }
    var sosConfirmState by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("紧急求助", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.semantics { contentDescription = "返回" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SOSButton(
                    onSosClick = {
                        if (sosConfirmState) {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:110")
                            context.startActivity(intent)
                            sosConfirmState = false
                        } else {
                            sosConfirmState = true
                        }
                    },
                    isConfirm = sosConfirmState
                )
            }

            item {
                Text(
                    text = if (sosConfirmState) "再次点击拨打 110" else "点击 SOS 按钮紧急求助",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmergencyCallButton(
                        label = "拨打 110",
                        description = "报警电话",
                        number = "110",
                        icon = Icons.Default.LocalPolice,
                        onClick = {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:110")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.error
                    )
                    EmergencyCallButton(
                        label = "拨打 120",
                        description = "急救电话",
                        number = "120",
                        icon = Icons.Default.LocalHospital,
                        onClick = {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:120")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmergencyCallButton(
                        label = "拨打 119",
                        description = "火警电话",
                        number = "119",
                        icon = Icons.Default.LocalFireDepartment,
                        onClick = {
                            val intent = Intent(Intent.ACTION_CALL)
                            intent.data = Uri.parse("tel:119")
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                    EmergencyCallButton(
                        label = "发送位置",
                        description = "分享给紧急联系人",
                        number = "",
                        icon = Icons.Default.LocationOn,
                        onClick = {
                            viewModel.sendLocationMessage()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "紧急联系人",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showAddContactDialog = true },
                        modifier = Modifier.semantics { contentDescription = "添加紧急联系人" }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "添加联系人")
                    }
                }
            }

            items(uiState.contacts) { contact ->
                EmergencyContactCard(
                    name = contact.name,
                    phone = contact.phone,
                    relationship = contact.relationship,
                    onCall = {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse("tel:${contact.phone}")
                        context.startActivity(intent)
                    },
                    onDelete = {
                        viewModel.deleteContact(contact)
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "紧急提示",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "长按 SOS 按钮 3 秒可快速拨打紧急联系人",
                            "拨打时会自动发送位置信息给紧急联系人",
                            "请保持冷静，清晰表达你的需求"
                        ).forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddContactDialog) {
        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("添加紧急联系人") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("姓名") },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "联系人姓名" }
                    )
                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("电话号码") },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "联系人电话号码" }
                    )
                    OutlinedTextField(
                        value = newContactRelationship,
                        onValueChange = { newContactRelationship = it },
                        label = { Text("关系（如：家人、朋友）") },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "与联系人关系" }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                            viewModel.addContact(
                                name = newContactName,
                                phone = newContactPhone,
                                relationship = newContactRelationship.ifBlank { "其他" }
                            )
                            newContactName = ""
                            newContactPhone = ""
                            newContactRelationship = ""
                            showAddContactDialog = false
                        }
                    },
                    enabled = newContactName.isNotBlank() && newContactPhone.isNotBlank()
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SOSButton(onSosClick: () -> Unit, isConfirm: Boolean) {
    Button(
        onClick = onSosClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = if (isConfirm) "确认拨打 110 紧急电话" else "SOS 一键求助，点击拨打紧急联系人" },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isConfirm) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isConfirm) "再次点击确认拨打 110" else "SOS 一键求助",
                style = if (isConfirm) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmergencyCallButton(
    label: String,
    description: String,
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .semantics { contentDescription = "$label，$description" },
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun EmergencyContactCard(
    name: String,
    phone: String,
    relationship: String,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$phone · $relationship",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.semantics { contentDescription = "拨打 $name 的电话" }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "拨打", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "删除联系人 $name" }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
