package com.shadow.vpnclient.core

/**
 * Thin wrapper around the native Xray engine.
 *
 * INTEGRATION POINT:
 * Build `libv2ray.aar` from https://github.com/2dust/AndroidLibXrayLite
 * (gomobile bind ./ -target=android -o libv2ray.aar), drop it in app/libs/,
 * then uncomment the dependency in app/build.gradle.kts.
 *
 * That library exposes a Go-generated class roughly shaped like:
 *   libv2ray.Libv2ray.newV2RayPoint(callback: V2RayVPNServiceSupportsSet, useProxy: Boolean): V2RayPoint
 * with V2RayPoint.configureFileContent, .runLoop(), .stopLoop(), .queryStats(...).
 *
 * This class isolates that boundary so the rest of the app (ViewModel, UI,
 * VpnConnectionService) never touches the native API directly — only this file
 * needs to change when you wire in the real .aar.
 */
object XrayCoreManager {

    private var running = false

    interface StatsCallback {
        fun onStats(uplinkBytes: Long, downlinkBytes: Long)
    }

    /**
     * Starts the core with a fully-built Xray JSON config (see ConfigBuilder).
     * tunFd is the file descriptor of the TUN interface created by VpnConnectionService;
     * the core's tun2socks layer reads/writes packets on it and forwards them
     * through the socks inbound defined in the JSON config.
     */
    fun start(configJson: String, tunFd: Int, statsCallback: StatsCallback? = null): Boolean {
        if (running) return true
        // TODO real integration:
        // val point = Libv2ray.newV2RayPoint(callbackImpl, false)
        // point.configureFileContent = configJson
        // point.vpnSupportReady(tunFd)
        // point.runLoop(false)
        running = true
        return true
    }

    fun stop() {
        if (!running) return
        // TODO: point.stopLoop()
        running = false
    }

    fun isRunning(): Boolean = running

    /** Returns cumulative uplink/downlink bytes since start(), for the traffic UI. */
    fun queryStats(): Pair<Long, Long> {
        // TODO: return point.queryStats("proxy", "uplink") to point.queryStats("proxy", "downlink")
        return 0L to 0L
    }
}
