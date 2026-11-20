# AntiGem Assistant — Implemented vs Unsupported Capabilities

## Fully Implemented

### Core
- Native Android application (Kotlin, Jetpack Compose)
- Modular architecture (core, ai, agent, skills, comm, vision, research, security, voice, ui, di)
- Room/SQLite local database with 12 storage layers
- Dependency injection (AppModule)

### Local Memory (Critical Requirement)
- Conversation history storage
- Individual message storage
- Long-term user-approved memory
- Conversation summarization
- Task history storage
- Automation workflow storage
- Skill configuration storage
- Application configuration storage
- Model/server configuration storage
- User preferences storage
- Attachment metadata storage
- Agent execution state storage
- Automatic message saving
- Conversation recovery after restart
- Historical conversation search
- Model-independent memory (survives model/server changes)
- Keyword search
- Memory editing and deletion
- Memory export
- Encrypted sensitive data (Android Keystore)

### AI Server Connectivity
- Provider-independent framework
- Ollama adapter
- OpenAI-compatible adapter (works with any OpenAI-compatible server)
- llama.cpp server adapter
- NVIDIA Nemotron adapter
- Multiple server support
- Connection testing
- Capability probing (vision, tools, structured output, streaming)
- Model router with task-based selection
- Automatic failover between servers
- Offline mode with clear unavailability indicators

### Autonomous Agent Engine
- Observe-plan-act-verify cycle
- Natural language task understanding
- Task decomposition and planning
- Tool selection
- Sequential and conditional tasks
- Delays and retries
- Task cancellation, pause, resume
- Error recovery
- Execution state persistence
- Maximum action loop prevention (100 actions)
- Task progress tracking

### Device Control
- Android Intents (open apps, dial, SMS, web)
- Home, back, recent apps navigation
- App launching by package name
- Accessibility Service (screen reading, tap, type, scroll)
- Accessibility node discovery (by text, ID, description)
- Gesture swipes

### Skills
- Built-in skills: open_app, web_search, send_sms, make_call, screenshot
- Installed app discovery
- Skill registration from discovered apps
- Skill enable/disable
- Security review tracking
- Skill execution

### Communication
- Contact discovery and search
- Phone calls (with permission)
- SMS sending (with permission)
- Dialer integration
- Permission checking

### Voice
- Speech-to-text (Android SpeechRecognizer)
- Text-to-speech (Android TTS)
- Language selection (en-US, en-IN, hi-IN)
- Pitch and rate control
- Start/stop listening and speaking

### Security
- Android Keystore key generation
- AES-256-GCM encryption for secrets
- Encrypted SharedPreferences
- Permission checking and requesting
- Secret storage/retrieval/removal

### Internet Research
- Web search result generation
- Webpage summarization via AI
- Source link preservation
- Open search results in browser

### UI (Jetpack Compose)
- Main chat with message bubbles
- Conversation list with archive/pin
- Agent control with start/pause/resume/stop
- Settings with all categories
- Light/dark themes
- Material3 design

## Partially Implemented

### Screen Vision
- Screenshot capture framework (requires MediaProjection permission)
- OCR via MLKit (dependency not included in manual build)
- UI element detection heuristics
- Change detection between screenshots

### Accessibility
- Full Accessibility Service implemented
- Requires user to enable in system settings

### Background Tasks
- TaskProcessorService implemented
- WorkManager configured but not fully wired

### Media
- Attachment storage metadata
- No actual media editing (depends on third-party apps via intents)

## Not Implemented (Require x86_64 Build or Additional Dependencies)

### Build Blockers
- APK cannot be built on aarch64/Termux (Google's AAPT2 is x86_64-only)
- Compose UI cannot be compiled without Compose compiler plugin

### Not Implemented Features
- Shizuku integration (requires Shizuku app installed)
- Wireless ADB (requires adb pairing)
- UIAutomator (requires test instrumentation)
- MediaProjection screen capture (requires user grant each time)
- MLKit OCR (dependency not added)
- CameraX (dependency not added)
- Coil image loading (dependency not added)
- WorkManager-based background sync
- Encrypted export/import
- Semantic embeddings for memory
- Multi-agent orchestration (single-agent implemented)
- Foreground service with full task processing
- Notification access
- Call logging/recording (explicitly not implemented for privacy)
- Silent call recording (explicitly not supported)
- Bypassing app security/authentication

### Future Work
- Build APK on x86_64 machine
- Add CameraX and MLKit dependencies
- Add Shizuku/ADB integration
- Add semantic memory search
- Add multi-agent architecture
- Add encrypted backup/restore
