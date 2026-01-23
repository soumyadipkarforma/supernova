package com.supernova.app.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.app.IDEViewModel
import java.io.File

@Composable
fun EditorScreen(viewModel: IDEViewModel) {
    val content by viewModel.activeFileContent.collectAsState()
    val activeFile by viewModel.currentFile.collectAsState()

    if (activeFile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Select a file to edit", color = Color.Gray)
        }
        return
    }

    val annotatedString = buildAnnotatedString {
        append(content)
        val keywords = listOf("fun", "val", "var", "import", "package", "class", "interface", "object", "if", "else", "for", "while", "return", "true", "false", "null")
        val regex = "\\b(${keywords.joinToString("|")})\\b".toRegex()
        
        regex.findAll(content).forEach { match ->
            addStyle(
                style = SpanStyle(color = Color(0xFFBD93F9), fontWeight = FontWeight.Bold),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2E))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF282A36)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(activeFile?.name ?: "", color = Color.White, fontSize = 14.sp)
            Button(
                onClick = { viewModel.saveFile() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B), contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Save", fontSize = 12.sp)
            }
        }

        BasicTextField(
            value = TextFieldValue(annotatedString),
            onValueChange = { viewModel.activeFileContent.value = it.text },
            modifier = Modifier.fillMaxSize().padding(12.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFF8F8F2),
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(Color.White)
        )
    }
}
