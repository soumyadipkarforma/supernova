package com.supernova.app.feature.filemanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
            TopAppBar(
                title = { Text(currentDir.name.ifEmpty { "Workspace" }) },
                navigationIcon = {
                    if (currentDir != workspaceRoot) {
                        IconButton(onClick = { currentDir = currentDir.parentFile ?: workspaceRoot }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "New File")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(files) { file ->
                ListItem(
                    headlineContent = { Text(file.name) },
                    supportingContent = { Text(if (file.isDirectory) "Folder" else "${file.length()} bytes") },
                    leadingContent = {
                        Icon(
                            if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        if (file.isDirectory) {
                            currentDir = file
                        } else {
                            onFileSelected(file)
                        }
                    }
                )
            }
        }

        if (showCreateDialog) {
            var fileName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New File") },
                text = {
                    TextField(value = fileName, onValueChange = { fileName = it }, placeholder = { Text("index.py") })
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (fileName.isNotBlank()) {
                            File(currentDir, fileName).createNewFile()
                            files = currentDir.listFiles()?.toList() ?: emptyList()
                            showCreateDialog = false
                        }
                    }) { Text("Create") }
                }
            )
        }
    }
}
