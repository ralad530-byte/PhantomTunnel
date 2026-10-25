package com.shadow.vpnclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenSubscriptions: () -> Unit) {
    var bypassLan by remember { mutableStateOf(true) }
    var perAppProxy by remember { mutableStateOf(false) }
    var autoConnect by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("الإعدادات") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            SettingsRow("إدارة الاشتراكات", "إضافة/تحديث روابط الاشتراك", onClick = onOpenSubscriptions)

            Divider(Modifier.padding(vertical = 8.dp))

            SwitchRow("تجاوز الشبكة المحلية (LAN)", "إبقاء الاتصال بالأجهزة المحلية خارج النفق", bypassLan) { bypassLan = it }
            SwitchRow("بروكسي لكل تطبيق", "اختيار تطبيقات معينة فقط لتمر عبر VPN", perAppProxy) { perAppProxy = it }
            SwitchRow("اتصال تلقائي عند بدء التشغيل", "الاتصال بآخر سيرفر مستخدم تلقائياً", autoConnect) { autoConnect = it }

            Divider(Modifier.padding(vertical = 8.dp))

            Text("الإصدار 1.0.0", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier.clickableRow(onClick)
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) }
    )
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
