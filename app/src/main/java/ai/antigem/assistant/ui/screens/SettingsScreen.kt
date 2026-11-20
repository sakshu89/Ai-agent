package ai.antigem.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.antigem.assistant.ui.theme.*

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        section {
            Text("AI Servers", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Model Server Manager",
                subtitle = "Configure AI inference servers",
                icon = Icons.Default.Server,
                onClick = { /* open server manager */ }
            )
            SettingsRow(
                title = "Model Selector",
                subtitle = "Choose default model",
                icon = Icons.Default.Smartphone,
                onClick = { /* open model selector */ }
            )
        }

        section {
            Text("Memory & Personalization", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Memory Manager",
                subtitle = "View and edit stored memories",
                icon = Icons.Default.Memory,
                onClick = { /* open memory manager */ }
            )
        }

        section {
            Text("Automation", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Skill Manager",
                subtitle = "Discover and manage skills",
                icon = Icons.Default.Extensions,
                onClick = { /* open skill manager */ }
            )
            SettingsRow(
                title = "App Integrations",
                subtitle = "Connected applications",
                icon = Icons.Default.Apps,
                onClick = { /* open app integrations */ }
            )
            SettingsRow(
                title = "Automation Policies",
                subtitle = "Trusted actions and confirmations",
                icon = Icons.Default.Policy,
                onClick = { /* open automation policies */ }
            )
        }

        section {
            Text("Permissions", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Permission Manager",
                subtitle = "Grant and review permissions",
                icon = Icons.Default.Lock,
                onClick = { /* open permission manager */ }
            )
        }

        section {
            Text("Voice", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Voice Settings",
                subtitle = "Speech recognition and synthesis",
                icon = Icons.Default.Mic,
                onClick = { /* open voice settings */ }
            )
        }

        section {
            Text("Backup & Safety", style = MaterialTheme.typography.titleMedium)
            SettingsRow(
                title = "Backup & Restore",
                subtitle = "Export and import data",
                icon = Icons.Default.Backup,
                onClick = { /* open backup */ }
            )
            SettingsRow(
                title = "Diagnostics",
                subtitle = "System health and logs",
                icon = Icons.Default.BugReport,
                onClick = { /* open diagnostics */ }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
