package com.shadow.vpnclient.util

import com.shadow.vpnclient.core.XrayCoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

data class TrafficSample(val uplinkBps: Long, val downlinkBps: Long, val totalUp: Long, val totalDown: Long)

/**
 * Polls XrayCoreManager.queryStats() periodically and emits instantaneous
 * throughput (bytes/sec) for the connection screen's live speed indicator.
 */
object TrafficStatsMonitor {
    fun stream(intervalMs: Long = 1000) = flow {
        var (lastUp, lastDown) = XrayCoreManager.queryStats()
        while (true) {
            delay(intervalMs)
            val (up, down) = XrayCoreManager.queryStats()
            emit(TrafficSample(
                uplinkBps = ((up - lastUp) * 1000 / intervalMs).coerceAtLeast(0),
                downlinkBps = ((down - lastDown) * 1000 / intervalMs).coerceAtLeast(0),
                totalUp = up,
                totalDown = down
            ))
            lastUp = up
            lastDown = down
        }
    }
}
