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
            .background(Color(0xFF000000)) // Pure matte black like Termux
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
            items(lines.size) {
                Text(
                    text = lines[index],
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color(0xFFE0E0E0), // Crisp white/grey text
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Virtual Keys Toolbar (Termux Style)
        var isCtrl by remember { mutableStateOf(false) }
        var isAlt by remember { mutableStateOf(false) }

        val onKey: (String, String) -> Unit = { label, code ->
            val finalCode = if (isAlt && label != "Alt") "\u001B$code" else code
            
            if (label == "HomeDir") {
                viewModel.sendCommand("cd ${workingDir.absolutePath}")
            } else {
                viewModel.sendRaw(finalCode)
            }
            
            if (label != "Ctrl" && label != "Alt" && label != "Shift") {
                isCtrl = false
                isAlt = false
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF121212),
            tonalElevation = 4.dp
        ) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                item { VirtualKey("ESC", onClick = { onKey("Esc", "\u001B") }) }
                item { VirtualKey("TAB", onClick = { onKey("Tab", "\u0009") }) }
                item { VirtualKey("CTRL", isSelected = isCtrl, onClick = { isCtrl = !isCtrl }) }
                item { VirtualKey("ALT", isSelected = isAlt, onClick = { isAlt = !isAlt }) }
                item { VirtualKey("-", onClick = { onKey("-", "-") }) }
                // Arrows
                item { VirtualKey("▲", onClick = { onKey("Up", "\u001B[A") }) }
                item { VirtualKey("▼", onClick = { onKey("Down", "\u001B[B") }) }
                item { VirtualKey("◀", onClick = { onKey("Left", "\u001B[D") }) }
                item { VirtualKey("▶", onClick = { onKey("Right", "\u001B[C") }) }
            }
        }
        
        // Input Area (Compact)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF000000),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF333333))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "$ ",
                    color = Color(0xFF80CBC4), // Termux Teal prompt
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF80CBC4)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (input.isNotEmpty()) {
                            viewModel.sendCommand(input)
                            input = ""
                        }
                    }),
                    modifier = Modifier.weight(1f).padding(start = 2.dp)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        color = if (isSelected) Color(0xFF80CBC4).copy(alpha = 0.3f) else Color(0xFF212121),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4)) else null,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFF80CBC4) else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
