package com.tripgenie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Brand Colors ────────────────────────────────────────────────────
val PrimaryBlue = Color(0xFF2D4DE0)
val AccentTeal = Color(0xFF1EB8A6)
val BackgroundStart = Color(0xFFF0F4FF)
val BackgroundEnd = Color(0xFFE8F5F3)
val CardWhite = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val TextHint = Color(0xFF9CA3AF)
val BorderLight = Color(0xFFE5E7EB)
val ErrorRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val WarningOrange = Color(0xFFF97316)
val BadgeBg = Color(0xFFEBF4FF)
val BadgeText = Color(0xFF2563EB)

// ─── Chart Colors ────────────────────────────────────────────────────
val ChartAccommodation = Color(0xFF2D4DE0)
val ChartActivities = Color(0xFF1EB8A6)
val ChartFood = Color(0xFFF97316)
val ChartTransport = Color(0xFF8B5CF6)

// ─── Day Route Colors ────────────────────────────────────────────────
val dayColors = listOf(
    Color(0xFF2D4DE0), Color(0xFF1EB8A6), Color(0xFFF97316),
    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6)
)

// ─── Gradients ───────────────────────────────────────────────────────
val AppBackgroundGradient = Brush.verticalGradient(
    colors = listOf(BackgroundStart, BackgroundEnd)
)

val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryBlue, AccentTeal)
)

val NavBarGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1A237E), PrimaryBlue)
)

val HeroGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A237E), PrimaryBlue, AccentTeal)
)

// ─── Color Scheme ────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = AccentTeal,
    background = BackgroundStart,
    surface = CardWhite,
    onPrimary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

// ─── Typography ──────────────────────────────────────────────────────
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal, color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp, fontWeight = FontWeight.Normal, color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextSecondary
    )
)

// ─── Theme ───────────────────────────────────────────────────────────
@Composable
fun TripGenieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
