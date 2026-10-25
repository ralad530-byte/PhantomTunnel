package com.shadow.vpnclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shadow.vpnclient.model.ServerConfig
import com.shadow.vpnclient.ui.theme.NeonCyan
import com.shadow.vpnclient.ui.theme.TextSecondary

@Composable
fun ServerCard(
    server: ServerConfig,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPing: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) NeonCyan else TextSecondary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(server.remark, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Row {
                    ProtocolBadge(server.protocol.name)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${server.address}:${server.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            LatencyPill(server.latencyMs, onClick = onPing)
        }
    }
}

@Composable
private fun ProtocolBadge(name: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = NeonCyan.copy(alpha = 0.15f)
    ) {
        Text(
            name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan
        )
    }
}

@Composable
private fun LatencyPill(latencyMs: Int?, onClick: () -> Unit) {
    val (text, color) = when {
        latencyMs == null -> "—" to TextSecondary
        latencyMs < 0 -> "timeout" to Color(0xFFFF5370)
        latencyMs < 150 -> "${latencyMs}ms" to Color(0xFF3DDC97)
        latencyMs < 400 -> "${latencyMs}ms" to Color(0xFFFFC857)
        else -> "${latencyMs}ms" to Color(0xFFFF5370)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
