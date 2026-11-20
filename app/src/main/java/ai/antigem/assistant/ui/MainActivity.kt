package ai.antigem.assistant.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// ============================================================================
// AntiGem Assistant - Main Activity (View-based)
// ============================================================================

class MainActivity : AppCompatActivity() {

    private lateinit var chatInput: EditText
    private lateinit var chatMessagesContainer: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ai.antigem.assistant.R.layout.activity_main)

        chatInput = findViewById(ai.antigem.assistant.R.id.chat_input)
        chatMessagesContainer = findViewById(ai.antigem.assistant.R.id.chat_messages_container)

        addChatMessage("assistant", "Hello! I'm your AntiGem assistant. How can I help you today?")

        val sendButton = findViewById<android.widget.Button>(ai.antigem.assistant.R.id.send_button)
        sendButton.setOnClickListener {
            val text = chatInput.text.toString().trim()
            if (text.isNotEmpty()) {
                addChatMessage("user", text)
                chatInput.setText("")
            }
        }
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

        chatMessagesContainer.addView(messageView)
        chatMessagesContainer.postDelayed({
            chatMessagesContainer.fullScroll(android.view.View.FOCUS_DOWN)
        }, 100)
    }
}
