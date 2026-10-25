package com.shadow.vpnclient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shadow.vpnclient.ui.ConnectionState
import com.shadow.vpnclient.ui.MainViewModel
import com.shadow.vpnclient.ui.screens.AddServerScreen
import com.shadow.vpnclient.ui.screens.ServerListScreen
import com.shadow.vpnclient.ui.screens.SettingsScreen
import com.shadow.vpnclient.ui.screens.SubscriptionScreen
import com.shadow.vpnclient.ui.theme.ShadowVPNTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            pendingConnectServer?.let { viewModel.connect(it) }
        }
        pendingConnectServer = null
    }

    private var pendingConnectServer: com.shadow.vpnclient.model.ServerConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deep-link import: app opened via vmess://, vless://, trojan://, etc.
        intent?.data?.toString()?.let { link ->
            viewModel.addServerFromLink(link)
        }

        setContent {
            ShadowVPNTheme {
                val navController = rememberNavController()
                val connectionState by viewModel.connectionState.collectAsState()
                val servers by viewModel.servers.collectAsState()
                val selectedId by viewModel.selectedServerId.collectAsState()

                NavHost(navController, startDestination = "list") {
                    composable("list") {
                        ServerListScreen(
                            viewModel = viewModel,
                            onAddServer = { navController.navigate("add") },
                            onOpenSettings = { navController.navigate("settings") },
                            onRequestConnect = {
                                val isConnected = connectionState is ConnectionState.Connected
                                if (isConnected) {
                                    viewModel.disconnect()
                                } else {
                                    val server = servers.firstOrNull { it.id == selectedId } ?: return@ServerListScreen
                                    val permissionIntent = viewModel.prepareVpnPermission()
                                    if (permissionIntent != null) {
                                        pendingConnectServer = server
                                        vpnPermissionLauncher.launch(permissionIntent)
                                    } else {
                                        viewModel.connect(server)
                                    }
                                }
                            }
                        )
                    }
                    composable("add") {
                        AddServerScreen(
                            viewModel = viewModel,
                            onDone = { navController.popBackStack() },
                            onScanQr = { /* TODO: launch CameraX + ML Kit barcode scanner screen */ }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onOpenSubscriptions = { navController.navigate("subscriptions") })
                    }
                    composable("subscriptions") {
                        SubscriptionScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
