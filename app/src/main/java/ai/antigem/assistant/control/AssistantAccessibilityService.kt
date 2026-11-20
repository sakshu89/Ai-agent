package ai.antigem.assistant.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import kotlinx.coroutines.*

// ============================================================================
// Accessibility Service - Device control via Accessibility API
// ============================================================================

class AssistantAccessibilityService : AccessibilityService() {

    private var callback: ((AccessibilityEvent) -> Unit)? = null
    private var nodeCallback: ((AccessibilityNodeInfo?) -> Unit)? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        callback?.invoke(event)
        mainHandler.post {
            nodeCallback?.invoke(rootInActiveWindow)
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    // ----- Set callbacks -----
    fun setEventCallback(cb: (AccessibilityEvent) -> Unit) {
        callback = cb
    }

    fun setNodeCallback(cb: (AccessibilityNodeInfo?) -> Unit) {
        nodeCallback = cb
    }

    // ----- Actions -----
    fun performGlobalActionWrapper(action: Int) {
        this@AssistantAccessibilityService.performGlobalAction(action)
    }

    suspend fun clickNode(node: AccessibilityNodeInfo): Boolean = withContext(Dispatchers.Main) {
        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    suspend fun longClickNode(node: AccessibilityNodeInfo): Boolean = withContext(Dispatchers.Main) {
        node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    suspend fun setTextNode(node: AccessibilityNodeInfo, text: String): Boolean = withContext(Dispatchers.Main) {
        val bundle = Bundle()
        bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
    }

    suspend fun scrollNode(node: AccessibilityNodeInfo, direction: Int): Boolean = withContext(Dispatchers.Main) {
        node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    // ----- Find nodes -----
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return findNode(rootInActiveWindow, { node ->
            node.text?.contains(text, ignoreCase = true) == true
        })
    }

    fun findNodeById(resourceId: String): AccessibilityNodeInfo? {
        return findNode(rootInActiveWindow, { node ->
            node.viewIdResourceName?.contains(resourceId) == true
        })
    }

    fun findNodeByDescription(desc: String): AccessibilityNodeInfo? {
        return findNode(rootInActiveWindow, { node ->
            node.contentDescription?.contains(desc, ignoreCase = true) == true
        })
    }

    private fun findNode(root: AccessibilityNodeInfo?, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (root == null) return null
        if (predicate(root)) return root
        for (i in 0 until root.childCount) {
            val result = findNode(root.getChild(i), predicate)
            if (result != null) return result
        }
        return null
    }

    // ----- Gestures -----
    suspend fun gestureSwipe(fromX: Float, fromY: Float, toX: Float, toY: Float, durationMs: Long) = withContext(Dispatchers.Main) {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()
        dispatchGesture(gesture, null, mainHandler)
    }

    // ----- Window info -----
    fun getCurrentWindowTitle(): String? {
        return rootInActiveWindow?.packageName?.toString()
    }

    fun getAllWindows(): List<AccessibilityWindowInfo> {
        return windows
    }
}
