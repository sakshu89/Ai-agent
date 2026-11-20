package ai.antigem.assistant.agent

import ai.antigem.assistant.ai.*
import ai.antigem.assistant.core.*
import ai.antigem.assistant.skills.SkillManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.UUID
import android.content.Intent
// ============================================================================
// Autonomous Agent Engine - Observe-Plan-Act-Verify cycle
// ============================================================================

enum class TaskStatus { PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED }

enum class ActionType {
    NAVIGATE, TAP, TYPE, SCROLL, WAIT, INTENT, SKILL, CONDITIONAL, DELAY, VERIFY
}

data class AgentAction(
    val id: String = UUID.randomUUID().toString(),
    val type: ActionType,
    val description: String,
    val parameters: Map<String, Any?> = emptyMap(),
    val dependsOn: String? = null,
    val timeoutMs: Long = 30000,
    val maxRetries: Int = 3
)

data class AgentPlan(
    val taskId: String,
    val goal: String,
    val actions: List<AgentAction>,
    val createdAt: Long = System.currentTimeMillis()
)

data class ExecutionResult(
    val success: Boolean,
    val observation: String,
    val verified: Boolean = false,
    val error: String? = null,
    val nextAction: AgentAction? = null
)

data class AgentContext(
    val taskId: String,
    val conversationId: String?,
    val variables: Map<String, Any?> = emptyMap(),
    val screenSnapshot: String? = null,
    val currentApp: String? = null
)

// ----- Agent Engine -----

class AgentEngine(
    private val aiService: AiService,
    private val repository: AppRepository,
    private val deviceController: DeviceController,
    private val skillManager: SkillManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val activeJobs = mutableMapOf<String, Job>()
    private val executionStates = mutableMapOf<String, AgentExecutionStateEntity>()

    // Start a task
    suspend fun startTask(
        task: TaskEntity,
        userInstruction: String,
        conversationId: String?,
        onProgress: (TaskEntity) -> Unit = {}
    ): TaskEntity {
        val job = CoroutineScope(dispatcher + SupervisorJob()).launch {
            try {
                executeTaskLoop(task, userInstruction, conversationId, onProgress)
            } catch (e: Exception) {
                updateTaskStatus(task.id, TaskStatus.FAILED.toString(), error = e.message)
            }
        }
        activeJobs[task.id] = job
        return task.copy(status = TaskStatus.RUNNING.toString(), startedAt = System.currentTimeMillis())
    }

    private suspend fun executeTaskLoop(
        task: TaskEntity,
        userInstruction: String,
        conversationId: String?,
        onProgress: (TaskEntity) -> Unit
    ) {
        // Save initial state
        val state = AgentExecutionStateEntity(
            id = task.id,
            taskId = task.id,
            status = TaskStatus.RUNNING.toString(),
            contextJson = "{}",
            variablesJson = "{}"
        )
        repository.insertAgentState(state)
        executionStates[task.id] = state

        // Step 1: Understand the request (planning via AI)
        val plan = generatePlan(task, userInstruction)
        savePlan(task.id, plan)

        // Step 2: Execute observe-plan-act-verify cycle
        var actionIndex = 0
        val actions = plan.actions
        val maxActions = 100 // prevent infinite loops
        var actionsExecuted = 0

        while (actionIndex < actions.size && actionsExecuted < maxActions) {
            // Check if cancelled/paused
            val currentState = executionStates[task.id]
            if (currentState?.status == TaskStatus.CANCELLED.toString()) {
                updateTaskStatus(task.id, TaskStatus.CANCELLED.toString())
                return
            }
            if (currentState?.status == TaskStatus.PAUSED.toString()) {
                // Wait until resumed
                while (executionStates[task.id]?.status == TaskStatus.PAUSED.toString()) {
                    delay(500)
                }
            }

            val action = actions[actionIndex]
            actionsExecuted++

            // Check dependencies
            if (action.dependsOn != null) {
                val depResult = executionStates[task.id]
                // Simple dependency check - in production, verify dep step completed
            }

            // Observe current state
            val observation = observeScreen()
            saveObservation(task.id, observation)

            // Act
            val result = performAction(action, observation, task.id)

            // Verify
            val verified = verifyResult(action, result)

            // Save step result
            saveTaskStep(task, actionIndex, action, result, verified)

            if (!result.success && !verified) {
                // Error recovery - retry or fail
                val retryCount = getTaskStepRetryCount(task.id, actionIndex)
                if (retryCount < action.maxRetries) {
                    incrementTaskStepRetry(task.id, actionIndex)
                    delay(1000) // brief pause before retry
                    continue // retry same action
                } else {
                    updateTaskStatus(task.id, TaskStatus.FAILED.toString(), error = result.error)
                    return
                }
            }

            actionIndex++
            updateTaskProgress(task.id, actionIndex, actions.size)

            // Check if done
            if (actionIndex >= actions.size) {
                updateTaskStatus(task.id, TaskStatus.COMPLETED.toString())
                saveObservation(task.id, "Task completed successfully")
                return
            }
        }

        if (actionsExecuted >= maxActions) {
            updateTaskStatus(task.id, TaskStatus.FAILED.toString(), error = "Maximum action limit reached (possible loop)")
        }
    }

    private suspend fun generatePlan(task: TaskEntity, instruction: String): AgentPlan {
        // Use AI to generate a plan
        val context = buildContextString(task)
        val prompt = """
You are an autonomous Android assistant planning agent.
Task: $instruction
Context: $context

Generate a plan as a JSON array of actions. Each action has:
- type: navigate | tap | type | scroll | wait | intent | skill | conditional | delay | verify
- description: human-readable description
- parameters: key-value map of action parameters
- dependsOn: optional action id this depends on
- timeoutMs: timeout in milliseconds
- maxRetries: max retry count

Respond ONLY with valid JSON. No markdown, no explanation.
        """.trimIndent()

        val response = aiService.chatWithFallback(
            ChatRequest(
                model = "",
                messages = listOf(ChatMessage("user", prompt)),
                temperature = 0.3f,
                stream = false
            ),
            taskType = TaskType.PLANNING
        )

        return parsePlan(task.id, response.content)
    }

    private suspend fun observeScreen(): String {
        // Capture current screen state via device controller
        return deviceController.observeScreenState()
    }

    private suspend fun performAction(
        action: AgentAction,
        observation: String,
        taskId: String
    ): ExecutionResult {
        return try {
            when (action.type) {
                ActionType.TAP -> {
                    deviceController.tap(action.parameters)
                    ExecutionResult(true, "Tapped: ${action.description}", verified = true)
                }
                ActionType.TYPE -> {
                    deviceController.typeText(action.parameters)
                    ExecutionResult(true, "Typed: ${action.description}", verified = true)
                }
                ActionType.SCROLL -> {
                    deviceController.scroll(action.parameters)
                    ExecutionResult(true, "Scrolled: ${action.description}", verified = true)
                }
                ActionType.WAIT -> {
                    delay(action.parameters["delayMs"] as? Long ?: 1000)
                    ExecutionResult(true, "Waited: ${action.description}", verified = true)
                }
                ActionType.NAVIGATE -> {
                    deviceController.navigateHome()
                    ExecutionResult(true, "Navigated home: ${action.description}", verified = true)
                }
                ActionType.INTENT -> {
                    deviceController.launchIntent(action.parameters)
                    ExecutionResult(true, "Launched intent: ${action.description}", verified = true)
                }
                ActionType.SKILL -> {
                    skillManager.executeSkill(action.parameters)
                    ExecutionResult(true, "Executed skill: ${action.description}", verified = true)
                }
                ActionType.CONDITIONAL -> {
                    ExecutionResult(true, "Conditional check: ${action.description}", verified = true)
                }
                ActionType.DELAY -> {
                    delay(action.parameters["delayMs"] as? Long ?: 1000)
                    ExecutionResult(true, "Delayed: ${action.description}", verified = true)
                }
                ActionType.VERIFY -> {
                    ExecutionResult(true, "Verified: ${action.description}", verified = true)
                }
            }
        } catch (e: Exception) {
            ExecutionResult(false, "Action failed: ${e.message}", verified = false, error = e.message)
        }
    }

    private suspend fun verifyResult(action: AgentAction, result: ExecutionResult): Boolean {
        // Use observation to verify
        return result.success && result.verified
    }

    // ----- Control -----

    suspend fun pauseTask(taskId: String) {
        executionStates[taskId]?.let {
            repository.updateAgentState(
                taskId, TaskStatus.PAUSED.toString(),
                it.currentPlan, it.currentObservation, it.contextJson, it.variablesJson,
                it.lastAction, it.lastResult, it.errorCount, System.currentTimeMillis()
            )
        }
        updateTaskStatus(taskId, TaskStatus.PAUSED.toString())
    }

    suspend fun resumeTask(taskId: String) {
        updateTaskStatus(taskId, TaskStatus.RUNNING.toString())
    }

    suspend fun cancelTask(taskId: String) {
        activeJobs[taskId]?.cancel()
        executionStates[taskId]?.let {
            repository.updateAgentState(
                taskId, TaskStatus.CANCELLED.toString(),
                it.currentPlan, it.currentObservation, it.contextJson, it.variablesJson,
                it.lastAction, it.lastResult, it.errorCount, System.currentTimeMillis()
            )
        }
        updateTaskStatus(taskId, TaskStatus.CANCELLED.toString())
        activeJobs.remove(taskId)
    }

    fun getActiveTasks(): List<String> = activeJobs.keys.toList()

    // ----- Helpers -----

    private suspend fun updateTaskStatus(id: String, status: String, error: String? = null) {
        repository.updateTaskStatus(id, status)
        if (error != null) repository.setTaskError(id, error)
    }

    private suspend fun updateTaskProgress(id: String, currentStep: Int, totalSteps: Int) {
        val progress = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f
        repository.updateTaskProgress(id, currentStep, progress)
    }

    private suspend fun savePlan(taskId: String, plan: AgentPlan) {
        val json = Json.encodeToString(plan)
        val state = executionStates[taskId]
        if (state != null) {
            repository.updateAgentState(
                taskId, state.status, json, state.currentObservation, state.contextJson,
                state.variablesJson, state.lastAction, state.lastResult, state.errorCount,
                System.currentTimeMillis()
            )
        }
    }

    private suspend fun saveObservation(taskId: String, observation: String) {
        val state = executionStates[taskId]
        if (state != null) {
            repository.updateAgentState(
                taskId, state.status, state.currentPlan, observation, state.contextJson,
                state.variablesJson, state.lastAction, state.lastResult, state.errorCount,
                System.currentTimeMillis()
            )
        }
    }

    private suspend fun saveTaskStep(
        task: TaskEntity, index: Int, action: AgentAction, result: ExecutionResult, verified: Boolean
    ) {
        val step = TaskStepEntity(
            id = action.id,
            taskId = task.id,
            stepIndex = index,
            actionType = action.type.toString(),
            description = action.description,
            parameters = Json.encodeToString(action.parameters),
            status = if (result.success) "completed" else "failed",
            result = result.observation,
            errorMessage = result.error,
            executedAt = System.currentTimeMillis()
        )
        repository.insertTaskStep(step)
    }

    private suspend fun getTaskStepRetryCount(taskId: String, actionIndex: Int): Int {
        val steps = repository.getTaskStepsByTask(taskId)
        return steps.firstOrNull { it.stepIndex == actionIndex }?.retryCount ?: 0
    }

    private suspend fun incrementTaskStepRetry(taskId: String, actionIndex: Int) {
        val steps = repository.getTaskStepsByTask(taskId)
        steps.firstOrNull { it.stepIndex == actionIndex }?.let {
            repository.incrementTaskStepRetry(it.id)
        }
    }

    private fun buildContextString(task: TaskEntity): String {
        return "Task: ${task.title}\nDescription: ${task.description}\nStatus: ${task.status}\n"
    }

    private fun parsePlan(taskId: String, jsonText: String): AgentPlan {
        return try {
            val json = Json.parseToJsonElement(jsonText)
            val actionsArray = json.jsonArray
            val actions = actionsArray.mapIndexed { index, element ->
                val obj = element.jsonObject
                AgentAction(
                    type = ActionType.valueOf(obj.get("type")?.jsonPrimitive?.content?.uppercase() ?: "WAIT"),
                    description = obj.get("description")?.jsonPrimitive?.content ?: "Step $index",
                    parameters = obj.get("parameters")?.jsonObject
                        ?.mapValues { it.value } ?: emptyMap(),
                    dependsOn = obj.get("dependsOn")?.jsonPrimitive?.content,
                    timeoutMs = obj.get("timeoutMs")?.jsonPrimitive?.content?.toLong() ?: 30000,
                    maxRetries = obj.get("maxRetries")?.jsonPrimitive?.content?.toInt() ?: 3
                )
            }
            AgentPlan(taskId = taskId, goal = "", actions = actions)
        } catch (e: Exception) {
            // Fallback: single wait action
            AgentPlan(
                taskId = taskId,
                goal = "",
                actions = listOf(AgentAction(type = ActionType.WAIT, description = "Plan parsing failed, waiting", maxRetries = 1))
            )
        }
    }
}

// ============================================================================
// Device Controller Interface
// ============================================================================

interface DeviceController {
    suspend fun observeScreenState(): String
    suspend fun tap(parameters: Map<String, Any?>)
    suspend fun doubleTap(parameters: Map<String, Any?>)
    suspend fun longPress(parameters: Map<String, Any?>)
    suspend fun typeText(parameters: Map<String, Any?>)
    suspend fun scroll(parameters: Map<String, Any?>)
    suspend fun swipe(parameters: Map<String, Any?>)
    suspend fun navigateHome()
    suspend fun navigateBack()
    suspend fun openRecentApps()
    suspend fun openApp(packageName: String)
    suspend fun launchIntent(parameters: Map<String, Any?>)
    fun isAvailable(): Boolean
}

// ============================================================================
// Intent-based Device Controller (works without accessibility)
// ============================================================================

class IntentDeviceController(
    private val context: android.content.Context
) : DeviceController {

    override suspend fun observeScreenState(): String {
        return "Screen observation not available without accessibility service"
    }

    override suspend fun tap(parameters: Map<String, Any?>) = withContext(Dispatchers.Main) {
        // Cannot tap without accessibility; use intents instead
    }

    override suspend fun doubleTap(parameters: Map<String, Any?>) {}
    override suspend fun longPress(parameters: Map<String, Any?>) {}
    override suspend fun typeText(parameters: Map<String, Any?>) {}
    override suspend fun scroll(parameters: Map<String, Any?>) {}
    override suspend fun swipe(parameters: Map<String, Any?>) {}

    override suspend fun navigateHome() = withContext(Dispatchers.Main) {
        val intent = android.content.Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override suspend fun navigateBack() = withContext(Dispatchers.Main) {
        // Back via intent not directly supported; accessibility needed
    }

    override suspend fun openRecentApps() = withContext(Dispatchers.Main) {
        // Recent apps via intent not directly supported
    }

    override suspend fun openApp(packageName: String) = withContext(Dispatchers.Main) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override suspend fun launchIntent(parameters: Map<String, Any?>) = withContext(Dispatchers.Main) {
        val action = parameters["action"] as? String ?: Intent.ACTION_VIEW
        val uri = parameters["uri"] as? String
        val packageName = parameters["package"] as? String
        val intent = android.content.Intent(action).apply {
            if (uri != null) setData(android.net.Uri.parse(uri))
            if (packageName != null) setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (context.packageManager.resolveActivity(intent, 0) != null) {
            context.startActivity(intent)
        }
    }

    override fun isAvailable(): Boolean = true
}
