package com.sacredpath.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sacredpath.app.R

// ── Brand Colors ──────────────────────────────────────────────────────────────
object SacredColors {
    val Burgundy       = Color(0xFF7B2D3E)
    val BurgundyLight  = Color(0xFFA84D60)
    val BurgundyDark   = Color(0xFF5A1E2C)
    val Gold           = Color(0xFFC9A84C)
    val GoldLight      = Color(0xFFE2C878)
    val Olive          = Color(0xFF8A9A5B)
    val OliveLight     = Color(0xFFA8B878)

    // Highlights
    val HighlightGold   = Color(0xFFF9E4A0)
    val HighlightPink   = Color(0xFFF9C0CC)
    val HighlightGreen  = Color(0xFFC6E8C6)
    val HighlightBlue   = Color(0xFFB8D8F0)
    val HighlightPurple = Color(0xFFD4C0F0)

    // Status
    val Success = Color(0xFF5C8A4A)
    val Error   = Color(0xFFC0392B)
    val Warning = Color(0xFFC9A84C)
}

// ── Light Theme ───────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = SacredColors.Burgundy,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFF2E4E7),
    secondary        = SacredColors.Gold,
    onSecondary      = Color.White,
    tertiary         = SacredColors.Olive,
    background       = Color(0xFFFAF6EF),
    onBackground     = Color(0xFF2E1B0E),
    surface          = Color.White,
    onSurface        = Color(0xFF2E1B0E),
    surfaceVariant   = Color(0xFFF2EDE4),
    outline          = Color(0xFFE0D8CC),
    error            = SacredColors.Error,
)

// ── Dark Theme ────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = SacredColors.BurgundyLight,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF3A1520),
    secondary        = SacredColors.Gold,
    onSecondary      = Color(0xFF0F1B2D),
    tertiary         = SacredColors.Olive,
    background       = Color(0xFF0F1B2D),
    onBackground     = Color(0xFFE8DFD0),
    surface          = Color(0xFF1A2840),
    onSurface        = Color(0xFFE8DFD0),
    surfaceVariant   = Color(0xFF162236),
    outline          = Color(0xFF2A3F5F),
    error            = SacredColors.Error,
)

// ── Parchment Theme ───────────────────────────────────────────────────────────
private val ParchmentColorScheme = lightColorScheme(
    primary          = SacredColors.Burgundy,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8D8B8),
    secondary        = Color(0xFFA07C2A),
    onSecondary      = Color.White,
    tertiary         = Color(0xFF6A7A3E),
    background       = Color(0xFFF4EDD8),
    onBackground     = Color(0xFF3A2010),
    surface          = Color(0xFFFBF5E6),
    onSurface        = Color(0xFF3A2010),
    surfaceVariant   = Color(0xFFEDE5CE),
    outline          = Color(0xFFC8B898),
    error            = SacredColors.Error,
)

// ── Night Theme ───────────────────────────────────────────────────────────────
private val NightColorScheme = darkColorScheme(
    primary          = Color(0xFF6B1E2E),
    onPrimary        = Color(0xFFF0E8D8),
    primaryContainer = Color(0xFF0C0810),
    secondary        = Color(0xFF8A6830),
    onSecondary      = Color(0xFFC8B898),
    tertiary         = Color(0xFF5A6A3A),
    background       = Color(0xFF080C10),
    onBackground     = Color(0xFFC8B898),
    surface          = Color(0xFF101820),
    onSurface        = Color(0xFFC8B898),
    surfaceVariant   = Color(0xFF0C1218),
    outline          = Color(0xFF1A2530),
    error            = SacredColors.Error,
)

// ── Font Families ─────────────────────────────────────────────────────────────
// Fonts must be placed in res/font/ — see README for download instructions
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold,    FontWeight.Bold),
    Font(R.font.playfair_display_italic,  FontWeight.Normal, FontStyle.Italic),
)

val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_bold,    FontWeight.Bold),
    Font(R.font.lora_italic,  FontWeight.Normal, FontStyle.Italic),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium,  FontWeight.Medium),
    Font(R.font.inter_bold,    FontWeight.Bold),
)

// ── Typography ────────────────────────────────────────────────────────────────
val SacredTypography = Typography(
    displayLarge  = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 42.sp),
    displayMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 26.sp, lineHeight = 34.sp),
    displaySmall  = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold,   fontSize = 20.sp, lineHeight = 28.sp),
    headlineMedium= TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    bodyLarge     = TextStyle(fontFamily = Lora,            fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
    bodyMedium    = TextStyle(fontFamily = Lora,            fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 26.sp),
    bodySmall     = TextStyle(fontFamily = Lora,            fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    labelLarge    = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelMedium   = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Normal, fontSize = 12.sp),
)

// ── Theme Composable ──────────────────────────────────────────────────────────
@Composable
fun SacredPathTheme(
    themeMode: String = "light",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "dark"      -> DarkColorScheme
        "parchment" -> ParchmentColorScheme
        "night"     -> NightColorScheme
        else        -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SacredTypography,
        content     = content
    )
}
