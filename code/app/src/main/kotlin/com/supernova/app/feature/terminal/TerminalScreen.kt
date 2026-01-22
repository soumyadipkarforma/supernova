package com.supernova.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.termux.view.TerminalView
import java.io.File

@Composable
fun TerminalScreen(workingDir: File = File("/data/data/com.termux/files/home")) {
    val viewModel: TerminalViewModel = viewModel()
    val context = LocalContext.current
    
    val terminalView = remember { TerminalView(context, null) }
    
    val client = remember {
        object : DefaultTerminalViewClient() {
            override fun onTextChanged(changedSession: com.termux.terminal.TerminalSession) {
                terminalView.postInvalidate()
            }
        }
    }

    LaunchedEffect(terminalView) {
        terminalView.setTerminalViewClient(client)
        terminalView.setTextSize(40)
    }

    LaunchedEffect(workingDir) {
        viewModel.initSession(workingDir, client)
        terminalView.attachSession(viewModel.session)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { terminalView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )
    }
}
