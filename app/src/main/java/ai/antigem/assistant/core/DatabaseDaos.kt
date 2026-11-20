package ai.antigem.assistant.core

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET updated_at = :updatedAt, title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String, updatedAt: Long)

    @Query("UPDATE conversations SET is_archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE conversations SET is_pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET folder_id = :folderId WHERE id = :id")
    suspend fun setFolder(id: String, folderId: String?)

    @Query("UPDATE conversations SET model_id = :modelId, server_id = :serverId WHERE id = :id")
    suspend fun setModelServer(id: String, modelId: String?, serverId: String?)

    @Query("UPDATE conversations SET summary = :summary, message_count = :count, last_message_preview = :preview WHERE id = :id")
    suspend fun updateSummary(id: String, summary: String?, count: Int, preview: String?)

    @Delete
    suspend fun delete(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM conversations ORDER BY is_pinned DESC, updated_at DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    suspend fun getActive(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE is_archived = 1 ORDER BY updated_at DESC")
    suspend fun getArchived(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    @Query("""
        SELECT * FROM conversations 
        WHERE title LIKE :query OR last_message_preview LIKE :query
        ORDER BY is_pinned DESC, updated_at DESC
    """)
    suspend fun search(query: String): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getActiveFlow(): Flow<List<ConversationEntity>>
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET content = :content, is_edited = 1, edited_at = :editedAt WHERE id = :id")
    suspend fun updateContent(id: String, content: String, editedAt: Long)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteAllForConversation(conversationId: String)

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    suspend fun getByConversation(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecent(conversationId: String, limit: Int): List<MessageEntity>

    @Query("""
        SELECT * FROM messages 
        WHERE conversation_id = :conversationId AND content LIKE :query
        ORDER BY created_at DESC
    """)
    suspend fun searchInConversation(conversationId: String, query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun getByConversationFlow(conversationId: String): Flow<List<MessageEntity>>
}

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)

    @Query("UPDATE memories SET content = :content, confidence = :confidence, tags = :tags, is_user_approved = :approved, edit_count = edit_count + 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun update(
        id: String,
        content: String,
        confidence: Float,
        tags: String?,
        approved: Boolean,
        updatedAt: Long
    )

    @Delete
    suspend fun delete(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM memories WHERE is_expired = 0 ORDER BY confidence DESC, created_at DESC")
    suspend fun getAllActive(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE category = :category AND is_expired = 0 ORDER BY confidence DESC, created_at DESC")
    suspend fun getByCategory(category: String): List<MemoryEntity>

    @Query("""
        SELECT * FROM memories 
        WHERE is_expired = 0 AND (content LIKE :query OR tags LIKE :query)
        ORDER BY confidence DESC, created_at DESC
    """)
    suspend fun search(query: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getById(id: String): MemoryEntity?

    @Query("""
        SELECT * FROM memories 
        WHERE is_expired = 0 AND category = :category AND (content LIKE :query OR tags LIKE :query)
        ORDER BY confidence DESC
        LIMIT :limit
    """)
    suspend fun getRelevant(category: String, query: String, limit: Int): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE is_expired = 0")
    fun getAllActiveFlow(): Flow<List<MemoryEntity>>
}

@Dao
interface ConversationSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: ConversationSummaryEntity)

    @Query("SELECT * FROM conversation_summaries WHERE conversation_id = :conversationId")
    suspend fun getByConversation(conversationId: String): ConversationSummaryEntity?

    @Query("DELETE FROM conversation_summaries WHERE conversation_id = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    @Query("SELECT * FROM conversation_summaries ORDER BY created_at DESC")
    suspend fun getAll(): List<ConversationSummaryEntity>
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE tasks SET current_step_index = :index, progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, index: Int, progress: Float)

    @Query("UPDATE tasks SET error_message = :error WHERE id = :id")
    suspend fun setError(id: String, error: String?)

    @Query("UPDATE tasks SET completed_at = :completedAt WHERE id = :id")
    suspend fun setCompleted(id: String, completedAt: Long)

    @Query("UPDATE tasks SET started_at = :startedAt WHERE id = :id")
    suspend fun setStarted(id: String, startedAt: Long)

    @Query("UPDATE tasks SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY created_at DESC")
    suspend fun getByStatus(status: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    suspend fun getByConversation(conversationId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status IN ('running', 'paused') ORDER BY created_at DESC")
    suspend fun getActive(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getByIdFlow(id: String): Flow<TaskEntity?>
}

@Dao
interface TaskStepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(step: TaskStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<TaskStepEntity>)

    @Query("UPDATE task_steps SET status = :status, result = :result, error_message = :error, executed_at = :executedAt WHERE id = :id")
    suspend fun updateResult(
        id: String,
        status: String,
        result: String?,
        error: String?,
        executedAt: Long?
    )

    @Query("UPDATE task_steps SET retry_count = retry_count + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)

    @Query("DELETE FROM task_steps WHERE task_id = :taskId")
    suspend fun deleteAllForTask(taskId: String)

    @Query("SELECT * FROM task_steps WHERE task_id = :taskId ORDER BY step_index ASC")
    suspend fun getByTask(taskId: String): List<TaskStepEntity>

    @Query("SELECT * FROM task_steps WHERE id = :id")
    suspend fun getById(id: String): TaskStepEntity?
}

@Dao
interface AutomationWorkflowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workflow: AutomationWorkflowEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workflows: List<AutomationWorkflowEntity>)

    @Delete
    suspend fun delete(workflow: AutomationWorkflowEntity)

    @Query("DELETE FROM automation_workflows WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE automation_workflows SET is_enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE automation_workflows SET last_run_at = :runAt, run_count = run_count + 1 WHERE id = :id")
    suspend fun recordRun(id: String, runAt: Long)

    @Query("SELECT * FROM automation_workflows ORDER BY created_at DESC")
    suspend fun getAll(): List<AutomationWorkflowEntity>

    @Query("SELECT * FROM automation_workflows WHERE is_enabled = 1 ORDER BY created_at DESC")
    suspend fun getEnabled(): List<AutomationWorkflowEntity>

    @Query("SELECT * FROM automation_workflows WHERE id = :id")
    suspend fun getById(id: String): AutomationWorkflowEntity?
}

@Dao
interface SkillConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: SkillConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<SkillConfigEntity>)

    @Delete
    suspend fun delete(skill: SkillConfigEntity)

    @Query("DELETE FROM skill_configs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE skill_configs SET is_enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE skill_configs SET security_reviewed = :reviewed, security_notes = :notes WHERE id = :id")
    suspend fun setSecurityReview(id: String, reviewed: Boolean, notes: String?)

    @Query("UPDATE skill_configs SET installation_status = :status WHERE id = :id")
    suspend fun setInstallationStatus(id: String, status: String)

    @Query("SELECT * FROM skill_configs ORDER BY name ASC")
    suspend fun getAll(): List<SkillConfigEntity>

    @Query("SELECT * FROM skill_configs WHERE is_enabled = 1 ORDER BY name ASC")
    suspend fun getEnabled(): List<SkillConfigEntity>

    @Query("SELECT * FROM skill_configs WHERE category = :category ORDER BY name ASC")
    suspend fun getByCategory(category: String): List<SkillConfigEntity>

    @Query("SELECT * FROM skill_configs WHERE id = :id")
    suspend fun getById(id: String): SkillConfigEntity?
}

@Dao
interface AppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: AppConfigEntity)

    @Query("SELECT * FROM app_configs WHERE key = :key")
    suspend fun getByKey(key: String): AppConfigEntity?

    @Query("SELECT * FROM app_configs")
    suspend fun getAll(): List<AppConfigEntity>

    @Query("DELETE FROM app_configs WHERE key = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM app_configs WHERE key = :key")
    fun getByKeyFlow(key: String): Flow<AppConfigEntity?>
}

@Dao
interface ServerConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: ServerConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(servers: List<ServerConfigEntity>)

    @Delete
    suspend fun delete(server: ServerConfigEntity)

    @Query("DELETE FROM server_configs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE server_configs SET is_connected = :connected, last_tested_at = :testedAt, last_test_result = :result WHERE id = :id")
    suspend fun setConnectionStatus(id: String, connected: Boolean, testedAt: Long, result: String?)

    @Query("UPDATE server_configs SET is_default = 0 WHERE is_default = 1")
    suspend fun clearDefault()

    @Query("UPDATE server_configs SET is_default = 1 WHERE id = :id")
    suspend fun setDefault(id: String)

    @Query("SELECT * FROM server_configs ORDER BY name ASC")
    suspend fun getAll(): List<ServerConfigEntity>

    @Query("SELECT * FROM server_configs WHERE is_default = 1")
    suspend fun getDefault(): ServerConfigEntity?

    @Query("SELECT * FROM server_configs WHERE id = :id")
    suspend fun getById(id: String): ServerConfigEntity?

    @Query("SELECT * FROM server_configs WHERE is_connected = 1")
    suspend fun getConnected(): List<ServerConfigEntity>
}

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: PreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preferences: List<PreferenceEntity>)

    @Query("SELECT * FROM preferences WHERE key = :key")
    suspend fun getByKey(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences WHERE category = :category")
    suspend fun getByCategory(category: String): List<PreferenceEntity>

    @Query("SELECT * FROM preferences")
    suspend fun getAll(): List<PreferenceEntity>

    @Query("DELETE FROM preferences WHERE key = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM preferences WHERE key = :key")
    fun getByKeyFlow(key: String): Flow<PreferenceEntity?>
}

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM attachments WHERE message_id = :messageId")
    suspend fun deleteByMessage(messageId: String)

    @Query("SELECT * FROM attachments WHERE message_id = :messageId")
    suspend fun getByMessage(messageId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE conversation_id = :conversationId")
    suspend fun getByConversation(conversationId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?
}

@Dao
interface AgentExecutionStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: AgentExecutionStateEntity)

    @Query("UPDATE agent_execution_states SET status = :status, current_plan = :plan, current_observation = :observation, context_json = :context, variables_json = :variables, last_action = :action, last_result = :result, error_count = :errors, updated_at = :updatedAt WHERE task_id = :taskId")
    suspend fun update(
        taskId: String,
        status: String,
        plan: String?,
        observation: String?,
        context: String,
        variables: String,
        action: String?,
        result: String?,
        errors: Int,
        updatedAt: Long
    )

    @Query("DELETE FROM agent_execution_states WHERE task_id = :taskId")
    suspend fun deleteByTask(taskId: String)

    @Query("SELECT * FROM agent_execution_states WHERE task_id = :taskId")
    suspend fun getByTask(taskId: String): AgentExecutionStateEntity?

    @Query("SELECT * FROM agent_execution_states WHERE status = 'running' OR status = 'paused'")
    suspend fun getActive(): List<AgentExecutionStateEntity>

    @Query("SELECT * FROM agent_execution_states WHERE id = :id")
    suspend fun getById(id: String): AgentExecutionStateEntity?
}
