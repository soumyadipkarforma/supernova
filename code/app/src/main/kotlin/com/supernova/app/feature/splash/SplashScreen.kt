package com.supernova.app.feature.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernova.app.R
import kotlinx.coroutines.delay

@Composable
fun AppSplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // Show splash for 2 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D2B), // Deep space blue
                        Color(0xFF1A1A40)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_supernova_logo),
                contentDescription = "Supernova Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale)
                    .alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SUPERNOVA",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    color = Color.White
                ),
                modifier = Modifier.alpha(alpha)
            )
            
            Text(
                text = "Mobile Development Reimagined",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(alpha)
            )
        }
        
        Text(
            text = "v1.1",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

// Helper for OvershootInterpolator in Compose
private fun OvershootInterpolator() = android.view.animation.OvershootInterpolator(2f)
private fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }
