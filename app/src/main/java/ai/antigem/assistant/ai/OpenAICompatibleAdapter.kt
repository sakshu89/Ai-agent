package ai.antigem.assistant.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// ============================================================================
// OpenAI-Compatible Adapter (works with Ollama, llama.cpp, generic OpenAI API)
// ============================================================================

@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = null,
    val stream: Boolean = false,
    val tools: List<OpenAITool>? = null,
    val tool_choice: Any? = null,
    val format: Any? = null
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String?,
    val tool_calls: List<OpenAIToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
)

@Serializable
data class OpenAITool(
    val type: String = "function",
    val function: OpenAIFunction
)

@Serializable
data class OpenAIFunction(
    val name: String,
    val description: String?,
    val parameters: Map<String, Any?>
)

@Serializable
data class OpenAIToolCall(
    val id: String,
    val type: String = "function",
    val function: OpenAIFunctionCall
)

@Serializable
data class OpenAIFunctionCall(
    val name: String,
    val arguments: String
)

@Serializable
data class OpenAIChatResponse(
    val id: String?,
    @SerialName("object") val odata: String?,
    val created: Long?,
    val model: String?,
    val choices: List<OpenAIChoice>,
    val usage: OpenAIUsage?
)

@Serializable
data class OpenAIChoice(
    val index: Int,
    val message: OpenAIMessage?,
    val delta: OpenAIMessage?,
    val finish_reason: String?
)

@Serializable
data class OpenAIUsage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

@Serializable
data class OpenAIModel(
    val id: String,
    @SerialName("object") val odata: String?,
    val created: Long?,
    val owned_by: String?
)

@Serializable
data class OpenAIModelList(
    val data: List<OpenAIModel>
)

class OpenAICompatibleAdapter(
    override val serverConfig: ServerConfig,
    val client: HttpClient
) : ModelProviderAdapter {

    override val providerType: String = serverConfig.providerType

    private val baseUrl: String
        get() {
            val scheme = if (serverConfig.port == 443) "https" else "http"
            return "$scheme://${serverConfig.address}:${serverConfig.port}${serverConfig.apiEndpoint}"
        }

    override suspend fun testConnection(): ConnectionTestResult {
        return try {
            val url = "$baseUrl/models"
            val response = client.get(url) {
                header("Accept", "application/json")
                addAuthHeaders()
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val body = response.bodyAsText()
                    val models = Json.decodeFromString<OpenAIModelList>(body)
                    val availableModels = models.data.map { it.id }
                    val capabilities = probeCapabilities()
                    ConnectionTestResult(
                        success = true,
                        message = "Connected to ${serverConfig.name}",
                        capabilities = capabilities.copy(availableModels = availableModels)
                    )
                }
                else -> ConnectionTestResult(
                    success = false,
                    message = "Unexpected status: ${response.status}"
                )
            }
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                message = "Connection failed: ${e.message}"
            )
        }
    }

    override suspend fun probeCapabilities(): CapabilityProbe {
        return CapabilityProbe(
            supportsVision = serverConfig.supportsVision,
            supportsTools = serverConfig.supportsTools,
            supportsStructuredOutput = serverConfig.supportsStructuredOutput,
            supportsStreaming = serverConfig.supportsStreaming,
            contextLength = serverConfig.contextLength
        )
    }

    override suspend fun chat(request: ChatRequest): ChatResponse {
        val openAIRequest = toOpenAIRequest(request, stream = false)
        val response = client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/json")
            addAuthHeaders()
            setBody(Json.encodeToString(openAIRequest))
        }
        return parseResponse(response.bodyAsText())
    }

    override suspend fun chatStream(
        request: ChatRequest,
        onChunk: (String) -> Unit
    ): ChatResponse {
        val openAIRequest = toOpenAIRequest(request, stream = true)
        var fullContent = ""
        var toolCalls: List<ToolCall>? = null
        var finishReason: String? = null
        var tokensIn = 0
        var tokensOut = 0

        client.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Accept", "text/event-stream")
            header("Cache-Control", "no-cache")
            addAuthHeaders()
            setBody(Json.encodeToString(openAIRequest))
        }.bodyAsText().let { sseBody ->
            for (line in sseBody.lines()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:")) {
                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    try {
                        val json = Json.parseToJsonElement(data)
                        val choicesObj = json.jsonObject["choices"]?.jsonArray
                        if (choicesObj?.isNotEmpty() == true) {
                            val delta = choicesObj[0].jsonObject["delta"]?.jsonObject
                            val content = delta?.get("content")?.jsonPrimitive?.content ?: ""
                            if (content.isNotBlank()) {
                                fullContent += content
                                onChunk(content)
                            }
                            val toolCallsDelta = delta?.get("tool_calls")?.jsonArray
                            if (toolCallsDelta != null) {
                                toolCalls = parseToolCallsFromStream(toolCallsDelta)
                            }
                            finishReason = choicesObj[0].jsonObject["finish_reason"]?.jsonPrimitive?.content
                        }
                        val usage = json.jsonObject["usage"]?.jsonObject
                        if (usage != null) {
                            tokensIn = usage["prompt_tokens"]?.jsonPrimitive?.content?.toInt() ?: 0
                            tokensOut = usage["completion_tokens"]?.jsonPrimitive?.content?.toInt() ?: 0
                        }
                    } catch (e: Exception) {
                        // ignore parse errors on partial SSE lines
                    }
                }
            }
        }

        return ChatResponse(
            content = fullContent,
            isComplete = true,
            toolCalls = toolCalls,
            tokensIn = tokensIn,
            tokensOut = tokensOut,
            finishReason = finishReason
        )
    }

    override val isAvailable: Boolean get() = true

    private fun toOpenAIRequest(request: ChatRequest, stream: Boolean): OpenAIChatRequest {
        val messages = request.messages.map { msg ->
            OpenAIMessage(
                role = msg.role,
                content = msg.content.takeIf { it.isNotBlank() },
                tool_calls = msg.toolName?.let {
                    listOf(OpenAIToolCall(id = "call_$it", function = OpenAIFunctionCall(name = msg.toolName, arguments = msg.toolResult ?: "{}")))
                },
                tool_call_id = if (msg.role == "tool") msg.toolName else null,
                name = null
            )
        }
        return OpenAIChatRequest(
            model = request.model,
            messages = messages,
            temperature = request.temperature,
            max_tokens = request.maxTokens,
            stream = stream,
            tools = request.tools?.map {
                OpenAITool(function = OpenAIFunction(it.name, it.description, it.parameters))
            },
            tool_choice = request.toolChoice,
            format = request.format
        )
    }

    private fun addAuthHeaders(): HttpRequestBuilder.() -> Unit = {
        if (serverConfig.authType == "bearer" && serverConfig.authValue != null) {
            header("Authorization", "Bearer ${serverConfig.authValue}")
        } else if (serverConfig.authType == "api_key" && serverConfig.authValue != null) {
            header("Authorization", "Api-Key ${serverConfig.authValue}")
        } else if (serverConfig.authType == "basic" && serverConfig.authValue != null) {
            header("Authorization", "Basic ${serverConfig.authValue}")
        }
    }

    private fun parseResponse(body: String): ChatResponse {
        val parsed = Json.decodeFromString<OpenAIChatResponse>(body)
        val choice = parsed.choices.firstOrNull() ?: return ChatResponse(content = "", isComplete = true)
        val content = choice.message?.content ?: ""
        val toolCalls = choice.message?.tool_calls?.map { tc ->
            ToolCall(id = tc.id, name = tc.function.name, arguments = parseJsonMap(tc.function.arguments))
        }
        return ChatResponse(
            content = content,
            isComplete = choice.finish_reason != null,
            toolCalls = toolCalls,
            tokensIn = parsed.usage?.prompt_tokens ?: 0,
            tokensOut = parsed.usage?.completion_tokens ?: 0,
            finishReason = choice.finish_reason
        )
    }

    private fun parseJsonMap(s: String): Map<String, Any?> {
        return try {
            Json.decodeFromString<Map<String, JsonElement>>(s).mapValues { it.value }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun parseToolCallsFromStream(array: JsonArray): List<ToolCall> {
        return array.mapIndexed { index, element ->
            val obj = element.jsonObject
            val function = obj["function"]?.jsonObject
            ToolCall(
                id = obj["id"]?.jsonPrimitive?.content ?: "call_$index",
                name = function?.get("name")?.jsonPrimitive?.content ?: "",
                arguments = parseJsonMap(function?.get("arguments")?.jsonPrimitive?.content ?: "{}")
            )
        }
    }
}

// ============================================================================
// Ollama Adapter (specialized)
// ============================================================================

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = false,
    val format: String? = null,
    val options: OllamaOptions? = null
)

@Serializable
data class OllamaMessage(
    val role: String,
    val content: String,
    val images: List<String>? = null,
    val tool_calls: List<OllamaToolCall>? = null
)

@Serializable
data class OllamaToolCall(
    val function: OllamaFunctionCall
)

@Serializable
data class OllamaFunctionCall(
    val name: String,
    val arguments: Map<String, Any?>
)

@Serializable
data class OllamaOptions(
    val temperature: Float? = null,
    val num_predict: Int? = null
)

@Serializable
data class OllamaChatResponse(
    val model: String?,
    val created_at: String?,
    val message: OllamaMessage?,
    val done: Boolean = false,
    val total_duration: Long? = null,
    val prompt_eval_count: Int? = null,
    val eval_count: Int? = null
)

class OllamaAdapter(
    override val serverConfig: ServerConfig,
    val client: HttpClient
) : ModelProviderAdapter {

    override val providerType: String = "ollama"

    private val baseUrl: String
        get() = "http://${serverConfig.address}:${serverConfig.port}"

    override suspend fun testConnection(): ConnectionTestResult {
        return try {
            val response = client.get("$baseUrl/api/tags") {
                header("Accept", "application/json")
            }
            when (response.status) {
                HttpStatusCode.OK -> {
                    val body = response.bodyAsText()
                    val parsed = Json.decodeFromString<JsonElement>(body)
                    val models = parsed.jsonObject["models"]?.jsonArray
                        ?.map { it.jsonObject["name"]?.jsonPrimitive?.content ?: "" }
                        ?.filter { it.isNotEmpty() }
                    val capabilities = probeCapabilities()
                    ConnectionTestResult(
                        success = true,
                        message = "Connected to Ollama at ${serverConfig.name}",
                        capabilities = capabilities.copy(availableModels = models ?: emptyList())
                    )
                }
                else -> ConnectionTestResult(success = false, message = "Unexpected status: ${response.status}")
            }
        } catch (e: Exception) {
            ConnectionTestResult(success = false, message = "Ollama connection failed: ${e.message}")
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
        val ollamaRequest = OllamaChatRequest(
            model = request.model,
            messages = request.messages.map {
                OllamaMessage(role = it.role, content = it.content)
            },
            stream = false,
            format = request.format,
            options = OllamaOptions(temperature = request.temperature, num_predict = request.maxTokens)
        )
        val response = client.post("$baseUrl/api/chat") {
            contentType(ContentType.Application.Json)
            header("Accept", "application/json")
            setBody(Json.encodeToString(ollamaRequest))
        }
        val body = response.bodyAsText()
        val parsed = Json.decodeFromString<OllamaChatResponse>(body)
        val content = parsed.message?.content ?: ""
        return ChatResponse(
            content = content,
            isComplete = parsed.done,
            tokensIn = parsed.prompt_eval_count ?: 0,
            tokensOut = parsed.eval_count ?: 0,
            finishReason = if (parsed.done) "stop" else null
        )
    }

    override suspend fun chatStream(
        request: ChatRequest,
        onChunk: (String) -> Unit
    ): ChatResponse {
        val ollamaRequest = OllamaChatRequest(
            model = request.model,
            messages = request.messages.map {
                OllamaMessage(role = it.role, content = it.content)
            },
            stream = true,
            format = request.format,
            options = OllamaOptions(temperature = request.temperature, num_predict = request.maxTokens)
        )
        var fullContent = ""
        var done = false
        var tokensIn = 0
        var tokensOut = 0

        client.post("$baseUrl/api/chat") {
            contentType(ContentType.Application.Json)
            header("Accept", "text/event-stream")
            setBody(Json.encodeToString(ollamaRequest))
        }.bodyAsText().let { sseBody ->
            for (line in sseBody.lines()) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    try {
                        val parsed = Json.decodeFromString<OllamaChatResponse>(trimmed)
                        val content = parsed.message?.content ?: ""
                        if (content.isNotBlank()) {
                            fullContent += content
                            onChunk(content)
                        }
                        done = parsed.done
                        tokensIn = parsed.prompt_eval_count ?: 0
                        tokensOut = parsed.eval_count ?: 0
                        if (done) break
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }

        return ChatResponse(
            content = fullContent,
            isComplete = done,
            tokensIn = tokensIn,
            tokensOut = tokensOut,
            finishReason = if (done) "stop" else null
        )
    }

    override val isAvailable: Boolean get() = true
}
