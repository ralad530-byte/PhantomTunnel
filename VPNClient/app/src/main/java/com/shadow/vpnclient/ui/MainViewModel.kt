package com.shadow.vpnclient.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shadow.vpnclient.core.ConfigBuilder
import com.shadow.vpnclient.core.VpnConnectionService
import com.shadow.vpnclient.data.AppDatabase
import com.shadow.vpnclient.data.ServerEntity
import com.shadow.vpnclient.data.SubscriptionEntity
import com.shadow.vpnclient.data.SubscriptionRepository
import com.shadow.vpnclient.model.ServerConfig
import com.shadow.vpnclient.parser.UriParser
import com.shadow.vpnclient.util.PingTester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.VpnService

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val server: ServerConfig) : ConnectionState()
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val subRepo = SubscriptionRepository(db)

    val servers: StateFlow<List<ServerConfig>> = db.serverDao().observeAll()
        .map { list -> list.map { it.toModel() } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = db.subscriptionDao().observeAll()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _selectedServerId = MutableStateFlow<Long?>(null)
    val selectedServerId: StateFlow<Long?> = _selectedServerId

    fun selectServer(id: Long) { _selectedServerId.value = id }

    fun addServerFromLink(link: String): Boolean {
        val parsed = UriParser.parse(link) ?: return false
        viewModelScope.launch { db.serverDao().insert(ServerEntity.fromModel(parsed)) }
        return true
    }

    fun addServerManual(server: ServerConfig) {
        viewModelScope.launch { db.serverDao().insert(ServerEntity.fromModel(server)) }
    }

    fun deleteServer(server: ServerConfig) {
        viewModelScope.launch { db.serverDao().delete(ServerEntity.fromModel(server)) }
    }

    fun pingServer(server: ServerConfig) {
        viewModelScope.launch {
            val ms = PingTester.tcpPing(server.address, server.port)
            db.serverDao().updateLatency(server.id, ms)
        }
    }

    fun pingAll() {
        viewModelScope.launch {
            servers.value.forEach { pingServer(it) }
        }
    }

    fun addSubscription(name: String, url: String) {
        viewModelScope.launch {
            val id = db.subscriptionDao().insert(SubscriptionEntity(name = name, url = url))
            subRepo.refresh(id, url)
        }
    }

    fun refreshSubscription(sub: SubscriptionEntity) {
        viewModelScope.launch { subRepo.refresh(sub.id, sub.url) }
    }

    /** Returns the VpnService consent Intent if needed, or null if already granted. */
    fun prepareVpnPermission(): Intent? = VpnService.prepare(getApplication())

    fun connect(server: ServerConfig, bypassedApps: List<String> = emptyList()) {
        val ctx = getApplication<Application>()
        val config = ConfigBuilder.build(server)
        val intent = Intent(ctx, VpnConnectionService::class.java).apply {
            action = VpnConnectionService.ACTION_CONNECT
            putExtra(VpnConnectionService.EXTRA_CONFIG_JSON, config)
            putStringArrayListExtra(VpnConnectionService.EXTRA_BYPASSED_APPS, ArrayList(bypassedApps))
        }
        ctx.startService(intent)
        _connectionState.value = ConnectionState.Connected(server)
    }

    fun disconnect() {
        val ctx = getApplication<Application>()
        ctx.startService(Intent(ctx, VpnConnectionService::class.java).apply {
            action = VpnConnectionService.ACTION_DISCONNECT
        })
        _connectionState.value = ConnectionState.Disconnected
    }
}
