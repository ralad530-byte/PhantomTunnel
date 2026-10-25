package com.shadow.vpnclient.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.shadow.vpnclient.MainActivity
import com.shadow.vpnclient.R
import com.shadow.vpnclient.model.ServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VpnConnectionService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.shadow.vpnclient.CONNECT"
        const val ACTION_DISCONNECT = "com.shadow.vpnclient.DISCONNECT"
        const val EXTRA_CONFIG_JSON = "extra_config_json"
        const val EXTRA_BYPASSED_APPS = "extra_bypassed_apps" // package names excluded from tunnel
        private const val NOTIF_CHANNEL = "vpn_status"
        private const val NOTIF_ID = 1

        var isConnected: Boolean = false
            private set
    }

    private var tunInterface: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                disconnect()
                return START_NOT_STICKY
            }
            ACTION_CONNECT -> {
                val configJson = intent.getStringExtra(EXTRA_CONFIG_JSON) ?: return START_NOT_STICKY
                val bypassedApps = intent.getStringArrayListExtra(EXTRA_BYPASSED_APPS) ?: arrayListOf()
                connect(configJson, bypassedApps)
            }
        }
        return START_STICKY
    }

    private fun connect(configJson: String, bypassedApps: List<String>) {
        startForeground(NOTIF_ID, buildNotification("جارٍ الاتصال..."))

        val builder = Builder()
            .setSession("Shadow VPN")
            .setMtu(1500)
            .addAddress("10.10.14.1", 30)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.addRoute("::", 0)
        }

        // Per-app routing: exclude chosen apps from the tunnel (they go direct).
        for (pkg in bypassedApps) {
            try { builder.addDisallowedApplication(pkg) } catch (_: Exception) {}
        }

        builder.setBlocking(false)

        tunInterface = builder.establish()
        val fd = tunInterface?.fd ?: run {
            stopSelf()
            return
        }

        scope.launch {
            val ok = XrayCoreManager.start(configJson, fd)
            if (ok) {
                isConnected = true
                updateNotification("متصل \u2022 Shadow VPN")
            } else {
                disconnect()
            }
        }
    }

    private fun disconnect() {
        XrayCoreManager.stop()
        isConnected = false
        tunInterface?.close()
        tunInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onRevoke() {
        disconnect()
        super.onRevoke()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        val mgr = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(NOTIF_CHANNEL, "VPN Status", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Shadow VPN")
            .setContentText(text)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val mgr = getSystemService(NotificationManager::class.java)
        mgr.notify(NOTIF_ID, buildNotification(text))
    }
}
