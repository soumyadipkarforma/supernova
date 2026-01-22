package com.supernova.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.supernova.app.feature.browser.LocalBrowserScreen
import com.supernova.app.feature.checkinstall.TermuxCheckerScreen
import com.supernova.app.feature.checkinstall.isTermuxInstalled
import com.supernova.app.feature.editor.EditorScreen
import com.supernova.app.feature.filemanager.FileManagerScreen
import com.supernova.app.feature.terminal.TerminalScreen
import com.supernova.app.ui.theme.SupernovaTheme
import java.io.File

import com.supernova.app.feature.splash.AppSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SupernovaTheme {
                var showSplash by remember { mutableStateOf(true) }
                var isInstalled by remember { mutableStateOf(true) } // Default to true to avoid flicker
                var hasStoragePermission by remember { mutableStateOf(true) } // Default to true to avoid flicker
                
                LaunchedEffect(Unit) {
                    try {
                        isInstalled = isTermuxInstalled(this@MainActivity)
                        hasStoragePermission = checkStoragePermission()
                    } catch (e: Exception) {
                        // If checks fail, we might want to default to something or log it
                        isInstalled = false 
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        AppSplashScreen(onFinished = { showSplash = false })
                    } else if (!hasStoragePermission) {
                        StoragePermissionScreen(
                            onPermissionGranted = { hasStoragePermission = checkStoragePermission() },
                            onRequestPermission = { requestStoragePermission() }
                        )
                    } else if (!isInstalled) {
                        TermuxCheckerScreen(onRetry = {
                            isInstalled = isTermuxInstalled(this@MainActivity)
                        })
                    } else {
                        MainNavigation()
                    }
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // For simplicity in this CLI update, assuming old permission is handled or manually granted. Real app should use ActivityResultContracts.
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 100)
            }
        }
    }
}

@Composable
fun StoragePermissionScreen(onPermissionGranted: () -> Unit, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Storage Permission Needed", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Supernova needs access to your device storage to create the workspace folder and manage files.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Polling button for user convenience after they return
        OutlinedButton(onClick = onPermissionGranted) {
            Text("I have granted permission")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    
    // Global State for Editor
    var activeFile by remember { mutableStateOf<File?>(null) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("files", Icons.Default.Folder, "Files"),
                    Triple("editor", Icons.Default.Code, "Editor"),
                    Triple("terminal", Icons.Default.Terminal, "Terminal"),
                    Triple("browser", Icons.Default.Language, "Browser")
                )
                
                items.forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentDestination == route,
                        onClick = { 
                            if (currentDestination != route) {
                                navController.navigate(route) {
                                    popUpTo("files") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "files",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            composable("files") {
                FileManagerScreen(onFileSelected = { file ->
                    activeFile = file
                    navController.navigate("editor")
                })
            }
            composable("editor") {
                EditorScreen(file = activeFile)
            }
            composable("terminal") {
                // Use Shared Storage workspace
                val workspace = File(Environment.getExternalStorageDirectory(), "workspace").apply { if (!exists()) mkdirs() }
                TerminalScreen(workingDir = workspace)
            }
            composable("browser") {
                LocalBrowserScreen()
            }
        }
    }
}
