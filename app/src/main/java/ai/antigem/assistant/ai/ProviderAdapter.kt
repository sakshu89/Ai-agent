package ai.antigem.assistant.ai

import kotlinx.coroutines.flow.Flow

// ============================================================================
// AI Server Connectivity - Provider-Independent Framework
// ============================================================================

// ----- Data Models -----

data class ServerConfig(
    val id: String,
    val name: String,
    val address: String,
    val port: Int,
    val apiEndpoint: String,
    val modelId: String?,
    val authType: String, // "none", "bearer", "api_key", "basic"
    val authValue: String?,
    val contextLength: Int,
    val supportsStreaming: Boolean,
    val supportsVision: Boolean,
    val supportsTools: Boolean,
    val supportsStructuredOutput: Boolean,
    val timeoutMs: Long,
    val retryCount: Int,
    val retryDelayMs: Long,
    val providerType: String, // "ollama", "llamacpp", "openai", "nemotron", "generic"
    val isDefault: Boolean = false,
    val isConnected: Boolean = false
)

data class ChatMessage(
    val role: String, // "system", "user", "assistant", "tool"
    val content: String,
    val toolName: String? = null,
    val toolResult: String? = null
)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int? = null,
    val stream: Boolean = false,
    val tools: List<ToolDefinition>? = null,
    val toolChoice: String? = null,
    val format: String? = null // for structured output
)

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any?>
)

data class ChatResponse(
    val content: String,
    val isComplete: Boolean,
    val toolCalls: List<ToolCall>? = null,
    val tokensIn: Int = 0,
    val tokensOut: Int = 0,
    val finishReason: String? = null
)

data class ToolCall(
    val id: String,
    val name: String,
    val arguments: Map<String, Any?>
)

data class CapabilityProbe(
    val supportsVision: Boolean = false,
    val supportsTools: Boolean = false,
    val supportsStructuredOutput: Boolean = false,
    val supportsStreaming: Boolean = false,
    val contextLength: Int = 4096,
    val availableModels: List<String> = emptyList()
)

data class ConnectionTestResult(
    val success: Boolean,
    val message: String,
    val capabilities: CapabilityProbe? = null
)

// ----- Provider Interface -----

interface ModelProviderAdapter {
    val providerType: String
    val serverConfig: ServerConfig

    suspend fun testConnection(): ConnectionTestResult
    suspend fun probeCapabilities(): CapabilityProbe
    suspend fun chat(request: ChatRequest): ChatResponse
    suspend fun chatStream(request: ChatRequest, onChunk: (String) -> Unit): ChatResponse

    val isAvailable: Boolean
}

// ----- Exception -----

class AiServerException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class AiServerUnavailableException(message: String) : Exception(message)
