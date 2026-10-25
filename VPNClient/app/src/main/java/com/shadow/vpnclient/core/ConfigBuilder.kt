package com.shadow.vpnclient.core

import com.shadow.vpnclient.model.Protocol
import com.shadow.vpnclient.model.ServerConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 * Builds the full Xray-core JSON configuration string for a given server.
 * The resulting JSON is what gets passed into libv2ray's startLoop(config: String)
 * (see XrayCoreManager). Routing rules keep LAN/private traffic, and optionally
 * apps the user excludes, outside the tunnel.
 */
object ConfigBuilder {

    fun build(
        server: ServerConfig,
        socksPort: Int = 10808,
        httpPort: Int = 10809,
        bypassLan: Boolean = true,
        enableSniffing: Boolean = true
    ): String {
        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))

        root.put("inbounds", JSONArray().apply {
            put(inbound("socks", socksPort, enableSniffing))
            put(inbound("http", httpPort, enableSniffing))
        })

        root.put("outbounds", JSONArray().apply {
            put(buildOutbound(server))
            put(JSONObject().put("protocol", "freedom").put("tag", "direct"))
            put(JSONObject().put("protocol", "blackhole").put("tag", "block"))
        })

        if (bypassLan) {
            root.put("routing", buildRouting())
        }

        return root.toString()
    }

    private fun inbound(protocol: String, port: Int, sniffing: Boolean) = JSONObject().apply {
        put("tag", "$protocol-in")
        put("protocol", protocol)
        put("listen", "127.0.0.1")
        put("port", port)
        put("settings", JSONObject().put("udp", true))
        if (sniffing) {
            put("sniffing", JSONObject()
                .put("enabled", true)
                .put("destOverride", JSONArray(listOf("http", "tls"))))
        }
    }

    private fun buildRouting() = JSONObject().apply {
        put("domainStrategy", "IPIfNonMatch")
        put("rules", JSONArray().apply {
            put(JSONObject()
                .put("type", "field")
                .put("ip", JSONArray(listOf(
                    "geoip:private", "0.0.0.0/8", "10.0.0.0/8",
                    "127.0.0.0/8", "169.254.0.0/16", "172.16.0.0/12",
                    "192.168.0.0/16", "fc00::/7", "fe80::/10"
                )))
                .put("outboundTag", "direct"))
        })
    }

    private fun buildOutbound(s: ServerConfig): JSONObject = when (s.protocol) {
        Protocol.VMESS -> vmessOutbound(s)
        Protocol.VLESS -> vlessOutbound(s)
        Protocol.TROJAN -> trojanOutbound(s)
        Protocol.SHADOWSOCKS -> shadowsocksOutbound(s)
        Protocol.HYSTERIA2 -> hysteria2Outbound(s)
        Protocol.TUIC -> tuicOutbound(s)
    }

    private fun streamSettings(s: ServerConfig): JSONObject {
        val stream = JSONObject().put("network", s.network)

        when (s.network) {
            "ws" -> stream.put("wsSettings", JSONObject().apply {
                put("path", s.path ?: "/")
                s.host?.let { put("headers", JSONObject().put("Host", it)) }
            })
            "grpc" -> stream.put("grpcSettings", JSONObject().apply {
                put("serviceName", s.path ?: "")
            })
            "http", "h2" -> stream.put("httpSettings", JSONObject().apply {
                put("path", s.path ?: "/")
                s.host?.let { put("host", JSONArray(listOf(it))) }
            })
            "tcp" -> if (s.headerType == "http") {
                stream.put("tcpSettings", JSONObject().put("header", JSONObject()
                    .put("type", "http")
                    .put("request", JSONObject().put("path", JSONArray(listOf(s.path ?: "/"))))))
            }
        }

        when (s.tls) {
            "tls" -> {
                stream.put("security", "tls")
                stream.put("tlsSettings", JSONObject().apply {
                    put("allowInsecure", s.allowInsecure)
                    (s.sni ?: s.host)?.let { put("serverName", it) }
                    s.fingerprint?.let { put("fingerprint", it) }
                    s.alpn?.let { put("alpn", JSONArray(it.split(","))) }
                })
            }
            "reality" -> {
                stream.put("security", "reality")
                stream.put("realitySettings", JSONObject().apply {
                    put("serverName", s.sni ?: "")
                    put("fingerprint", s.fingerprint ?: "chrome")
                    put("publicKey", s.publicKey ?: "")
                    put("shortId", s.shortId ?: "")
                    s.spiderX?.let { put("spiderX", it) }
                })
            }
        }
        return stream
    }

    private fun vmessOutbound(s: ServerConfig) = JSONObject().apply {
        put("tag", "proxy")
        put("protocol", "vmess")
        put("settings", JSONObject().put("vnext", JSONArray().put(JSONObject()
            .put("address", s.address)
            .put("port", s.port)
            .put("users", JSONArray().put(JSONObject()
                .put("id", s.uuid)
                .put("alterId", s.alterId)
                .put("security", s.security ?: "auto")))
        )))
        put("streamSettings", streamSettings(s))
    }

    private fun vlessOutbound(s: ServerConfig) = JSONObject().apply {
        put("tag", "proxy")
        put("protocol", "vless")
        put("settings", JSONObject().put("vnext", JSONArray().put(JSONObject()
            .put("address", s.address)
            .put("port", s.port)
            .put("users", JSONArray().put(JSONObject().apply {
                put("id", s.uuid)
                put("encryption", "none")
                s.flow?.let { put("flow", it) }
            }))
        )))
        put("streamSettings", streamSettings(s))
    }

    private fun trojanOutbound(s: ServerConfig) = JSONObject().apply {
        put("tag", "proxy")
        put("protocol", "trojan")
        put("settings", JSONObject().put("servers", JSONArray().put(JSONObject()
            .put("address", s.address)
            .put("port", s.port)
            .put("password", s.password)
        )))
        put("streamSettings", streamSettings(s))
    }

    private fun shadowsocksOutbound(s: ServerConfig) = JSONObject().apply {
        put("tag", "proxy")
        put("protocol", "shadowsocks")
        put("settings", JSONObject().put("servers", JSONArray().put(JSONObject()
            .put("address", s.address)
            .put("port", s.port)
            .put("method", s.method ?: "aes-256-gcm")
            .put("password", s.password)
        )))
    }

    // Hysteria2 and TUIC are not implemented inside xray-core's main outbound set;
    // they need either the sing-box core or Xray's experimental bridges. The JSON
    // below targets the standalone hysteria2/tuic-client config schema instead,
    // and XrayCoreManager routes to the matching native engine based on protocol.
    private fun hysteria2Outbound(s: ServerConfig) = JSONObject().apply {
        put("server", "${s.address}:${s.port}")
        put("auth", s.password)
        put("tls", JSONObject().apply {
            put("sni", s.sni ?: s.address)
            put("insecure", s.allowInsecure)
        })
        s.obfs?.let {
            put("obfs", JSONObject().put("type", it).put("password", s.obfsPassword ?: ""))
        }
        put("socks5", JSONObject().put("listen", "127.0.0.1:10808"))
    }

    private fun tuicOutbound(s: ServerConfig) = JSONObject().apply {
        put("relay", JSONObject().apply {
            put("server", "${s.address}:${s.port}")
            put("uuid", s.uuid)
            put("password", s.password)
            put("congestion_control", s.congestionControl ?: "bbr")
            put("udp_relay_mode", s.udpRelayMode ?: "native")
            put("alpn", JSONArray((s.alpn ?: "h3").split(",")))
            put("sni", s.sni ?: s.address)
            put("insecure", s.allowInsecure)
        })
        put("local", JSONObject().put("server", "127.0.0.1:10808"))
    }
}
