package ai.antigem.assistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),
    primaryContainer = Color(0xFF4F46E5),
    secondary = Color(0xFF0EA5E9),
    secondaryContainer = Color(0xFF155E75),
    tertiary = Color(0xFF10B981),
    error = Color(0xFFF87171),
    background = Color(0xFF09090B),
    surface = Color(0xFF1F1F1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color.White,
    onBackground = Color(0xFFFAFAFA)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),
    primaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF0EA5E9),
    secondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFF10B981),
    error = Color(0xFFEF4444),
    background = Color.White,
    surface = Color(0xFFFAFAFA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color(0xFF1A1A1A),
    onBackground = Color(0xFF1A1A1A)
)

@Composable
fun AntigemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
            titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            titleSmall = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            bodyLarge = TextStyle(fontSize = 16.sp),
            bodyMedium = TextStyle(fontSize = 14.sp),
            bodySmall = TextStyle(fontSize = 12.sp),
            labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        ),
        content = content
    )
}
