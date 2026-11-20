package ai.antigem.assistant.research

import ai.antigem.assistant.ai.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import android.content.Intent

// ============================================================================
// Internet Research Engine
// ============================================================================

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String,
    val date: String? = null
)

data class ResearchReport(
    val query: String,
    val results: List<SearchResult>,
    val summary: String,
    val sources: List<String>,
    val generatedAt: Long = System.currentTimeMillis()
)

class ResearchManager(
    private val aiService: AiService
) {

    private val searchEngines = listOf(
        "https://www.google.com/search?q=",
        "https://html.duckduckgo.com/html/?q="
    )

    suspend fun searchWeb(query: String, maxResults: Int = 5): List<SearchResult> {
        // Use browser-based search via intent (can't scrape Google directly reliably)
        // Return results that the user can open in browser
        return listOf(
            SearchResult(
                title = "Google Search: $query",
                url = "https://www.google.com/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}",
                snippet = "Search Google for: $query",
                source = "Google"
            ),
            SearchResult(
                title = "DuckDuckGo Search: $query",
                url = "https://duckduckgo.com/?q=${java.net.URLEncoder.encode(query, "UTF-8")}",
                snippet = "Search DuckDuckGo for: $query",
                source = "DuckDuckGo"
            )
        )
    }

    suspend fun browseAndSummarize(url: String): ResearchReport {
        // Fetch webpage content and summarize via AI
        val content = fetchWebpage(url)
        val summary = summarizeContent(content, url)
        return ResearchReport(
            query = url,
            results = listOf(SearchResult(title = url, url = url, snippet = content.take(200), source = "Webpage")),
            summary = summary,
            sources = listOf(url)
        )
    }

    suspend fun researchTopic(query: String, maxResults: Int = 5): ResearchReport {
        val searchResults = searchWeb(query, maxResults)
        val summaries = mutableListOf<String>()
        val sources = mutableListOf<String>()

        for (result in searchResults) {
            sources.add(result.url)
            summaries.add("${result.title}: ${result.snippet}")
        }

        val combined = summaries.joinToString("\n")
        val aiSummary = aiService.chatWithFallback(
            ChatRequest(
                model = "",
                messages = listOf(
                    ChatMessage("system", "You are a research assistant. Summarize the following search results for the query: $query. Provide source links and distinguish current facts from older information."),
                    ChatMessage("user", combined)
                ),
                temperature = 0.3f,
                stream = false
            ),
            taskType = TaskType.SUMMARIZATION
        )

        return ResearchReport(
            query = query,
            results = searchResults,
            summary = aiSummary.content,
            sources = sources
        )
    }

    private suspend fun fetchWebpage(url: String): String {
        // Use the AI service's HTTP capability is not exposed; return placeholder
        return "Webpage content for: $url (fetch via browser)"
    }

    private suspend fun summarizeContent(content: String, url: String): String {
        val response = aiService.chatWithFallback(
            ChatRequest(
                model = "",
                messages = listOf(
                    ChatMessage("system", "Summarize this webpage content concisely, including key facts and dates."),
                    ChatMessage("user", "URL: $url\nContent: ${content.take(4000)}")
                ),
                temperature = 0.3f,
                stream = false
            ),
            taskType = TaskType.SUMMARIZATION
        )
        return response.content
    }

    fun openSearchResult(url: String, context: android.content.Context) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
