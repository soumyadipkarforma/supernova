package com.supernova.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We should ensure JetBrains Mono is loaded or fallback gracefully
private val JetBrainsMono = FontFamily.Monospace

private val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val TermuxTeal = Color(0xFF80CBC4)
private val TermuxAmber = Color(0xFFFFCC80)
private val DeepBlack = Color(0xFF000000)
private val SurfaceGrey = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = TermuxTeal,
    secondary = TermuxAmber,
    tertiary = Color(0xFFB0BEC5),
    background = DeepBlack,
    surface = SurfaceGrey,
    onPrimary = DeepBlack,
    onSecondary = DeepBlack,
    onTertiary = DeepBlack,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = Color(0xFF424242)
)

@Composable
fun SupernovaTheme(
    darkTheme: Boolean = true, 
    content: @Composable () -> Unit
) {
    // We force dark theme for the "Best-in-class" terminal experience
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}