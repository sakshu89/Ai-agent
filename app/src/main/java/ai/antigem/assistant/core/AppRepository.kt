package ai.antigem.assistant.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ============================================================================
// Repository - single source of truth for all data
// ============================================================================

class AppRepository(private val db: AppDatabase) {

    // ---------- Conversations ----------
    suspend fun insertConversation(c: ConversationEntity) = db.conversationDao().insert(c)
    suspend fun insertConversations(list: List<ConversationEntity>) = db.conversationDao().insertAll(list)
    suspend fun updateConversationTitle(id: String, title: String, updatedAt: Long) =
        db.conversationDao().updateTitle(id, title, updatedAt)
    suspend fun setConversationArchived(id: String, archived: Boolean) =
        db.conversationDao().setArchived(id, archived)
    suspend fun setConversationPinned(id: String, pinned: Boolean) =
        db.conversationDao().setPinned(id, pinned)
    suspend fun setConversationFolder(id: String, folderId: String?) =
        db.conversationDao().setFolder(id, folderId)
    suspend fun setConversationModelServer(id: String, modelId: String?, serverId: String?) =
        db.conversationDao().setModelServer(id, modelId, serverId)
    suspend fun updateConversationSummary(
        id: String, summary: String?, count: Int, preview: String?
    ) = db.conversationDao().updateSummary(id, summary, count, preview)
    suspend fun deleteConversation(c: ConversationEntity) = db.conversationDao().delete(c)
    suspend fun deleteConversationById(id: String) = db.conversationDao().deleteById(id)
    suspend fun getAllConversations() = db.conversationDao().getAll()
    suspend fun getActiveConversations() = db.conversationDao().getActive()
    suspend fun getArchivedConversations() = db.conversationDao().getArchived()
    suspend fun getConversationById(id: String) = db.conversationDao().getById(id)
    suspend fun searchConversations(query: String) = db.conversationDao().search(query)
    fun getActiveConversationsFlow(): Flow<List<ConversationEntity>> =
        db.conversationDao().getActiveFlow()
    fun getConversationFlow(id: String): Flow<ConversationEntity?> =
        db.conversationDao().getByIdFlow(id)

    // ---------- Messages ----------
    suspend fun insertMessage(m: MessageEntity) = db.messageDao().insert(m)
    suspend fun insertMessages(list: List<MessageEntity>) = db.messageDao().insertAll(list)
    suspend fun updateMessageContent(id: String, content: String, editedAt: Long) =
        db.messageDao().updateContent(id, content, editedAt)
    suspend fun deleteMessage(m: MessageEntity) = db.messageDao().delete(m)
    suspend fun deleteMessageById(id: String) = db.messageDao().deleteById(id)
    suspend fun deleteAllMessagesForConversation(conversationId: String) =
        db.messageDao().deleteAllForConversation(conversationId)
    suspend fun getMessagesByConversation(conversationId: String) =
        db.messageDao().getByConversation(conversationId)
    suspend fun getRecentMessages(conversationId: String, limit: Int) =
        db.messageDao().getRecent(conversationId, limit)
    suspend fun searchMessagesInConversation(conversationId: String, query: String) =
        db.messageDao().searchInConversation(conversationId, query)
    suspend fun getMessageById(id: String) = db.messageDao().getById(id)
    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>> =
        db.messageDao().getByConversationFlow(conversationId)

    // ---------- Memories ----------
    suspend fun insertMemory(m: MemoryEntity) = db.memoryDao().insert(m)
    suspend fun insertMemories(list: List<MemoryEntity>) = db.memoryDao().insertAll(list)
    suspend fun updateMemory(
        id: String, content: String, confidence: Float, tags: String?,
        approved: Boolean, updatedAt: Long
    ) = db.memoryDao().update(id, content, confidence, tags, approved, updatedAt)
    suspend fun deleteMemory(m: MemoryEntity) = db.memoryDao().delete(m)
    suspend fun deleteMemoryById(id: String) = db.memoryDao().deleteById(id)
    suspend fun getAllActiveMemories() = db.memoryDao().getAllActive()
    suspend fun getMemoriesByCategory(category: String) = db.memoryDao().getByCategory(category)
    suspend fun searchMemories(query: String) = db.memoryDao().search(query)
    suspend fun getMemoryById(id: String) = db.memoryDao().getById(id)
    suspend fun getRelevantMemories(category: String, query: String, limit: Int) =
        db.memoryDao().getRelevant(category, query, limit)
    fun getAllActiveMemoriesFlow(): Flow<List<MemoryEntity>> =
        db.memoryDao().getAllActiveFlow()

    // ---------- Conversation Summaries ----------
    suspend fun insertSummary(s: ConversationSummaryEntity) = db.conversationSummaryDao().insert(s)
    suspend fun getSummaryByConversation(conversationId: String) =
        db.conversationSummaryDao().getByConversation(conversationId)
    suspend fun deleteSummaryByConversation(conversationId: String) =
        db.conversationSummaryDao().deleteByConversation(conversationId)
    suspend fun getAllSummaries() = db.conversationSummaryDao().getAll()

    // ---------- Tasks ----------
    suspend fun insertTask(t: TaskEntity) = db.taskDao().insert(t)
    suspend fun insertTasks(list: List<TaskEntity>) = db.taskDao().insertAll(list)
    suspend fun updateTaskStatus(id: String, status: String) = db.taskDao().updateStatus(id, status)
    suspend fun updateTaskProgress(id: String, index: Int, progress: Float) =
        db.taskDao().updateProgress(id, index, progress)
    suspend fun setTaskError(id: String, error: String?) = db.taskDao().setError(id, error)
    suspend fun setTaskCompleted(id: String, completedAt: Long) =
        db.taskDao().setCompleted(id, completedAt)
    suspend fun setTaskStarted(id: String, startedAt: Long) = db.taskDao().setStarted(id, startedAt)
    suspend fun incrementTaskRetry(id: String) = db.taskDao().incrementRetry(id)
    suspend fun deleteTask(t: TaskEntity) = db.taskDao().delete(t)
    suspend fun deleteTaskById(id: String) = db.taskDao().deleteById(id)
    suspend fun getAllTasks() = db.taskDao().getAll()
    suspend fun getTasksByStatus(status: String) = db.taskDao().getByStatus(status)
    suspend fun getTaskById(id: String) = db.taskDao().getById(id)
    suspend fun getTasksByConversation(conversationId: String) =
        db.taskDao().getByConversation(conversationId)
    suspend fun getActiveTasks() = db.taskDao().getActive()
    fun getTaskFlow(id: String): Flow<TaskEntity?> = db.taskDao().getByIdFlow(id)

    // ---------- Task Steps ----------
    suspend fun insertTaskStep(s: TaskStepEntity) = db.taskStepDao().insert(s)
    suspend fun insertTaskSteps(list: List<TaskStepEntity>) = db.taskStepDao().insertAll(list)
    suspend fun updateTaskStepResult(
        id: String, status: String, result: String?, error: String?, executedAt: Long?
    ) = db.taskStepDao().updateResult(id, status, result, error, executedAt)
    suspend fun incrementTaskStepRetry(id: String) = db.taskStepDao().incrementRetry(id)
    suspend fun deleteTaskStepsForTask(taskId: String) =
        db.taskStepDao().deleteAllForTask(taskId)
    suspend fun getTaskStepsByTask(taskId: String) = db.taskStepDao().getByTask(taskId)
    suspend fun getTaskStepById(id: String) = db.taskStepDao().getById(id)

    // ---------- Automation Workflows ----------
    suspend fun insertWorkflow(w: AutomationWorkflowEntity) =
        db.automationWorkflowDao().insert(w)
    suspend fun insertWorkflows(list: List<AutomationWorkflowEntity>) =
        db.automationWorkflowDao().insertAll(list)
    suspend fun deleteWorkflow(w: AutomationWorkflowEntity) =
        db.automationWorkflowDao().delete(w)
    suspend fun deleteWorkflowById(id: String) = db.automationWorkflowDao().deleteById(id)
    suspend fun setWorkflowEnabled(id: String, enabled: Boolean) =
        db.automationWorkflowDao().setEnabled(id, enabled)
    suspend fun recordWorkflowRun(id: String, runAt: Long) =
        db.automationWorkflowDao().recordRun(id, runAt)
    suspend fun getAllWorkflows() = db.automationWorkflowDao().getAll()
    suspend fun getEnabledWorkflows() = db.automationWorkflowDao().getEnabled()
    suspend fun getWorkflowById(id: String) = db.automationWorkflowDao().getById(id)

    // ---------- Skill Configs ----------
    suspend fun insertSkill(s: SkillConfigEntity) = db.skillConfigDao().insert(s)
    suspend fun insertSkills(list: List<SkillConfigEntity>) = db.skillConfigDao().insertAll(list)
    suspend fun deleteSkill(s: SkillConfigEntity) = db.skillConfigDao().delete(s)
    suspend fun deleteSkillById(id: String) = db.skillConfigDao().deleteById(id)
    suspend fun setSkillEnabled(id: String, enabled: Boolean) =
        db.skillConfigDao().setEnabled(id, enabled)
    suspend fun setSkillSecurityReview(id: String, reviewed: Boolean, notes: String?) =
        db.skillConfigDao().setSecurityReview(id, reviewed, notes)
    suspend fun setSkillInstallationStatus(id: String, status: String) =
        db.skillConfigDao().setInstallationStatus(id, status)
    suspend fun getAllSkills() = db.skillConfigDao().getAll()
    suspend fun getEnabledSkills() = db.skillConfigDao().getEnabled()
    suspend fun getSkillsByCategory(category: String) = db.skillConfigDao().getByCategory(category)
    suspend fun getSkillById(id: String) = db.skillConfigDao().getById(id)

    // ---------- App Config ----------
    suspend fun insertAppConfig(c: AppConfigEntity) = db.appConfigDao().insert(c)
    suspend fun getAppConfigByKey(key: String) = db.appConfigDao().getByKey(key)
    suspend fun getAllAppConfigs() = db.appConfigDao().getAll()
    suspend fun deleteAppConfigByKey(key: String) = db.appConfigDao().deleteByKey(key)
    fun getAppConfigFlow(key: String): Flow<AppConfigEntity?> =
        db.appConfigDao().getByKeyFlow(key)

    // ---------- Server Configs ----------
    suspend fun insertServer(s: ServerConfigEntity) = db.serverConfigDao().insert(s)
    suspend fun insertServers(list: List<ServerConfigEntity>) = db.serverConfigDao().insertAll(list)
    suspend fun deleteServer(s: ServerConfigEntity) = db.serverConfigDao().delete(s)
    suspend fun deleteServerById(id: String) = db.serverConfigDao().deleteById(id)
    suspend fun setServerConnectionStatus(
        id: String, connected: Boolean, testedAt: Long, result: String?
    ) = db.serverConfigDao().setConnectionStatus(id, connected, testedAt, result)
    suspend fun clearDefaultServer() = db.serverConfigDao().clearDefault()
    suspend fun setDefaultServer(id: String) = db.serverConfigDao().setDefault(id)
    suspend fun getAllServers() = db.serverConfigDao().getAll()
    suspend fun getDefaultServer() = db.serverConfigDao().getDefault()
    suspend fun getServerById(id: String) = db.serverConfigDao().getById(id)
    suspend fun getConnectedServers() = db.serverConfigDao().getConnected()

    // ---------- Preferences ----------
    suspend fun insertPreference(p: PreferenceEntity) = db.preferenceDao().insert(p)
    suspend fun insertPreferences(list: List<PreferenceEntity>) = db.preferenceDao().insertAll(list)
    suspend fun getPreferenceByKey(key: String) = db.preferenceDao().getByKey(key)
    suspend fun getPreferencesByCategory(category: String) = db.preferenceDao().getByCategory(category)
    suspend fun getAllPreferences() = db.preferenceDao().getAll()
    suspend fun deletePreferenceByKey(key: String) = db.preferenceDao().deleteByKey(key)
    fun getPreferenceFlow(key: String): Flow<PreferenceEntity?> =
        db.preferenceDao().getByKeyFlow(key)

    // ---------- Attachments ----------
    suspend fun insertAttachment(a: AttachmentEntity) = db.attachmentDao().insert(a)
    suspend fun insertAttachments(list: List<AttachmentEntity>) = db.attachmentDao().insertAll(list)
    suspend fun deleteAttachment(a: AttachmentEntity) = db.attachmentDao().delete(a)
    suspend fun deleteAttachmentById(id: String) = db.attachmentDao().deleteById(id)
    suspend fun deleteAttachmentsByMessage(messageId: String) =
        db.attachmentDao().deleteByMessage(messageId)
    suspend fun getAttachmentsByMessage(messageId: String) =
        db.attachmentDao().getByMessage(messageId)
    suspend fun getAttachmentsByConversation(conversationId: String) =
        db.attachmentDao().getByConversation(conversationId)
    suspend fun getAttachmentById(id: String) = db.attachmentDao().getById(id)

    // ---------- Agent Execution States ----------
    suspend fun insertAgentState(s: AgentExecutionStateEntity) =
        db.agentExecutionStateDao().insert(s)
    suspend fun updateAgentState(
        taskId: String, status: String, plan: String?, observation: String?,
        context: String, variables: String, action: String?, result: String?,
        errors: Int, updatedAt: Long
    ) = db.agentExecutionStateDao().update(
        taskId, status, plan, observation, context, variables, action, result, errors, updatedAt
    )
    suspend fun deleteAgentStateByTask(taskId: String) =
        db.agentExecutionStateDao().deleteByTask(taskId)
    suspend fun getAgentStateByTask(taskId: String) =
        db.agentExecutionStateDao().getByTask(taskId)
    suspend fun getActiveAgentStates() = db.agentExecutionStateDao().getActive()
    suspend fun getAgentStateById(id: String) = db.agentExecutionStateDao().getById(id)
}
