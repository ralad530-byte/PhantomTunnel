package com.shadow.vpnclient.model

enum class Protocol {
    VMESS, VLESS, TROJAN, SHADOWSOCKS, HYSTERIA2, TUIC;

    companion object {
        fun fromScheme(scheme: String): Protocol? = when (scheme.lowercase()) {
            "vmess" -> VMESS
            "vless" -> VLESS
            "trojan" -> TROJAN
            "ss" -> SHADOWSOCKS
            "hysteria2", "hy2" -> HYSTERIA2
            "tuic" -> TUIC
            else -> null
        }
    }
}
