package com.supernova.app.feature.filemanager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(onFileSelected: (File) -> Unit) {
    val context = LocalContext.current
    val workspaceRoot = remember { 
        File(context.getExternalFilesDir(null), "workspace").apply { if (!exists()) mkdirs() }
    }
    
    var currentDir by remember { mutableStateOf(workspaceRoot) }
    var files by remember { mutableStateOf(currentDir.listFiles()?.toList() ?: emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentDir) {
        files = currentDir.listFiles()?.toList()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            currentDir.name.ifEmpty { "Workspace" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            currentDir.absolutePath.replace(workspaceRoot.parent ?: "", ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    if (currentDir != workspaceRoot) {
                        IconButton(onClick = { currentDir = currentDir.parentFile ?: workspaceRoot }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "New File")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files) { file ->
                FileItem(file = file, onClick = {
                    if (file.isDirectory) {
                        currentDir = file
                    } else {
                        onFileSelected(file)
                    }
                })
            }
        }

        if (showCreateDialog) {
            var fileName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New") },
                text = {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        placeholder = { Text("e.g., main.py or src/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (fileName.isNotBlank()) {
                            val newFile = File(currentDir, fileName)
                            if (fileName.endsWith("/")) {
                                newFile.mkdirs()
                            } else {
                                newFile.createNewFile()
                            }
                            files = currentDir.listFiles()?.toList() ?: emptyList()
                            showCreateDialog = false
                        }
                    }) { Text("Create") }
                }
            )
        }
    }
}

@Composable
fun FileItem(file: File, onClick: () -> Unit) {
    val (icon, color) = getFileIconAndColor(file)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (file.isDirectory) "Folder" else "${file.length()} bytes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            if (file.isDirectory) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun getFileIconAndColor(file: File): Pair<ImageVector, Color> {
    if (file.isDirectory) return Icons.Default.Folder to Color(0xFFFFCA28)
    
    return when (file.extension.lowercase()) {
        "py" -> Icons.Default.Description to Color(0xFF3776AB)
        "js", "ts" -> Icons.Default.Javascript to Color(0xFFF7DF1E)
        "kt" -> Icons.Default.Code to Color(0xFF7F52FF)
        "html", "htm" -> Icons.Default.Html to Color(0xFFE34F26)
        "css" -> Icons.Default.Css to Color(0xFF1572B6)
        "json" -> Icons.Default.Settings to Color(0xFFFF9800)
        else -> Icons.Default.Description to Color(0xFF90A4AE)
    }
}
