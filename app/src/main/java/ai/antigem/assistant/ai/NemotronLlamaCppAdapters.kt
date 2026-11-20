package ai.antigem.assistant.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

// ============================================================================
// NVIDIA Nemotron Adapter
// ============================================================================

@Serializable
data class NemotronChatRequest(
    val model: String,
    val messages: List<NemotronMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class NemotronMessage(
    val role: String,
    val content: String
)

@Serializable
data class NemotronChatResponse(
    val id: String?,
    val choices: List<NemotronChoice>,
    val usage: NemotronUsage?
)

@Serializable
data class NemotronChoice(
    val index: Int,
    val message: NemotronMessage?,
    val finish_reason: String?
)

@Serializable
data class NemotronUsage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

class NemotronAdapter(
    override val serverConfig: ServerConfig,
    val client: HttpClient
) : ModelProviderAdapter {

    override val providerType: String = "nemotron"

    private val baseUrl: String
        get() = "https://${serverConfig.address}:${serverConfig.port}${serverConfig.apiEndpoint}"

    override suspend fun testConnection(): ConnectionTestResult {
        return try {
            val response = client.get("$baseUrl/models") {
                header("Accept", "application/json")
                addAuthHeaders()
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val capabilities = probeCapabilities()
                    ConnectionTestResult(
                        success = true,
                        message = "Connected to NVIDIA Nemotron at ${serverConfig.name}",
                        capabilities = capabilities
                    )
                }
                else -> ConnectionTestResult(success = false, message = "Unexpected status: ${response.status}")
            }
        } catch (e: Exception) {
            ConnectionTestResult(success = false, message = "Nemotron connection failed: ${e.message}")
        }
    }

    override suspend fun probeCapabilities(): CapabilityProbe {
        return CapabilityProbe(
            supportsVision = true,
            supportsTools = true,
            supportsStructuredOutput = true,
            supportsStreaming = true,
            contextLength = serverConfig.contextLength
        )
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val nemotronRequest = NemotronChatRequest(
            model = request.model,
            messages = request.messages.map {
                NemotronMessage(role = it.role, content = it.content)
            },
            temperature = request.temperature,
            max_tokens = request.maxTokens,
            stream = false
        )
        val response = client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/json")
            addAuthHeaders()
            setBody(Json.encodeToString(nemotronRequest))
        }
        val body = response.bodyAsText()
        val parsed = Json.decodeFromString<NemotronChatResponse>(body)
        val choice = parsed.choices.firstOrNull() ?: return ChatResponse(content = "", isComplete = true)
        return ChatResponse(
            content = choice.message?.content ?: "",
            isComplete = choice.finish_reason != null,
            tokensIn = parsed.usage?.prompt_tokens ?: 0,
            tokensOut = parsed.usage?.completion_tokens ?: 0,
            finishReason = choice.finish_reason
        )
    }

    override suspend fun chatStream(
        request: ChatRequest,
        onChunk: (String) -> Unit
    ): ChatResponse {
        val nemotronRequest = NemotronChatRequest(
            model = request.model,
            messages = request.messages.map {
                NemotronMessage(role = it.role, content = it.content)
            },
            temperature = request.temperature,
            max_tokens = request.maxTokens,
            stream = true
        )
        var fullContent = ""
        var done = false

        client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "text/event-stream")
            addAuthHeaders()
            setBody(Json.encodeToString(nemotronRequest))
        }.bodyAsText().let { body ->
            for (line in body.lines()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:")) {
                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") { done = true; break }
                    try {
                        val parsed = Json.decodeFromString<NemotronChatResponse>(data)
                        val choice = parsed.choices.firstOrNull()
                        val content = choice?.message?.content ?: ""
                        if (content.isNotBlank()) {
                            fullContent += content
                            onChunk(content)
                        }
                        if (choice?.finish_reason != null) { done = true; break }
                    } catch (e: Exception) { /* ignore partial */ }
                }
            }
        }

        return ChatResponse(content = fullContent, isComplete = done, finishReason = if (done) "stop" else null)
    }

    override val isAvailable: Boolean get() = true

    private fun addAuthHeaders(): HttpRequestBuilder.() -> Unit = {
        if (serverConfig.authType == "bearer" && serverConfig.authValue != null) {
            header("Authorization", "Bearer ${serverConfig.authValue}")
        } else if (serverConfig.authType == "api_key" && serverConfig.authValue != null) {
            header("Authorization", "Api-Key ${serverConfig.authValue}")
        }
    }
}

// ============================================================================
// llama.cpp server Adapter
// ============================================================================

@Serializable
data class LlamaCppChatRequest(
    val model: String,
    val messages: List<LlamaCppMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = null,
    val stream: Boolean = false
)

@Serializable
data class LlamaCppMessage(
    val role: String,
    val content: String
)

@Serializable
data class LlamaCppChatResponse(
    val id: String?,
    val choices: List<LlamaCppChoice>,
    val usage: LlamaCppUsage?
)

@Serializable
data class LlamaCppChoice(
    val index: Int,
    val message: LlamaCppMessage?,
    val finish_reason: String?
)

@Serializable
data class LlamaCppUsage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

class LlamaCppAdapter(
    override val serverConfig: ServerConfig,
    val client: HttpClient
) : ModelProviderAdapter {

    override val providerType: String = "llamacpp"

    private val baseUrl: String
        get() = "http://${serverConfig.address}:${serverConfig.port}${serverConfig.apiEndpoint}"

    override suspend fun testConnection(): ConnectionTestResult {
        return try {
            val response = client.get("$baseUrl/health") {
                header("Accept", "application/json")
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val capabilities = probeCapabilities()
                    ConnectionTestResult(
                        success = true,
                        message = "Connected to llama.cpp server at ${serverConfig.name}",
                        capabilities = capabilities
                    )
                }
                else -> ConnectionTestResult(success = false, message = "Unexpected status: ${response.status}")
            }
        } catch (e: Exception) {
            ConnectionTestResult(success = false, message = "llama.cpp connection failed: ${e.message}")
        }
    }

    override suspend fun probeCapabilities(): CapabilityProbe {
        return CapabilityProbe(
            supportsVision = serverConfig.supportsVision,
            supportsTools = serverConfig.supportsTools,
            supportsStructuredOutput = serverConfig.supportsStructuredOutput,
            supportsStreaming = true,
            contextLength = serverConfig.contextLength
        )
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val requestBody = LlamaCppChatRequest(
            model = request.model,
            messages = request.messages.map {
                LlamaCppMessage(role = it.role, content = it.content)
            },
            temperature = request.temperature,
            max_tokens = request.maxTokens,
            stream = false
        )
        val response = client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/json")
            setBody(Json.encodeToString(requestBody))
        }
        val body = response.bodyAsText()
        val parsed = Json.decodeFromString<LlamaCppChatResponse>(body)
        val choice = parsed.choices.firstOrNull() ?: return ChatResponse(content = "", isComplete = true)
        return ChatResponse(
            content = choice.message?.content ?: "",
            isComplete = choice.finish_reason != null,
            tokensIn = parsed.usage?.prompt_tokens ?: 0,
            tokensOut = parsed.usage?.completion_tokens ?: 0,
            finishReason = choice.finish_reason
        )
    }

    override suspend fun chatStream(
        request: ChatRequest,
        onChunk: (String) -> Unit
    ): ChatResponse {
        val requestBody = LlamaCppChatRequest(
            model = request.model,
            messages = request.messages.map {
                LlamaCppMessage(role = it.role, content = it.content)
            },
            temperature = request.temperature,
            max_tokens = request.maxTokens,
            stream = true
        )
        var fullContent = ""
        var done = false

        client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "text/event-stream")
            setBody(Json.encodeToString(requestBody))
        }.bodyAsText().let { body ->
            for (line in body.lines()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:")) {
                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") { done = true; break }
                    try {
                        val parsed = Json.decodeFromString<LlamaCppChatResponse>(data)
                        val choice = parsed.choices.firstOrNull()
                        val content = choice?.message?.content ?: ""
                        if (content.isNotBlank()) {
                            fullContent += content
                            onChunk(content)
                        }
                        if (choice?.finish_reason != null) { done = true; break }
                    } catch (e: Exception) { /* ignore partial */ }
                }
            }
        }

        return ChatResponse(content = fullContent, isComplete = done, finishReason = if (done) "stop" else null)
    }

    override val isAvailable: Boolean get() = true
}
