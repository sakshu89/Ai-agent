package ai.antigem.assistant.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ai.antigem.assistant.R
import ai.antigem.assistant.github.GitHubApi
import ai.antigem.assistant.github.GitHubDeviceAuthorization
import ai.antigem.assistant.github.GitHubSessionStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Remote-build control surface. Builds execute on GitHub Actions, never on the device. */
class CodingAgentActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private lateinit var clientId: EditText
    private val session by lazy { GitHubSessionStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coding_agent)
        status = findViewById(R.id.coding_status)
        clientId = findViewById(R.id.github_client_id)
        renderDisconnectedState()
        findViewById<Button>(R.id.connect_github).setOnClickListener { connectGitHub() }
        findViewById<Button>(R.id.refresh_builds).setOnClickListener { refreshBuilds() }
    }

    private fun renderDisconnectedState() {
        status.text = if (session.hasToken()) {
            "Connected. Select a repository and refresh workflow status."
        } else {
            "Not connected. Use a GitHub OAuth App client ID to begin device authorization."
        }
    }

    private fun connectGitHub() {
        val id = clientId.text.toString().trim()
        if (id.isBlank()) { status.text = "Enter the OAuth App client ID first."; return }
        lifecycleScope.launch {
            val authorization = GitHubDeviceAuthorization()
            try {
                val code = authorization.requestCode(id)
                status.text = "Code ${code.userCode}: complete authorization in your browser. Waiting…"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(code.verificationUri)))
                val deadline = System.currentTimeMillis() + code.expiresInSeconds * 1000L
                while (System.currentTimeMillis() < deadline) {
                    delay(code.interval.coerceAtLeast(5) * 1000L)
                    val result = authorization.poll(id, code.deviceCode)
                    if (!result.accessToken.isNullOrBlank()) {
                        session.saveAccessToken(result.accessToken)
                        status.text = "Connected securely. Your token is encrypted with Android Keystore."
                        return@launch
                    }
                    if (result.error !in setOf("authorization_pending", "slow_down")) {
                        status.text = result.errorDescription ?: "GitHub authorization was not completed."
                        return@launch
                    }
                }
                status.text = "Authorization code expired. Try again."
            } catch (_: Exception) {
                status.text = "Could not contact GitHub. Check your connection and OAuth App configuration."
            } finally { authorization.close() }
        }
    }

    private fun refreshBuilds() {
        val token = session.accessToken()
        if (token == null) { status.text = "Connect GitHub before checking builds."; return }
        val repo = findViewById<EditText>(R.id.repository_name).text.toString().trim().split("/")
        if (repo.size != 2 || repo.any { it.isBlank() }) { status.text = "Enter repository as owner/name."; return }
        lifecycleScope.launch {
            val api = GitHubApi(token)
            try {
                val run = api.workflowRuns(repo[0], repo[1]).runs.firstOrNull()
                status.text = run?.let { "Latest run: ${it.status} · ${it.conclusion ?: "in progress"}\n${it.htmlUrl}" }
                    ?: "No workflow runs found."
            } catch (_: Exception) {
                status.text = "Could not retrieve workflow status. Verify repository access."
            } finally { api.close() }
        }
    }
}
