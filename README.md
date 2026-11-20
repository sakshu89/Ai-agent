# AntiGem Assistant

A powerful, autonomous Android AI assistant capable of operating as an intelligent personal assistant.

## Features

- Natural conversation with streaming AI responses
- Local-first memory (Room/SQLite) - survives model/server changes
- Multiple AI server support (Ollama, llama.cpp, Nemotron, OpenAI-compatible)
- Autonomous agent engine (observe-plan-act-verify cycle)
- Android device control (Intents, Accessibility Service)
- Skill discovery and management
- Phone calls and SMS
- Voice input and output (STT/TTS)
- Internet research and summarization
- Jetpack Compose UI with light/dark themes
- Local encryption (Android Keystore)
- Permission management dashboard

## Architecture

Modular Kotlin + Jetpack Compose architecture:
- `core/` - Database, repository, application
- `ai/` - AI server connectivity, adapters, router
- `agent/` - Autonomous agent engine, device control
- `skills/` - Skill discovery and management
- `comm/` - Communication (calls, SMS, contacts)
- `security/` - Keystore encryption
- `voice/` - Speech-to-text, text-to-speech
- `research/` - Internet research
- `control/` - Accessibility service
- `ui/` - Jetpack Compose screens

## Build

Requires Android Studio on x86_64 (Windows/Mac/Linux). APK cannot be built on aarch64 Android/Termux due to Google's x86_64-only AAPT2 binaries.

```bash
./gradlew assembleDebug
```

## License

MIT
