package ai.antigem.assistant.ai

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

// ============================================================================
// AI Service - orchestrates providers with fallback, retry, offline support
// ============================================================================

class AiService(
    private val router: ModelRouter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val clients = mutableMapOf<String, HttpClient>()

    private fun getClient(serverId: String): HttpClient {
        return clients.getOrPut(serverId) {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
    }

    private fun createAdapter(config: ServerConfig): ModelProviderAdapter {
        return when (config.providerType.lowercase()) {
            "ollama" -> OllamaAdapter(config, getClient(config.id))
            "llamacpp" -> LlamaCppAdapter(config, getClient(config.id))
            "nemotron" -> NemotronAdapter(config, getClient(config.id))
            else -> OpenAICompatibleAdapter(config, getClient(config.id))
        }
    }

    suspend fun chatWithFallback(
        request: ChatRequest,
        taskType: TaskType = TaskType.GENERAL,
        preferredModel: String? = null,
        onChunk: ((String) -> Unit)? = null
    ): ChatResponse {
        val decision = router.route(taskType, preferredModel)
            ?: throw AiServerUnavailableException("No AI server available for routing")

        val attempted = mutableSetOf<String>()
        var lastError: Throwable? = null

        val candidates = mutableListOf(decision.serverId)
        candidates.addAll(router.getAvailableServers().map { it.id }.filter { it != decision.serverId })

        for (serverId in candidates) {
            if (attempted.contains(serverId)) continue
            attempted.add(serverId)

            val config = router.getAvailableServers().firstOrNull { it.id == serverId }
                ?: continue

            val adapter = createAdapter(config)
            if (!adapter.isAvailable) continue

            val serverRequest = request.copy(model = config.modelId ?: request.model)

            try {
                return if (request.stream && onChunk != null && adapter.serverConfig.supportsStreaming) {
                    adapter.chatStream(serverRequest, onChunk)
                } else {
                    adapter.chat(serverRequest)
                }
            } catch (e: Exception) {
                lastError = e
                continue
            }
        }

        throw lastError ?: AiServerUnavailableException("All AI servers failed")
    }

    suspend fun testServer(config: ServerConfig): ConnectionTestResult {
        val adapter = createAdapter(config)
        return adapter.testConnection()
    }

    suspend fun probeCapabilities(config: ServerConfig): CapabilityProbe {
        val adapter = createAdapter(config)
        return adapter.probeCapabilities()
    }

    fun close() {
        clients.values.forEach { it.close() }
        clients.clear()
    }
}
