package com.supernova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.supernova.app.ui.theme.SupernovaTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SupernovaTheme {
                var isInstalled by remember { mutableStateOf(isTermuxInstalled(this)) }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
            composable("browser") {
                LocalBrowserScreen()
            }
        }
    }
}
