package ai.antigem.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Common spacing values
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object BorderRadius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val full = 500.dp
}

// Status colors
object StatusColor {
    val online = Color(0xFF10B981)
    val offline = Color(0xFFEF4444)
    val connecting = Color(0xFFF59E0B)
    val unknown = Color(0xFF9CA3AF)
}

@Composable
fun contentColor() = MaterialTheme.colorScheme.onSurface
