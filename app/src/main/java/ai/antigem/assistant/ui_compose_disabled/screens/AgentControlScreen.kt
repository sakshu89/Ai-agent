package ai.antigem.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.antigem.assistant.ui.theme.*

@Composable
fun AgentControlScreen() {
    var isTaskRunning by remember { mutableStateOf(false) }
    var currentTask by remember { mutableStateOf("No active task") }
    var progress by remember { mutableStateOf(0f) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Agent Control", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Status card
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    val statusColor = if (isTaskRunning) StatusColor.connecting else StatusColor.online
                    Box(modifier = Modifier.size(8.dp)
                        .background(statusColor))
                    Text(if (isTaskRunning) "Running" else "Idle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Current task: $currentTask", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* start task */ },
                enabled = !isTaskRunning
            ) {
                Text("Start Task")
            }
            Button(
                onClick = { /* pause task */ },
                enabled = isTaskRunning
            ) {
                Text("Pause")
            }
            Button(
                onClick = { /* resume task */ },
                enabled = isTaskRunning
            ) {
                Text("Resume")
            }
            Button(
                onClick = { /* stop task */ },
                enabled = isTaskRunning,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* emergency stop */ },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Emergency Stop")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Execution history
        Text("Execution History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("No recent tasks", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
