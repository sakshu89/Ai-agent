# ProGuard rules for AntiGem Assistant

# Keep Application class
-keep class ai.antigem.assistant.core.App { *; }

# Keep Room entities and DAOs
-keep class ai.antigem.assistant.core.* { *; }

# Keep model classes
-keep class ai.antigem.assistant.ai.* { *; }
-keep class ai.antigem.assistant.agent.* { *; }
-keep class ai.antigem.assistant.skills.* { *; }
-keep class ai.antigem.assistant.comm.* { *; }
-keep class ai.antigem.assistant.security.* { *; }
-keep class ai.antigem.assistant.voice.* { *; }
-keep class ai.antigem.assistant.research.* { *; }
-keep class ai.antigem.assistant.control.* { *; }

# Keep Kotlin metadata
-keep class kotlin.* { *; }

# Keep AccessibilityService
-keep class ai.antigem.assistant.control.AssistantAccessibilityService { *; }

# Keep VoiceRecognitionService
-keep class ai.antigem.assistant.voice.VoiceRecognitionService { *; }

# Suppress warnings
-dontwarn androidx.**
-dontwarn io.ktor.**
-dontwarn kotlinx.**
