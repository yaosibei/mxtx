package com.mindeye.app.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * 全局 SOS 外壳。
 * 所有页面都包在这里，所以用户在任何界面都能看到右下角的 SOS 按钮。
 */
@Composable
fun GlobalSosScaffold(
    onSosClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSosClick,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                text = { Text("SOS") },
                modifier = Modifier.semantics {
                    contentDescription = "全局 SOS 求助按钮，点击进入紧急求助页面"
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(paddingValues)
        }
    }
}
