package com.shadow.vpnclient.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadow.vpnclient.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val subs by viewModel.subscriptions.collectAsState()
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("الاشتراكات") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("اسم الاشتراك") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = url, onValueChange = { url = it },
                label = { Text("رابط الاشتراك") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && url.isNotBlank()) {
                        viewModel.addSubscription(name.trim(), url.trim())
                        name = ""; url = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("إضافة اشتراك") }

            Spacer(Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subs, key = { it.id }) { sub ->
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                Text(sub.url, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            IconButton(onClick = { viewModel.refreshSubscription(sub) }) {
                                Icon(Icons.Filled.Refresh, contentDescription = "تحديث")
                            }
                        }
                    }
                }
            }
        }
    }
}
