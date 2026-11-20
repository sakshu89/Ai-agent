# AntiGem Assistant — Troubleshooting Guide

## Common Issues

### "No AI server available for routing"
**Cause:** No servers configured or none connected.
**Fix:** Add a server in Settings > AI Servers and test the connection.

### "Connection failed"
**Cause:** Server address, port, or auth is incorrect.
**Fix:** Verify server is running and reachable. Check address, port, and credentials.

### "Permission denied" for device control
**Cause:** Accessibility or other permission not granted.
**Fix:** Go to Settings > Permission Manager and grant the required permission.

### Voice input not working
**Cause:** Microphone permission not granted or SpeechRecognizer unavailable.
**Fix:** Grant RECORD_AUDIO permission. Check Settings > Voice Settings.

### App crashes on launch
**Cause:** Incompatible Android version or missing dependencies.
**Fix:** Ensure Android 7.0+. Try clearing app data in Settings > Apps.

### Tasks not executing
**Cause:** Accessibility service not enabled or task configuration invalid.
**Fix:** Enable Accessibility service for AntiGem. Check task settings.

### Battery drain
**Cause:** Background tasks or frequent screen capture.
**Fix:** Adjust screenshot frequency in Settings > Automation. Disable unused background features.

## Build Issues (Developer)

### APK build fails on aarch64/Termux
**Cause:** Google's AAPT2 is x86_64-only; this device is aarch64.
**Fix:** Build on an x86_64 machine with Android Studio:
```bash
./gradlew assembleDebug
```

### "Cannot resolve com.android.tools.build:gradle"
**Cause:** No Maven repositories configured.
**Fix:** Ensure `google()` and `mavenCentral()` are in build.gradle.kts repositories.

### Kotlin/AGP version mismatch
**Cause:** Kotlin 2.4.21 requires AGP 8.5.2+.
**Fix:** Use AGP 8.5.2 or newer with Kotlin 2.4.21.

## Debug Logs
Enable Timber logging in Settings > Diagnostics. Logs are stored locally and can be exported.

## Support
For bugs or feature requests, check the project repository.
