package com.shadow.vpnclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.vpnclient.ui.MainViewModel

/**
 * Unified "add server" screen: paste a share-link (vmess/vless/trojan/ss/
 * hysteria2/tuic) or scan a QR code. Manual field-by-field entry can be
 * added as a second tab using ServerConfig directly if needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    viewModel: MainViewModel,
    onDone: () -> Unit,
    onScanQr: () -> Unit
) {
    var link by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("إضافة سيرفر") }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("الصق رابط المشاركة (vmess:// , vless:// , trojan:// , ss:// , hysteria2:// , tuic://)")

            OutlinedTextField(
                value = link,
                onValueChange = { link = it; error = null },
                label = { Text("رابط السيرفر") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onScanQr) { Text("مسح QR") }
                Button(onClick = {
                    val ok = viewModel.addServerFromLink(link.trim())
                    if (ok) onDone() else error = "تعذر فهم الرابط — تحقق من الصيغة"
                }) { Text("إضافة") }
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text(
                "ملاحظة: يمكنك أيضاً لصق عدة روابط (كل رابط بسطر) لإضافتها دفعة واحدة من شاشة الاشتراكات.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
