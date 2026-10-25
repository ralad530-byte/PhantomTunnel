package com.shadow.vpnclient.model

/**
 * Unified, protocol-agnostic representation of a single outbound server.
 * Persisted to Room; consumed by ConfigBuilder to produce the final
 * Xray-core JSON configuration.
 */
data class ServerConfig(
    val id: Long = 0,
    val remark: String,
    val protocol: Protocol,
    val address: String,
    val port: Int,

    val uuid: String? = null,          // VMess / VLESS id
    val password: String? = null,      // Trojan / SS / Hysteria2 / TUIC password
    val method: String? = null,        // Shadowsocks cipher

    val network: String = "tcp",       // tcp, ws, grpc, http, quic
    val path: String? = null,
    val host: String? = null,
    val headerType: String? = null,

    val tls: String = "none",          // none, tls, reality
    val sni: String? = null,
    val fingerprint: String? = null,
    val alpn: String? = null,
    val allowInsecure: Boolean = false,

    val publicKey: String? = null,     // REALITY
    val shortId: String? = null,
    val spiderX: String? = null,

    val flow: String? = null,          // VLESS: xtls-rprx-vision

    val alterId: Int = 0,
    val security: String? = null,      // VMess cipher

    val obfs: String? = null,
    val obfsPassword: String? = null,
    val congestionControl: String? = null,
    val udpRelayMode: String? = null,

    val subscriptionId: Long? = null,
    val latencyMs: Int? = null
)
