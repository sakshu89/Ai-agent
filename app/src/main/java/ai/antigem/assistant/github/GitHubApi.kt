package ai.antigem.assistant.github

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json

/** GitHub REST client used by the coding dashboard. All write endpoints are explicit calls. */
class GitHubApi(
    private val token: String,
    private val client: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
) {
    private fun io.ktor.client.request.HttpRequestBuilder.githubHeaders() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header("X-GitHub-Api-Version", "2022-11-28")
        header(HttpHeaders.Accept, "application/vnd.github+json")
    }

    suspend fun repositories(): List<GitHubRepository> = client.get("https://api.github.com/user/repos?sort=updated&per_page=100") {
        githubHeaders()
    }.body()

    suspend fun branches(owner: String, repository: String): List<GitHubBranch> =
        client.get("https://api.github.com/repos/$owner/$repository/branches?per_page=100") { githubHeaders() }.body()

    suspend fun workflowRuns(owner: String, repository: String): GitHubWorkflowRuns =
        client.get("https://api.github.com/repos/$owner/$repository/actions/runs?per_page=20") { githubHeaders() }.body()

    /** Starts an existing workflow only. Branch creation, commits and PRs require a reviewed plan upstream. */
    suspend fun dispatchWorkflow(owner: String, repository: String, workflow: String, branch: String) {
        client.post("https://api.github.com/repos/$owner/$repository/actions/workflows/$workflow/dispatches") {
            githubHeaders(); contentType(ContentType.Application.Json); setBody("{\"ref\":\"${json(branch)}\"}")
        }
    }

    fun close() = client.close()
    private fun json(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
}

@Serializable data class GitHubRepository(val id: Long, @SerialName("full_name") val fullName: String, @SerialName("default_branch") val defaultBranch: String)
@Serializable data class GitHubBranch(val name: String)
@Serializable data class GitHubWorkflowRuns(@SerialName("workflow_runs") val runs: List<GitHubWorkflowRun> = emptyList())
@Serializable data class GitHubWorkflowRun(val id: Long, val status: String, val conclusion: String? = null, @SerialName("html_url") val htmlUrl: String)
