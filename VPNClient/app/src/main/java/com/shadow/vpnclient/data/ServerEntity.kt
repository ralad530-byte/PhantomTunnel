package com.shadow.vpnclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shadow.vpnclient.model.Protocol
import com.shadow.vpnclient.model.ServerConfig

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remark: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val uuid: String? = null,
    val password: String? = null,
    val method: String? = null,
    val network: String = "tcp",
    val path: String? = null,
    val host: String? = null,
    val headerType: String? = null,
    val tls: String = "none",
    val sni: String? = null,
    val fingerprint: String? = null,
    val alpn: String? = null,
    val allowInsecure: Boolean = false,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null,
    val flow: String? = null,
    val alterId: Int = 0,
    val security: String? = null,
    val obfs: String? = null,
    val obfsPassword: String? = null,
    val congestionControl: String? = null,
    val udpRelayMode: String? = null,
    val subscriptionId: Long? = null,
    val latencyMs: Int? = null,
    val sortOrder: Int = 0
) {
    fun toModel() = ServerConfig(
        id, remark, Protocol.valueOf(protocol), address, port, uuid, password, method,
        network, path, host, headerType, tls, sni, fingerprint, alpn, allowInsecure,
        publicKey, shortId, spiderX, flow, alterId, security, obfs, obfsPassword,
        congestionControl, udpRelayMode, subscriptionId, latencyMs
    )

    companion object {
        fun fromModel(s: ServerConfig, sortOrder: Int = 0) = ServerEntity(
            s.id, s.remark, s.protocol.name, s.address, s.port, s.uuid, s.password, s.method,
            s.network, s.path, s.host, s.headerType, s.tls, s.sni, s.fingerprint, s.alpn,
            s.allowInsecure, s.publicKey, s.shortId, s.spiderX, s.flow, s.alterId, s.security,
            s.obfs, s.obfsPassword, s.congestionControl, s.udpRelayMode, s.subscriptionId,
            s.latencyMs, sortOrder
        )
    }
}

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val lastUpdated: Long = 0
)
