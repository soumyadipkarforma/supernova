package com.supernova.app.feature.browser

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@Composable
fun LocalBrowserScreen() {
    var url by remember { mutableStateOf("http://127.0.0.1:8080") }
    var activePorts by remember { mutableStateOf<List<Int>>(emptyList()) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        activePorts = PortScanner.scanLocalPorts()
    }

    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.weight(1f),
                label = { Text("URL") },
                singleLine = true
            )
            IconButton(onClick = { 
                webViewRef?.reload()
                // Re-scan ports using the composable coroutine scope
                coroutineScope.launch {
                    activePorts = PortScanner.scanLocalPorts()
                }
            }) {
                Icon(Icons.Default.Refresh, "Reload")
            }
        }

        if (activePorts.isNotEmpty()) {
            Text("Active Ports:", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.labelSmall)
            LazyRow(modifier = Modifier.padding(8.dp)) {
                items(activePorts) { port ->
                    AssistChip(
                        onClick = { url = "http://127.0.0.1:$port" },
                        label = { Text(":$port") },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { view ->
                if (view.url != url) {
                    view.loadUrl(url)
                }
                // keep reference updated in case the view instance changes
                webViewRef = view
            },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}