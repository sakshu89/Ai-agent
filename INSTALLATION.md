# AntiGem Assistant — Installation Guide

## System Requirements
- Android 7.0 (API 24) or higher
- ARM64, ARMv7, or x86_64 architecture
- Minimum 2GB RAM (4GB recommended)
- 500MB free storage

## Installation

### From APK (Debug Build)
1. Download `app-debug.apk` from the build output
2. On your Android device, enable "Install from Unknown Sources" in Settings
3. Open the APK file to install
4. Launch AntiGem Assistant from your app drawer

### From Source (x86_64 Build Machine)
```bash
# Clone or copy the project
cd antigem-assistant

# Set Android SDK path
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### From Source (This aarch64/Termux Environment)
**NOTE:** APK building is blocked on this architecture because Google distributes x86_64-only AAPT2 binaries. Use an x86_64 machine with Android Studio to build the APK.

## First Launch
1. Grant requested permissions (microphone, contacts, phone, SMS, accessibility, etc.)
2. Configure at least one AI server in Settings > AI Servers
3. Test the connection to verify your server is reachable
4. Start chatting!

## Uninstall
Settings > Apps > AntiGem Assistant > Uninstall
