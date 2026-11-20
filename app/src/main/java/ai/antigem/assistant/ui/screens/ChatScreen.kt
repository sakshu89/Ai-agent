package ai.antigem.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VoiceOvermail
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.softwareKeyboardController
import androidx.compose.ui.unit.dp
import ai.antigem.assistant.ui.theme.*

@Composable
fun ChatScreen(
    conversationId: String = "current",
    onBack: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Messages area
        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Assistant", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Hello! I'm your AntiGem assistant. How can I help you today?",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (isStreaming) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("Streaming response...",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Input area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* attach */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach")
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Type a message…") },
                    modifier = Modifier.weight(1f).padding(start = 8.dp, end = 8.dp),
                    singleLine = false,
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            messageText = ""
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                    enabled = messageText.isNotBlank() && !isStreaming
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }

                IconButton(
                    onClick = { /* voice input */ },
                    enabled = !isStreaming
                ) {
                    Icon(Icons.Default.VoiceOvermail, contentDescription = "Voice input")
                }
            }
        }
    }
}
