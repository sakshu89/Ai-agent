package ai.antigem.assistant.github

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** OAuth device authorization flow. The app never sees a password and persists only the resulting token. */
class GitHubDeviceAuthorization(private val client: HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}) {
    suspend fun requestCode(clientId: String): DeviceCode = client.post(DEVICE_CODE_URL) {
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Accept, "application/json")
        setBody("{\"client_id\":\"${escape(clientId)}\",\"scope\":\"repo workflow read:user\"}")
    }.body()

    suspend fun poll(clientId: String, deviceCode: String): TokenResult = client.post(TOKEN_URL) {
        contentType(ContentType.Application.Json)
        header(HttpHeaders.Accept, "application/json")
        setBody("{\"client_id\":\"${escape(clientId)}\",\"device_code\":\"${escape(deviceCode)}\",\"grant_type\":\"urn:ietf:params:oauth:grant-type:device_code\"}")
    }.body()

    fun close() = client.close()
    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
    private companion object {
        const val DEVICE_CODE_URL = "https://github.com/login/device/code"
        const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    }
}

@Serializable data class DeviceCode(
    @kotlinx.serialization.SerialName("device_code") val deviceCode: String,
    @kotlinx.serialization.SerialName("user_code") val userCode: String,
    @kotlinx.serialization.SerialName("verification_uri") val verificationUri: String,
    @kotlinx.serialization.SerialName("expires_in") val expiresInSeconds: Int,
    val interval: Int = 5
)
@Serializable data class TokenResult(
    @kotlinx.serialization.SerialName("access_token") val accessToken: String? = null,
    val error: String? = null,
    @kotlinx.serialization.SerialName("error_description") val errorDescription: String? = null
)
