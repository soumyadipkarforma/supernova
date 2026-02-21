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
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutExpo),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Slightly longer for "best-in-class" feel
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
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
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "SUPERNOVA",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 12.sp,
                    color = Color.White
                ),
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Professional IDE for Android",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF80CBC4),
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.alpha(alpha)
            )
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "built with love by Soumyadip Karforma",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "v1.2",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}