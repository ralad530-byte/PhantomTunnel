package com.shadow.vpnclient.parser

import android.util.Base64
import com.shadow.vpnclient.model.Protocol
import com.shadow.vpnclient.model.ServerConfig
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder

/**
 * Parses the share-link formats used across the V2Ray/Xray ecosystem into
 * a unified ServerConfig. Supports:
 *   vmess://<base64 json>
 *   vless://uuid@host:port?params#remark
 *   trojan://password@host:port?params#remark
 *   ss://base64(method:password)@host:port#remark   (and the fully-base64 legacy form)
 *   hysteria2://password@host:port?params#remark
 *   tuic://uuid:password@host:port?params#remark
 */
object UriParser {

    fun parse(raw: String): ServerConfig? {
        val trimmed = raw.trim()
        return try {
            when {
                trimmed.startsWith("vmess://") -> parseVmess(trimmed)
                trimmed.startsWith("vless://") -> parseVless(trimmed)
                trimmed.startsWith("trojan://") -> parseTrojan(trimmed)
                trimmed.startsWith("ss://") -> parseShadowsocks(trimmed)
                trimmed.startsWith("hysteria2://") || trimmed.startsWith("hy2://") -> parseHysteria2(trimmed)
                trimmed.startsWith("tuic://") -> parseTuic(trimmed)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ---- vmess: base64-encoded JSON blob ----
    private fun parseVmess(uri: String): ServerConfig {
        val b64 = uri.removePrefix("vmess://")
        val json = JSONObject(decodeB64(b64))
        return ServerConfig(
            remark = json.optString("ps", "VMess Server"),
            protocol = Protocol.VMESS,
            address = json.getString("add"),
            port = json.getString("port").toInt(),
            uuid = json.getString("id"),
            alterId = json.optString("aid", "0").toIntOrNull() ?: 0,
            security = json.optString("scy", "auto"),
            network = json.optString("net", "tcp"),
            path = json.optString("path").ifBlank { null },
            host = json.optString("host").ifBlank { null },
            headerType = json.optString("type").ifBlank { null },
            tls = json.optString("tls", "none").ifBlank { "none" },
            sni = json.optString("sni").ifBlank { null },
            fingerprint = json.optString("fp").ifBlank { null },
            alpn = json.optString("alpn").ifBlank { null }
        )
    }

    // ---- vless://uuid@host:port?params#remark ----
    private fun parseVless(uri: String): ServerConfig {
        val u = URI(uri)
        val (userInfo, host, port) = extractAuthority(u)
        val q = parseQuery(u.rawQuery)
        return ServerConfig(
            remark = decodeFragment(u.rawFragment) ?: "VLESS Server",
            protocol = Protocol.VLESS,
            address = host,
            port = port,
            uuid = userInfo,
            network = q["type"] ?: "tcp",
            path = q["path"],
            host = q["host"] ?: q["sni"],
            headerType = q["headerType"],
            tls = q["security"] ?: "none",
            sni = q["sni"],
            fingerprint = q["fp"],
            alpn = q["alpn"],
            allowInsecure = q["allowInsecure"] == "1" || q["allowInsecure"] == "true",
            publicKey = q["pbk"],
            shortId = q["sid"],
            spiderX = q["spx"],
            flow = q["flow"]
        )
    }

    // ---- trojan://password@host:port?params#remark ----
    private fun parseTrojan(uri: String): ServerConfig {
        val u = URI(uri)
        val (userInfo, host, port) = extractAuthority(u)
        val q = parseQuery(u.rawQuery)
        return ServerConfig(
            remark = decodeFragment(u.rawFragment) ?: "Trojan Server",
            protocol = Protocol.TROJAN,
            address = host,
            port = port,
            password = userInfo,
            network = q["type"] ?: "tcp",
            path = q["path"],
            host = q["host"] ?: q["sni"],
            tls = q["security"] ?: "tls",
            sni = q["sni"],
            fingerprint = q["fp"],
            alpn = q["alpn"],
            allowInsecure = q["allowInsecure"] == "1" || q["allowInsecure"] == "true"
        )
    }

    // ---- ss://base64(method:password)@host:port#remark ----
    private fun parseShadowsocks(uri: String): ServerConfig {
        val body = uri.removePrefix("ss://")
        val hashIdx = body.indexOf('#')
        val remark = if (hashIdx >= 0) decodeFragment(body.substring(hashIdx + 1)) else null
        val main = if (hashIdx >= 0) body.substring(0, hashIdx) else body

        val atIdx = main.lastIndexOf('@')
        return if (atIdx >= 0) {
            // SIP002 form: base64(method:password)@host:port
            val userInfo = decodeB64(main.substring(0, atIdx))
            val hostPort = main.substring(atIdx + 1)
            val parts = userInfo.split(":", limit = 2)
            val (h, p) = hostPort.split(":", limit = 2)
            ServerConfig(
                remark = remark ?: "SS Server",
                protocol = Protocol.SHADOWSOCKS,
                address = h,
                port = p.toInt(),
                method = parts.getOrNull(0),
                password = parts.getOrNull(1)
            )
        } else {
            // Legacy fully-base64 form: base64(method:password@host:port)
            val decoded = decodeB64(main)
            val at = decoded.lastIndexOf('@')
            val userInfo = decoded.substring(0, at)
            val hostPort = decoded.substring(at + 1)
            val parts = userInfo.split(":", limit = 2)
            val (h, p) = hostPort.split(":", limit = 2)
            ServerConfig(
                remark = remark ?: "SS Server",
                protocol = Protocol.SHADOWSOCKS,
                address = h,
                port = p.toInt(),
                method = parts.getOrNull(0),
                password = parts.getOrNull(1)
            )
        }
    }

    // ---- hysteria2://password@host:port?params#remark ----
    private fun parseHysteria2(uri: String): ServerConfig {
        val u = URI(uri.replaceFirst("hy2://", "hysteria2://"))
        val (userInfo, host, port) = extractAuthority(u)
        val q = parseQuery(u.rawQuery)
        return ServerConfig(
            remark = decodeFragment(u.rawFragment) ?: "Hysteria2 Server",
            protocol = Protocol.HYSTERIA2,
            address = host,
            port = port,
            password = userInfo,
            sni = q["sni"],
            tls = "tls",
            obfs = q["obfs"],
            obfsPassword = q["obfs-password"],
            allowInsecure = q["insecure"] == "1" || q["insecure"] == "true"
        )
    }

    // ---- tuic://uuid:password@host:port?params#remark ----
    private fun parseTuic(uri: String): ServerConfig {
        val u = URI(uri)
        val (userInfo, host, port) = extractAuthority(u)
        val q = parseQuery(u.rawQuery)
        val parts = userInfo.split(":", limit = 2)
        return ServerConfig(
            remark = decodeFragment(u.rawFragment) ?: "TUIC Server",
            protocol = Protocol.TUIC,
            address = host,
            port = port,
            uuid = parts.getOrNull(0),
            password = parts.getOrNull(1),
            sni = q["sni"],
            tls = "tls",
            congestionControl = q["congestion_control"] ?: "bbr",
            udpRelayMode = q["udp_relay_mode"] ?: "native",
            alpn = q["alpn"],
            allowInsecure = q["allow_insecure"] == "1" || q["allow_insecure"] == "true"
        )
    }

    // ---------- helpers ----------

    private fun extractAuthority(u: URI): Triple<String, String, Int> {
        val userInfo = u.userInfo ?: ""
        val host = u.host ?: throw IllegalArgumentException("missing host")
        val port = if (u.port > 0) u.port else 443
        return Triple(userInfo, host, port)
    }

    private fun parseQuery(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split("&").mapNotNull {
            val idx = it.indexOf('=')
            if (idx < 0) null
            else URLDecoder.decode(it.substring(0, idx), "UTF-8") to
                 URLDecoder.decode(it.substring(idx + 1), "UTF-8")
        }.toMap()
    }

    private fun decodeFragment(raw: String?): String? =
        raw?.let { URLDecoder.decode(it, "UTF-8") }

    private fun decodeB64(input: String): String {
        var s = input.replace('-', '+').replace('_', '/')
        while (s.length % 4 != 0) s += "="
        return String(Base64.decode(s, Base64.DEFAULT))
    }
}
