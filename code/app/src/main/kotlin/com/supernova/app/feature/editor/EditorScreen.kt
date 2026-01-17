package com.supernova.app.feature.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.app.feature.terminal.TermuxBridge
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(file: File?) {
    val context = LocalContext.current
    var code by remember(file) { mutableStateOf(file?.readText() ?: "") }
    val highlighter = remember { CodeHighlighter() }

    if (file == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Select a file from the manager to start coding")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name) },
                actions = {
                    IconButton(onClick = {
                        file.writeText(code)
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                    IconButton(onClick = {
                        file.writeText(code)
                        val cmd = when (file.extension) {
                            "py" -> "python \"${file.name}\""
                            "js" -> "node \"${file.name}\""
                            "sh" -> "bash \"${file.name}\""
                            else -> "echo 'Executing ${file.name}...'"
                        }
                        TermuxBridge.runCommand(context, cmd, file.parent ?: "")
                    }) {
                        Icon(Icons.Default.PlayArrow, "Run in Termux")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            BasicTextField(
                value = code,
                onValueChange = { code = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = highlighter
            )
        }
    }
}
