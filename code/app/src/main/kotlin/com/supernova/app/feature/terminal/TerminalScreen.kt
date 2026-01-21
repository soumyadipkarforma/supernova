package com.supernova.app.feature.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@Composable
fun TerminalScreen(workingDir: File = File("/storage/emulated/0")) {
    val viewModel: TerminalViewModel = viewModel()
    val output by viewModel.terminalOutput.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val lines = remember(output) { output.split("\n") }

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
