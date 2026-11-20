package ai.antigem.assistant.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.antigem.assistant.R
import ai.antigem.assistant.ui.theme.*

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedIndex by remember { mutableIntStateOf(0) }

    val screens = listOf(
        { ChatScreen() },
        { ConversationListScreen() },
        { AgentControlScreen() },
        { SettingsScreen() }
    )

    val titles = listOf("Chat", "Conversations", "Agent", "Settings")
    val icons = listOf(Icons.Default.Message, Icons.Default.History, Icons.Default.RocketLaunch, Icons.Default.Settings)

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = { Text(titles[selectedIndex]) },
                colors = AppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                for (i in screens.indices) {
                    NavigationBarItem(
                        icon = { Icon(icons[i], contentDescription = titles[i]) },
                        label = { Text(titles[i]) },
                        selected = selectedIndex == i,
                        onClick = { selectedIndex = i }
                    )
                }
            }
        },
        floatingActionButton = if (selectedIndex == 0) {
            FloatingActionButton(
                onClick = { /* New conversation */ },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New conversation")
            }
        } else null
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            screens[selectedIndex]()
        }
    }
}
