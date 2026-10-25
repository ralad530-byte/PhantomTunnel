package com.shadow.vpnclient.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Measures raw TCP connect latency to a server's host:port — a reasonable,
 * protocol-agnostic proxy for "how fast is this server" without needing
 * the tunnel to already be up.
 */
object PingTester {
    suspend fun tcpPing(address: String, port: Int, timeoutMs: Int = 3000): Int = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, port), timeoutMs)
            }
            (System.currentTimeMillis() - start).toInt()
        } catch (e: Exception) {
            -1 // unreachable / timeout
        }
    }
}
