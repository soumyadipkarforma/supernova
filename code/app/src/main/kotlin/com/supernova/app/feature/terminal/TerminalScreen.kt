package com.supernova.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.app.IDEViewModel

@Composable
fun TerminalScreen(viewModel: IDEViewModel) {
    val output by viewModel.terminalOutput.collectAsState()
    var inputCmd by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            listState.animateScrollToItem(output.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(8.dp)
        ) {
            items(output) { line ->
                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE0E0E0),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Divider(color = Color.DarkGray)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", color = Color(0xFF4CAF50), fontFamily = FontFamily.Monospace, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            BasicTextField(
                value = inputCmd,
                onValueChange = { inputCmd = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.weight(1f).padding(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputCmd.isNotBlank()) {
                        viewModel.sendTerminalCommand(inputCmd)
                        inputCmd = ""
                    }
                })
            )
        }
    }
}
