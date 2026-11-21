# AntiGem Assistant

A powerful, autonomous Android AI assistant capable of operating as an intelligent personal assistant.

## Status

This project is under active development. APK builds are intentionally performed by GitHub Actions or Android Studio on a supported x86_64 machine; the Android device is a control surface, not the primary build host.

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
- View-based UI (Material colors, light/dark themes)
- Local encryption (Android Keystore)
- Permission management dashboard
- GitHub Coding Agent dashboard with encrypted device-flow authentication and GitHub Actions status

## Architecture

Modular Kotlin architecture:
- `core/` - Database (Room), repository, application
- `ai/` - AI server connectivity, adapters (Ollama, OpenAI-compatible, llama.cpp, Nemotron), router
- `agent/` - Autonomous agent engine, device control (Intent-based)
- `skills/` - Skill discovery and management
- `comm/` - Communication (calls, SMS, contacts)
- `security/` - Android Keystore encryption
- `voice/` - Speech-to-text, text-to-speech, voice recognition service
- `research/` - Internet research and summarization
- `control/` - Accessibility service

## Build Instructions

### Option 1: Android Studio (Recommended)
1. Install Android Studio
2. Import the project at: `antigem-assistant/`
3. Run: Build → Generate Signed Bundle/APK → APK
4. Or: Build → Build Bundle → APK

### Option 2: Gradle CLI (desktop diagnostics only)
```bash
cd antigem-assistant
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: GitHub Actions (Already configured)
The GitHub Actions workflow is set up at:
```
https://github.com/sakshu89/Ai-agent/actions
```

## Post-Build Setup

1. Install APK: `adb install app-debug.apk`
2. Grant permissions in Settings → Permission Manager
3. Enable Accessibility Service (Settings → Accessibility → AntiGem Assistant)
4. Configure AI server (Settings → AI Servers)
5. Test connection

## Documentation

- [INSTALLATION.md](INSTALLATION.md) - Installation guide
- [PERMISSIONS.md](PERMISSIONS.md) - Permission setup guide
- [SERVERS.md](SERVERS.md) - AI server connection guide
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues
- [CAPABILITIES.md](CAPABILITIES.md) - Implemented vs. unsupported features
- [GITHUB_REMOTE_BUILDS.md](GITHUB_REMOTE_BUILDS.md) - GitHub device authorization and remote APK builds
- [DEVLOG.md](DEVLOG.md) - Development log

## GitHub remote builds

The repository workflow runs unit tests and builds a debug APK on GitHub-hosted runners. It uploads the APK as a short-lived workflow artifact. Configure a GitHub OAuth App with device authorization enabled, then enter its client ID in the app's Coding Agent screen. The app encrypts access tokens with Android Keystore and never logs them.

This checkout has not been built by GitHub Actions during this change, so no APK or build-success claim is made here.

## License

MIT
