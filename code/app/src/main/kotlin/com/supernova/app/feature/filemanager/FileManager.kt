package com.supernova.app.feature.filemanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.app.IDEViewModel
import com.supernova.app.core.fs.FileManager
import java.io.File

@Composable
fun FileManagerScreen(viewModel: IDEViewModel, onFileSelected: (File) -> Unit) {
    val files by viewModel.workspaceFiles.collectAsState()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showPlusOptions by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Determine if we're in the main workspace
    val isInMainWorkspace = FileManager.root.absolutePath == FileManager.currentDirectory.absolutePath

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with menu button
        TopAppBar(
            title = { Text("Workspace Explorer") },
            navigationIcon = {
                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                // Add more actions here if needed
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(files) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (file.isDirectory) {
                                    FileManager.currentDirectory = file
                                    FileManager.refreshFileSystem()
                                } else {
                                    onFileSelected(file)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .then(
                                if (selectedFile == file) {
                                    Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                                } else {
                                    Modifier
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (file.isDirectory) Color(0xFFF1C40F) else Color(0xFF3498DB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = file.name,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                selectedFile = file
                                showContextMenu = true
                            }
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                    }
                }
            }

            // Floating Plus Button
            FloatingActionButton(
                onClick = { showPlusOptions = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }

    // Plus button options dialog
    if (showPlusOptions) {
        PlusOptionsDialog(
            isInMainWorkspace = isInMainWorkspace,
            onDismiss = { showPlusOptions = false },
            onCreateProject = {
                showPlusOptions = false
                // Create a new project directory
                val projectName = "New Project ${System.currentTimeMillis()}"
                val newProjectDir = File(FileManager.currentDirectory, projectName)
                newProjectDir.mkdir()
                FileManager.refreshFileSystem()
            },
            onCreateFile = {
                showPlusOptions = false
                // Create a new file
                val fileName = "new_file.txt"
                val newFile = File(FileManager.currentDirectory, fileName)
                newFile.createNewFile()
                FileManager.refreshFileSystem()
            },
            onImportFile = {
                showPlusOptions = false
                // Import functionality would go here
            },
            onImportFolder = {
                showPlusOptions = false
                // Import folder functionality would go here
            },
            onCreateFolder = {
                showPlusOptions = false
                // Create a new folder
                val folderName = "New Folder ${System.currentTimeMillis()}"
                val newFolder = File(FileManager.currentDirectory, folderName)
                newFolder.mkdir()
                FileManager.refreshFileSystem()
            }
        )
    }

    // Context menu for selected file/folder
    if (showContextMenu && selectedFile != null) {
        ContextMenu(
            file = selectedFile!!,
            onDismiss = {
                showContextMenu = false
                selectedFile = null
            },
            onRename = { newName ->
                selectedFile?.let { file ->
                    val newFile = File(file.parentFile, newName)
                    file.renameTo(newFile)
                    FileManager.refreshFileSystem()
                    selectedFile = null
                }
            },
            onCompress = { compressionType ->
                selectedFile?.let { file ->
                    if (compressionType == "zip") {
                        val zipFile = File(file.parentFile, "${file.name}.zip")
                        FileManager.compressToZip(file, zipFile)
                    }
                    selectedFile = null
                }
            },
            onDelete = {
                selectedFile?.let { file ->
                    FileManager.deleteFile(file)
                    FileManager.refreshFileSystem()
                    selectedFile = null
                }
            }
        )
    }

    // Settings menu
    if (showSettingsMenu) {
        SettingsMenu(
            onDismiss = { showSettingsMenu = false },
            onThemeChange = { /* Handle theme change */ },
            onSettingsClick = { /* Handle settings click */ }
        )
    }
}

// Dialog for plus button options
@Composable
fun PlusOptionsDialog(
    isInMainWorkspace: Boolean,
    onDismiss: () -> Unit,
    onCreateProject: () -> Unit,
    onCreateFile: () -> Unit,
    onImportFile: () -> Unit,
    onImportFolder: () -> Unit,
    onCreateFolder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New") },
        text = {
            Column {
                if (isInMainWorkspace) {
                    ListItem(
                        headlineContent = { Text("New Project") },
                        leadingContent = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        modifier = Modifier.clickable { onCreateProject() }
                    )
                } else {
                    ListItem(
                        headlineContent = { Text("New Folder") },
                        leadingContent = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        modifier = Modifier.clickable { onCreateFolder() }
                    )
                }

                ListItem(
                    headlineContent = { Text("New File") },
                    leadingContent = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.clickable { onCreateFile() }
                )

                Divider()

                ListItem(
                    headlineContent = { Text("Import File") },
                    leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                    modifier = Modifier.clickable { onImportFile() }
                )

                ListItem(
                    headlineContent = { Text("Import Folder") },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { onImportFolder() }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Rename dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    file: File,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(file.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename ${if (file.isDirectory) "Folder" else "File"}") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != file.name) {
                        onRename(newName)
                    }
                    onDismiss()
                },
                enabled = newName.isNotBlank() && newName != file.name
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Context menu for file/folder operations
@Composable
fun ContextMenu(
    file: File,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onCompress: (String) -> Unit,  // "zip" or "gzip"
    onDelete: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    if (showRenameDialog) {
        RenameDialog(
            file = file,
            onRename = onRename,
            onDismiss = {
                showRenameDialog = false
                onDismiss()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Options for ${file.name}") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Rename") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showRenameDialog = true
                    }
                )

                ListItem(
                    headlineContent = { Text("Compress") },
                    leadingContent = { Icon(Icons.Default.Archive, contentDescription = null) },
                    modifier = Modifier.clickable {
                        // Show compression options
                        // For now, defaulting to ZIP
                        onCompress("zip")
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("Delete") },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDelete()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Settings menu
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenu(
    onDismiss: () -> Unit,
    onThemeChange: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("system") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Settings") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onSettingsClick()
                        onDismiss()
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedTheme,
                        onValueChange = { },
                        label = { Text("Theme") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Light") },
                            onClick = {
                                selectedTheme = "light"
                                onThemeChange("light")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark") },
                            onClick = {
                                selectedTheme = "dark"
                                onThemeChange("dark")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System") },
                            onClick = {
                                selectedTheme = "system"
                                onThemeChange("system")
                                expanded = false
                            }
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text("Storage") },
                    leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                    modifier = Modifier.clickable {
                        // Show storage info
                        onDismiss()
                    }
                )

                ListItem(
                    headlineContent = { Text("About") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable {
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {}
    )
}