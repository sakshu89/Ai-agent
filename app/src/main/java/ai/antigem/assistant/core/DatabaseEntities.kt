package ai.antigem.assistant.core

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["updated_at"]),
        Index(value = ["is_archived"]),
        Index(value = ["is_pinned"]),
        Index(value = ["folder_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "folder_id") val folderId: String? = null,
    @ColumnInfo(name = "model_id") val modelId: String? = null,
    @ColumnInfo(name = "server_id") val serverId: String? = null,
    @ColumnInfo(name = "summary") val summary: String? = null,
    @ColumnInfo(name = "message_count") val messageCount: Int = 0,
    @ColumnInfo(name = "last_message_preview") val lastMessagePreview: String? = null
)

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversation_id"]),
        Index(value = ["created_at"]),
        Index(value = ["role"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "role") val role: String, // "user", "assistant", "system", "tool"
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_edited") val isEdited: Boolean = false,
    @ColumnInfo(name = "edited_at") val editedAt: Long? = null,
    @ColumnInfo(name = "parent_id") val parentId: String? = null,
    @ColumnInfo(name = "tool_name") val toolName: String? = null,
    @ColumnInfo(name = "tool_result") val toolResult: String? = null,
    @ColumnInfo(name = "model_id") val modelId: String? = null,
    @ColumnInfo(name = "server_id") val serverId: String? = null,
    @ColumnInfo(name = "tokens_in") val tokensIn: Int = 0,
    @ColumnInfo(name = "tokens_out") val tokensOut: Int = 0,
    @ColumnInfo(name = "is_streamed") val isStreamed: Boolean = false
)

@Entity(
    tableName = "memories",
    indices = [
        Index(value = ["category"]),
        Index(value = ["confidence"]),
        Index(value = ["created_at"]),
        Index(value = ["is_expired"]),
        Index(value = ["tags"]),
    ]
)
data class MemoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "category") val category: String, // "preference", "fact", "instruction", "workflow", "contact", "task_history"
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "confidence") val confidence: Float = 1.0f, // 0.0 to 1.0
    @ColumnInfo(name = "source") val source: String = "user", // "user", "assistant", "observation", "inferred"
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "expires_at") val expiresAt: Long? = null,
    @ColumnInfo(name = "is_expired") val isExpired: Boolean = false,
    @ColumnInfo(name = "tags") val tags: String? = null, // comma-separated
    @ColumnInfo(name = "related_conversation_id") val relatedConversationId: String? = null,
    @ColumnInfo(name = "related_message_id") val relatedMessageId: String? = null,
    @ColumnInfo(name = "is_user_approved") val isUserApproved: Boolean = true,
    @ColumnInfo(name = "edit_count") val editCount: Int = 0
)

@Entity(
    tableName = "conversation_summaries",
    indices = [
        Index(value = ["conversation_id"], unique = true),
        Index(value = ["created_at"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ConversationSummaryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "summary_text") val summaryText: String,
    @ColumnInfo(name = "message_count_covered") val messageCountCovered: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "model_id") val modelId: String? = null
)

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["status"]),
        Index(value = ["created_at"]),
        Index(value = ["conversation_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "status") val status: String = "pending", // pending, running, paused, completed, failed, cancelled
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "started_at") val startedAt: Long? = null,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    @ColumnInfo(name = "conversation_id") val conversationId: String? = null,
    @ColumnInfo(name = "current_step_index") val currentStepIndex: Int = 0,
    @ColumnInfo(name = "total_steps") val totalSteps: Int = 0,
    @ColumnInfo(name = "progress") val progress: Float = 0.0f, // 0.0 to 1.0
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "max_retries") val maxRetries: Int = 3,
    @ColumnInfo(name = "timeout_ms") val timeoutMs: Long = 300000,
    @ColumnInfo(name = "can_pause") val canPause: Boolean = true,
    @ColumnInfo(name = "can_cancel") val canCancel: Boolean = true
)

@Entity(
    tableName = "task_steps",
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["status"]),
        Index(value = ["step_index"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskStepEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "step_index") val stepIndex: Int,
    @ColumnInfo(name = "action_type") val actionType: String, // "navigate", "tap", "type", "scroll", "wait", "intent", "skill", "conditional", "delay"
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "parameters") val parameters: String? = null, // JSON
    @ColumnInfo(name = "status") val status: String = "pending", // pending, running, completed, failed, skipped
    @ColumnInfo(name = "result") val result: String? = null,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
    @ColumnInfo(name = "executed_at") val executedAt: Long? = null,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0,
    @ColumnInfo(name = "depends_on_step_id") val dependsOnStepId: String? = null
)

@Entity(
    tableName = "automation_workflows",
    indices = [
        Index(value = ["name"]),
        Index(value = ["is_enabled"]),
        Index(value = ["created_at"]),
    ]
)
data class AutomationWorkflowEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "trigger_type") val triggerType: String = "manual", // manual, voice, schedule, event
    @ColumnInfo(name = "trigger_value") val triggerValue: String? = null,
    @ColumnInfo(name = "steps_json") val stepsJson: String, // JSON array of steps
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "requires_confirmation") val requiresConfirmation: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_run_at") val lastRunAt: Long? = null,
    @ColumnInfo(name = "run_count") val runCount: Int = 0
)

@Entity(
    tableName = "skill_configs",
    indices = [
        Index(value = ["name"]),
        Index(value = ["is_enabled"]),
        Index(value = ["category"]),
    ]
)
data class SkillConfigEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "version") val version: String = "1.0.0",
    @ColumnInfo(name = "category") val category: String = "general",
    @ColumnInfo(name = "capabilities_json") val capabilitiesJson: String = "[]",
    @ColumnInfo(name = "dependencies_json") val dependenciesJson: String = "[]",
    @ColumnInfo(name = "permissions_json") val permissionsJson: String = "[]",
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    @ColumnInfo(name = "is_built_in") val isBuiltIn: Boolean = false,
    @ColumnInfo(name = "security_reviewed") val securityReviewed: Boolean = false,
    @ColumnInfo(name = "security_notes") val securityNotes: String? = null,
    @ColumnInfo(name = "test_results_json") val testResultsJson: String = "{}",
    @ColumnInfo(name = "installation_status") val installationStatus: String = "not_installed", // not_installed, installed, failed
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "app_configs",
    indices = [
        Index(value = ["key"], unique = true),
    ]
)
data class AppConfigEntity(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "value_type") val valueType: String = "string", // string, int, boolean, json
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "server_configs",
    indices = [
        Index(value = ["name"]),
        Index(value = ["is_default"]),
    ]
)
data class ServerConfigEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "port") val port: Int = 443,
    @ColumnInfo(name = "api_endpoint") val apiEndpoint: String = "/v1",
    @ColumnInfo(name = "model_id") val modelId: String? = null,
    @ColumnInfo(name = "auth_type") val authType: String = "none", // none, bearer, api_key, basic
    @ColumnInfo(name = "auth_value") val authValue: String? = null, // encrypted
    @ColumnInfo(name = "context_length") val contextLength: Int = 4096,
    @ColumnInfo(name = "supports_streaming") val supportsStreaming: Boolean = true,
    @ColumnInfo(name = "supports_vision") val supportsVision: Boolean = false,
    @ColumnInfo(name = "supports_tools") val supportsTools: Boolean = false,
    @ColumnInfo(name = "supports_structured_output") val supportsStructuredOutput: Boolean = false,
    @ColumnInfo(name = "timeout_ms") val timeoutMs: Long = 60000,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 3,
    @ColumnInfo(name = "retry_delay_ms") val retryDelayMs: Long = 2000,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "is_connected") val isConnected: Boolean = false,
    @ColumnInfo(name = "last_tested_at") val lastTestedAt: Long? = null,
    @ColumnInfo(name = "last_test_result") val lastTestResult: String? = null,
    @ColumnInfo(name = "provider_type") val providerType: String = "openai", // ollama, llamacpp, openai, nemotron, generic
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") var updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "preferences",
    indices = [
        Index(value = ["key"], unique = true),
        Index(value = ["category"]),
    ]
)
data class PreferenceEntity(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "category") val category: String = "general",
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "value_type") val valueType: String = "string",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "is_sensitive") val isSensitive: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attachments",
    indices = [
        Index(value = ["message_id"]),
        Index(value = ["conversation_id"]),
        Index(value = ["mime_type"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "message_id") val messageId: String?,
    @ColumnInfo(name = "conversation_id") val conversationId: String?,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "file_size") val fileSize: Long = 0,
    @ColumnInfo(name = "uri") val uri: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_local") val isLocal: Boolean = true,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String? = null,
    @ColumnInfo(name = "width") val width: Int = 0,
    @ColumnInfo(name = "height") val height: Int = 0,
    @ColumnInfo(name = "duration_ms") val durationMs: Long = 0
)

@Entity(
    tableName = "agent_execution_states",
    indices = [
        Index(value = ["task_id"], unique = true),
        Index(value = ["status"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AgentExecutionStateEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "status") val status: String = "idle", // idle, running, paused, completed, failed, cancelled
    @ColumnInfo(name = "current_plan") val currentPlan: String? = null, // JSON
    @ColumnInfo(name = "current_observation") val currentObservation: String? = null, // JSON
    @ColumnInfo(name = "context_json") val contextJson: String = "{}",
    @ColumnInfo(name = "variables_json") val variablesJson: String = "{}",
    @ColumnInfo(name = "last_action") val lastAction: String? = null,
    @ColumnInfo(name = "last_result") val lastResult: String? = null,
    @ColumnInfo(name = "error_count") val errorCount: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
