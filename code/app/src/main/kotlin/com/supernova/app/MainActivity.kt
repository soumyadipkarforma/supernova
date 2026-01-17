package com.supernova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.supernova.app.feature.browser.LocalBrowserScreen
import com.supernova.app.feature.checkinstall.TermuxCheckerScreen
import com.supernova.app.feature.checkinstall.isTermuxInstalled
import com.supernova.app.feature.editor.EditorScreen
import com.supernova.app.feature.filemanager.FileManagerScreen
import com.supernova.app.ui.theme.SupernovaTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SupernovaTheme {
                var isInstalled by remember { mutableStateOf(isTermuxInstalled(this)) }
                
                if (!isInstalled) {
                    TermuxCheckerScreen(onRetry = {
                        isInstalled = isTermuxInstalled(this)
                    })
                } else {
                    MainNavigation()
                }
            }
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
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination == "files",
                    onClick = { navController.navigate("files") },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                    label = { Text("Files") }
                )
                NavigationBarItem(
                    selected = currentDestination == "editor",
                    onClick = { navController.navigate("editor") },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Editor") },
                    label = { Text("Editor") }
                )
                NavigationBarItem(
                    selected = currentDestination == "browser",
                    onClick = { navController.navigate("browser") },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Browser") },
                    label = { Text("Browser") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "files",
            modifier = Modifier.padding(innerPadding)
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
            composable("browser") {
                LocalBrowserScreen()
            }
        }
    }
}
