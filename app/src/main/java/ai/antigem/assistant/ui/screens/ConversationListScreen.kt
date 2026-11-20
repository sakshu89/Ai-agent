package ai.antigem.assistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ai.antigem.assistant.core.ConversationEntity
import ai.antigem.assistant.ui.theme.*

@Composable
fun ConversationListScreen(
    onConversationSelected: (String) -> Unit = {},
    onNewConversation: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Conversations") },
            actions = {
                IconButton(onClick = { /* Search */ }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(items = listOf<ConversationEntity>()) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    onClick = { onConversationSelected(conversation.id) },
                    onPin = { /* toggle pin */ },
                    onArchive = { /* toggle archive */ },
                    onDelete = { /* delete */ }
                )
                Divider()
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = conversation.title.ifEmpty { "New Conversation" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = androidx.compose.foundation.layout.Overflow.Ellipsis
            )
            Text(
                text = conversation.lastMessagePreview ?: "No messages yet",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = androidx.compose.foundation.layout.Overflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (conversation.isPinned) {
                Icon(Icons.Default.Pin, contentDescription = "Pinned", tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onArchive) {
                Icon(Icons.Default.Delete, contentDescription = "Archive")
            }
        }
    }
}
