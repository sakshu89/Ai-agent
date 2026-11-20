package ai.antigem.assistant.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// ============================================================================
// AntiGem Assistant — Main Activity (View-based, no Compose)
// ============================================================================

class MainActivity : AppCompatActivity() {

    private lateinit var rootContainer: FrameLayout
    private lateinit var chatInput: EditText
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private lateinit var chatMessagesContainer: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootContainer = findViewById(R.id.root_container)
        chatInput = findViewById(R.id.chat_input)
        sendButton = findViewById(R.id.send_button)
        statusText = findViewById(R.id.status_text)
        chatMessagesContainer = findViewById(R.id.chat_messages_container)

        // Show initial welcome message
        addChatMessage("system", "Hello! I'm your AntiGem assistant. How can I help you today?")

        sendButton.setOnClickListener {
            val text = chatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                // Add user message
                addChatMessage("user", text)
                chatInput.setText("")
                setStatus("Processing your request...")

                // Send to AI - placeholder for actual AI integration
                // In a full build, this would call AiService
                Thread {
                    Thread.sleep(500)
                    runOnUiThread {
                        addChatMessage("assistant", "I'm here to help! AntiGem Assistant is your autonomous Android AI assistant.")
                        setStatus("Ready")
                    }
                }.start()
            }
        }
    }

    private fun addChatMessage(sender: String, message: String) {
        val messageView = LayoutInflater.from(this).inflate(R.layout.chat_message, null, false)
        val senderText = messageView.findViewById<TextView>(R.id.sender_text)
        val messageText = messageView.findViewById<TextView>(R.id.message_text)

        senderText.text = sender.replaceFirstChar { it.uppercase() }
        messageText.text = message

        if (sender == "assistant") {
            senderText.setBackgroundColor(resources.getColor(R.color.color_secondary_container))
            messageView.setBackgroundColor(resources.getColor(R.color.color_surface_variant))
        } else {
            messageView.setBackgroundColor(resources.getColor(R.color.color_primary_container))
        }

        chatMessagesContainer.addView(messageView)
        chatMessagesContainer.postDelayed({
            chatMessagesContainer.fullScroll(View.FOCUS_DOWN)
        }, 100)
    }

    private fun setStatus(text: String) {
        statusText.text = text
    }

    // ===== Permission request handler =====
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setStatus("Permissions updated")
    }
}
