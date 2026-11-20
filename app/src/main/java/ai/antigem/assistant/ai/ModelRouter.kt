package ai.antigem.assistant.ai

import kotlinx.coroutines.flow.*

// ============================================================================
// Model Router - selects appropriate model for task type
// ============================================================================

enum class TaskType {
    GENERAL, REASONING, CODING, VISION, OCR, PLANNING, SUMMARIZATION, VOICE
}

data class RouteDecision(
    val serverId: String,
    val modelId: String?,
    val taskType: TaskType,
    val reason: String
)

class ModelRouter(
    private val serverConfigs: List<ServerConfig>
) {

    fun route(taskType: TaskType, preferredModel: String? = null): RouteDecision? {
        // 1. If user specified a preferred model, find a server with it
        if (preferredModel != null) {
            val match = serverConfigs.firstOrNull { it.modelId == preferredModel && it.isConnected }
            if (match != null) {
                return RouteDecision(match.id, match.modelId, taskType, "User-preferred model")
            }
        }

        // 2. Route by task type
        val candidate = when (taskType) {
            TaskType.VISION -> serverConfigs.firstOrNull { it.supportsVision && it.isConnected }
            TaskType.CODING -> serverConfigs.firstOrNull { it.supportsTools && it.isConnected }
            TaskType.REASONING -> serverConfigs.firstOrNull { it.contextLength >= 8192 && it.isConnected }
            TaskType.SUMMARIZATION -> serverConfigs.firstOrNull { it.supportsStreaming && it.isConnected }
            TaskType.VOICE -> serverConfigs.firstOrNull { it.supportsStreaming && it.isConnected }
            TaskType.PLANNING -> serverConfigs.firstOrNull { it.supportsTools && it.isConnected }
            TaskType.OCR -> serverConfigs.firstOrNull { it.supportsVision && it.isConnected }
            TaskType.GENERAL -> serverConfigs.firstOrNull { it.isConnected }
        }

        // 3. Fallback to any connected server
        val fallback = candidate ?: serverConfigs.firstOrNull { it.isConnected }
        // 4. Fallback to default server
        val finalCandidate = fallback ?: serverConfigs.firstOrNull { it.isDefault }

        return finalCandidate?.let {
            RouteDecision(it.id, it.modelId, taskType, "Auto-routed for $taskType")
        }
    }

    fun getAvailableServers(): List<ServerConfig> =
        serverConfigs.filter { it.isConnected }

    fun getDefaultServer(): ServerConfig? =
        serverConfigs.firstOrNull { it.isDefault }
}
