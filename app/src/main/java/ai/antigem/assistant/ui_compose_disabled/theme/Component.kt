package ai.antigem.assistant.ui.theme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

@Composable
fun cardModifier() = Modifier
    .background(MaterialTheme.colorScheme.surface)
    .padding(Spacing.m)

@Composable
fun chipModifier() = Modifier
    .background(MaterialTheme.colorScheme.surfaceVariant)
    .padding(horizontal = Spacing.s, vertical = Spacing.xs)
