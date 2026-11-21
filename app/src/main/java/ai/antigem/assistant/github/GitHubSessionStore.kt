package ai.antigem.assistant.github

import android.content.Context
import ai.antigem.assistant.security.SecurityManager

/** Stores only encrypted GitHub credentials. Tokens are never placed in Room or logs. */
class GitHubSessionStore(context: Context) {
    private val security = SecurityManager(context.applicationContext)

    fun saveAccessToken(token: String) = security.storeSecret(ACCESS_TOKEN_KEY, token)
    fun accessToken(): String? = security.getSecret(ACCESS_TOKEN_KEY)
    fun clear() = security.removeSecret(ACCESS_TOKEN_KEY)
    fun hasToken(): Boolean = !accessToken().isNullOrBlank()

    private companion object { const val ACCESS_TOKEN_KEY = "github_access_token" }
}
