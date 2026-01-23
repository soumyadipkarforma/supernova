package com.supernova.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@Composable
fun TerminalScreen(workingDir: File) {
    val viewModel: TerminalViewModel = viewModel()
    val output by viewModel.terminalOutput.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val lines = remember(output) { output.split("\n") }

    LaunchedEffect(workingDir) {
        viewModel.initSession(workingDir)
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Terminal Output Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(lines.size) { index ->
                Text(
                    text = lines[index],
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = Color(0xFFD4D4D4)
                    )
                )
            }
        }

        // Virtual Keys Toolbar
        var isCtrl by remember { mutableStateOf(false) }
        var isAlt by remember { mutableStateOf(false) }

        // Helper for keys
        val onKey: (String, String) -> Unit = { label, code ->
            // Simple logic: if Alt is active, prefix with Esc
            val finalCode = if (isAlt && label != "Alt") "\u001B$code" else code
            // Note: Ctrl handling is complex for all keys, implemented basically for now
            // For now, modifiers just toggle visual state or prefix
            
            if (label == "HomeDir") {
                viewModel.sendCommand("cd ${workingDir.absolutePath}")
            } else {
                viewModel.sendRaw(finalCode)
            }
            
            // Reset modifiers after non-modifier key
            if (label != "Ctrl" && label != "Alt" && label != "Shift") {
                isCtrl = false
                isAlt = false
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF252526),
            tonalElevation = 2.dp
        ) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item {
                    VirtualKey("Esc", onClick = { onKey("Esc", "\u001B") })
                }
                item {
                    VirtualKey("Tab", onClick = { onKey("Tab", "\u0009") })
                }
                item {
                    VirtualKey("Ctrl", isSelected = isCtrl, onClick = { isCtrl = !isCtrl })
                }
                item {
                    VirtualKey("Alt", isSelected = isAlt, onClick = { isAlt = !isAlt })
                }
                item {
                    VirtualKey("HomeDir", onClick = { onKey("HomeDir", "") })
                }
                item {
                    VirtualKey("Shells: 1", onClick = { /* TODO: Show menu */ })
                }
                // Arrows
                item { VirtualKey("▲", onClick = { onKey("Up", "\u001B[A") }) }
                item { VirtualKey("▼", onClick = { onKey("Down", "\u001B[B") }) }
                item { VirtualKey("◀", onClick = { onKey("Left", "\u001B[D") }) }
                item { VirtualKey("▶", onClick = { onKey("Right", "\u001B[C") }) }
                
                // Special
                item { VirtualKey("Home", onClick = { onKey("Home", "\u001B[H") }) }
                item { VirtualKey("End", onClick = { onKey("End", "\u001B[F") }) }
                item { VirtualKey("PgUp", onClick = { onKey("PgUp", "\u001B[5~") }) }
                item { VirtualKey("PgDn", onClick = { onKey("PgDn", "\u001B[6~") }) }
            }
        }
        
        // Input Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2D2D2D),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "$ ",
                    color = Color(0xFF4CAF50),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        viewModel.sendCommand(input)
                        input = ""
                    }),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun VirtualKey(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) Color(0xFF007ACC) else Color(0xFF3C3C3C),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}