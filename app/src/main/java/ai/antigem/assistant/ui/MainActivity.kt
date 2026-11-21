package ai.antigem.assistant.ui

import android.os.Bundle
import android.content.Intent
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ai.antigem.assistant.core.ConversationEntity
import ai.antigem.assistant.core.MessageEntity
import ai.antigem.assistant.core.appContext
import ai.antigem.assistant.github.GitHubSessionStore
import kotlinx.coroutines.launch
import java.util.UUID

// ============================================================================
// AntiGem Assistant - Main Activity (View-based)
// ============================================================================

class MainActivity : AppCompatActivity() {

    private lateinit var chatInput: EditText
    private lateinit var chatMessagesContainer: ScrollView
    private lateinit var messages: android.widget.LinearLayout
    private lateinit var conversationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ai.antigem.assistant.R.layout.activity_main)

        chatInput = findViewById(ai.antigem.assistant.R.id.chat_input)
        chatMessagesContainer = findViewById(ai.antigem.assistant.R.id.chat_messages_container)
        messages = findViewById(ai.antigem.assistant.R.id.chat_messages_linear)
        conversationId = savedInstanceState?.getString(STATE_CONVERSATION_ID)
            ?: UUID.randomUUID().toString()

        lifecycleScope.launch {
            val repository = appContext(this@MainActivity).appModule.repository
            if (repository.getConversationById(conversationId) == null) {
                repository.insertConversation(ConversationEntity(id = conversationId, title = "New conversation"))
                val welcome = "Hello! I keep this conversation on your device. Configure an AI server to start a model-backed chat."
                repository.insertMessage(MessageEntity(UUID.randomUUID().toString(), conversationId, "assistant", welcome))
            }
            repository.getMessagesByConversation(conversationId).forEach { addChatMessage(it.role, it.content) }
        }

        val githubStatus = findViewById<TextView>(ai.antigem.assistant.R.id.github_status)
        val session = GitHubSessionStore(this)
        githubStatus.text = if (session.hasToken()) "GitHub: connected" else "GitHub: connect to enable remote builds"
        findViewById<android.view.View>(ai.antigem.assistant.R.id.coding_agent_card).setOnClickListener {
            startActivity(Intent(this, CodingAgentActivity::class.java))
        }

        val sendButton = findViewById<android.widget.Button>(ai.antigem.assistant.R.id.send_button)
        sendButton.setOnClickListener {
            val text = chatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                addChatMessage("user", text)
                chatInput.setText("")
                lifecycleScope.launch {
                    appContext(this@MainActivity).appModule.repository.insertMessage(
                        MessageEntity(UUID.randomUUID().toString(), conversationId, "user", text)
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CONVERSATION_ID, conversationId)
        super.onSaveInstanceState(outState)
    }

    private fun addChatMessage(sender: String, message: String) {
        val messageView = android.view.LayoutInflater.from(this)
            .inflate(ai.antigem.assistant.R.layout.chat_message, null, false)

        val senderText = messageView.findViewById<TextView>(ai.antigem.assistant.R.id.sender_text)
        val messageText = messageView.findViewById<TextView>(ai.antigem.assistant.R.id.message_text)
        val container = messageView.findViewById<android.widget.LinearLayout>(ai.antigem.assistant.R.id.message_container)

        senderText.text = sender.replaceFirstChar { it.uppercase() }
        messageText.text = message

        if (sender == "assistant") {
            container.setBackgroundColor(resources.getColor(ai.antigem.assistant.R.color.color_surface_variant))
        } else {
            container.setBackgroundColor(resources.getColor(ai.antigem.assistant.R.color.color_primary_container))
        }

        messages.addView(messageView)
        chatMessagesContainer.postDelayed({
            chatMessagesContainer.fullScroll(android.view.View.FOCUS_DOWN)
        }, 100)
    }

    private companion object {
        const val STATE_CONVERSATION_ID = "conversation_id"
    }
}
