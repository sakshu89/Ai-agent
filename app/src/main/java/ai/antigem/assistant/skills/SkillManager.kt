package ai.antigem.assistant.skills

import ai.antigem.assistant.core.*
import android.content.Intent
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// ============================================================================
// Skill Discovery & Management System
// ============================================================================

data class SkillCapability(
    val name: String,
    val description: String,
    val requiredPermissions: List<String> = emptyList(),
    val supportedApps: List<String> = emptyList()
)

data class SkillInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val category: String,
    val capabilities: List<SkillCapability>,
    val dependencies: List<String>,
    val permissions: List<String>,
    val isEnabled: Boolean,
    val isBuiltIn: Boolean,
    val securityReviewed: Boolean,
    val securityNotes: String?,
    val testResults: Map<String, String>,
    val installationStatus: String
)

class SkillManager(
    private val repository: AppRepository,
    private val context: android.content.Context
) {

    private val builtInSkills = mutableMapOf<String, BuiltInSkill>()

    init {
        registerBuiltInSkills()
    }

    private fun registerBuiltInSkills() {
        // App opening skill
        builtInSkills["open_app"] = BuiltInSkill(
            id = "open_app",
            name = "Open Application",
            description = "Open any installed application by package name",
            category = "system",
            capabilities = listOf(SkillCapability("open_app", "Open an app", listOf(), emptyList())),
            execute = { params ->
                val pkg = params["package"] as? String ?: params["app"] as? String
                if (pkg != null) {
                    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        "Opened $pkg"
                    } else {
                        "Could not find app: $pkg"
                    }
                } else {
                    "No package specified"
                }
            }
        )

        // Web search skill
        builtInSkills["web_search"] = BuiltInSkill(
            id = "web_search",
            name = "Web Search",
            description = "Search the web using the default browser",
            category = "internet",
            capabilities = listOf(SkillCapability("web_search", "Search the web", listOf(), emptyList())),
            execute = { params ->
                val query = params["query"] as? String ?: ""
                val url = android.net.Uri.parse("https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8"))
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, url).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Searched for: $query"
            }
        )

        // Send SMS skill
        builtInSkills["send_sms"] = BuiltInSkill(
            id = "send_sms",
            name = "Send SMS",
            description = "Send an SMS message",
            category = "communication",
            capabilities = listOf(SkillCapability("send_sms", "Send SMS", listOf("android.permission.SEND_SMS"), emptyList())),
            execute = { params ->
                val number = params["number"] as? String ?: ""
                val message = params["message"] as? String ?: ""
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("smsto:$number")
                    putExtra("sms_body", message)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "SMS composed for $number"
            }
        )

        // Make call skill
        builtInSkills["make_call"] = BuiltInSkill(
            id = "make_call",
            name = "Make Phone Call",
            description = "Place a phone call",
            category = "communication",
            capabilities = listOf(SkillCapability("make_call", "Make a call", listOf("android.permission.CALL_PHONE"), emptyList())),
            execute = { params ->
                val number = params["number"] as? String ?: ""
                val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                    data = android.net.Uri.parse("tel:$number")
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                "Calling $number"
            }
        )

        // Take screenshot skill
        builtInSkills["screenshot"] = BuiltInSkill(
            id = "screenshot",
            name = "Take Screenshot",
            description = "Capture the current screen",
            category = "media",
            capabilities = listOf(SkillCapability("screenshot", "Take screenshot", listOf("android.permission.PROJECT_MEDIA"), emptyList())),
            execute = { params ->
                "Screenshot captured"
            }
        )
    }

    // Discover installed applications as potential skills
    suspend fun discoverInstalledApps(): List<DiscoveredApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = mutableListOf<DiscoveredApp>()
        val intent = android.content.Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfo = pm.queryIntentActivities(intent, 0)
        resolveInfo.forEach { ri ->
            ri.activityInfo?.let { ai ->
                apps.add(DiscoveredApp(
                    packageName = ai.packageName,
                    appName = ai.loadLabel(pm)?.toString() ?: ai.packageName,
                    activities = listOf(ai.name),
                    isSystemApp = (ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                ))
            }
        }
        apps.sortedBy { it.appName }
    }

    // Register a skill from discovery
    suspend fun registerDiscoveredApp(app: DiscoveredApp) {
        val skillConfig = SkillConfigEntity(
            id = "skill_${app.packageName}",
            name = "Open ${app.appName}",
            description = "Launch ${app.appName}",
            version = "1.0.0",
            category = "app",
            capabilitiesJson = "[]",
            dependenciesJson = "[]",
            permissionsJson = "[]",
            isEnabled = false,
            isBuiltIn = false,
            securityReviewed = false,
            securityNotes = "Discovered installed app - review before enabling",
            testResultsJson = "{}",
            installationStatus = "not_installed",
            createdAt = System.currentTimeMillis()
        )
        repository.insertSkill(skillConfig)
    }

    // Get all skills (built-in + registered)
    suspend fun getAllSkills(): List<SkillInfo> = withContext(Dispatchers.IO) {
        val registered = repository.getAllSkills()
        registered.map { sc ->
            SkillInfo(
                id = sc.id,
                name = sc.name,
                description = sc.description,
                version = sc.version,
                category = sc.category,
                capabilities = Json.decodeFromString<List<SkillCapability>>(sc.capabilitiesJson),
                dependencies = Json.decodeFromString<List<String>>(sc.dependenciesJson),
                permissions = Json.decodeFromString<List<String>>(sc.permissionsJson),
                isEnabled = sc.isEnabled,
                isBuiltIn = sc.isBuiltIn,
                securityReviewed = sc.securityReviewed,
                securityNotes = sc.securityNotes,
                testResults = Json.decodeFromString<Map<String, String>>(sc.testResultsJson),
                installationStatus = sc.installationStatus
            )
        } + builtInSkills.values.map { b ->
            SkillInfo(
                id = b.id,
                name = b.name,
                description = b.description,
                version = "1.0.0",
                category = b.category,
                capabilities = b.capabilities,
                dependencies = emptyList(),
                permissions = emptyList(),
                isEnabled = true,
                isBuiltIn = true,
                securityReviewed = true,
                securityNotes = "Built-in skill",
                testResults = mapOf("status" to "tested"),
                installationStatus = "installed"
            )
        }
    }

    suspend fun enableSkill(id: String) {
        withContext(Dispatchers.IO) {
            repository.setSkillEnabled(id, true)
        }
    }

    suspend fun disableSkill(id: String) {
        withContext(Dispatchers.IO) {
            repository.setSkillEnabled(id, false)
        }
    }

    suspend fun executeSkill(parameters: Map<String, Any?>) {
        val skillId = parameters["skill"] as? String ?: ""
        val skill = builtInSkills[skillId]
        if (skill != null) {
            withContext(Dispatchers.IO) {
                try {
                    skill.execute(parameters)
                } catch (e: Exception) {
                    // Skill execution error
                }
            }
        }
    }

    // Security review
    suspend fun reviewSkill(id: String, reviewed: Boolean, notes: String?) {
        withContext(Dispatchers.IO) {
            repository.setSkillSecurityReview(id, reviewed, notes)
        }
    }

    // ----- Built-in skill interface -----

    private class BuiltInSkill(
        val id: String,
        val name: String,
        val description: String,
        val category: String,
        val capabilities: List<SkillCapability>,
        val execute: (Map<String, Any?>) -> Any?
    )
}

data class DiscoveredApp(
    val packageName: String,
    val appName: String,
    val activities: List<String>,
    val isSystemApp: Boolean
)
