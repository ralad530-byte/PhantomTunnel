package com.shadow.vpnclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.vpnclient.ui.ConnectionState
import com.shadow.vpnclient.ui.MainViewModel
import com.shadow.vpnclient.ui.components.ServerCard
import com.shadow.vpnclient.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    viewModel: MainViewModel,
    onAddServer: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestConnect: () -> Unit
) {
    val servers by viewModel.servers.collectAsState()
    val selectedId by viewModel.selectedServerId.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected = connectionState is ConnectionState.Connected

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shadow VPN") },
                actions = {
                    IconButton(onClick = { viewModel.pingAll() }) {
                        Icon(Icons.Filled.Speed, contentDescription = "Ping All")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddServer, containerColor = NeonCyan) {
                Icon(Icons.Filled.Add, contentDescription = "Add Server")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ConnectButton(
                isConnected = isConnected,
                enabled = selectedId != null,
                onClick = onRequestConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )

            if (servers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا يوجد سيرفرات بعد — اضغط + للإضافة", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(servers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            isSelected = server.id == selectedId,
                            onClick = { viewModel.selectServer(server.id) },
                            onPing = { viewModel.pingServer(server) },
                            onDelete = { viewModel.deleteServer(server) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConnectButton(
    isConnected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = if (isConnected) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    Icons.Filled.PowerSettingsNew,
                    contentDescription = if (isConnected) "Disconnect" else "Connect",
                    tint = if (isConnected) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}
