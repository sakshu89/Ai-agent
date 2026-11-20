# AntiGem Assistant — Permission Setup Guide

## Permissions Overview

AntiGem requests the following permissions. Each is explained below.

### Core Permissions (Required for basic operation)

| Permission | Purpose | Required |
|------------|---------|----------|
| INTERNET | Connect to AI servers | Yes |
| ACCESS_NETWORK_STATE | Check network connectivity | Yes |
| ACCESS_WIFI_STATE | Check Wi-Fi connectivity | Yes |

### Device Control Permissions

| Permission | Purpose | Required |
|------------|---------|----------|
| SYSTEM_ALERT_WINDOW | Display overlays on screen | No (optional) |
| WRITE_SETTINGS | Modify system settings | No (optional) |
| REORDER_TASKS | Reorder app tasks | No (optional) |
| FOREGROUND_SERVICE | Run background tasks | Yes |
| FOREGROUND_SERVICE_SPECIAL_USE | Special background use | Yes |

### Accessibility (For screen reading and control)

| Permission | Purpose | Required |
|------------|---------|----------|
| BIND_ACCESSIBILITY_SERVICE | Read screen content, tap, type | No (optional but recommended) |

**How to enable Accessibility:**
1. Settings > Accessibility > AntiGem Assistant > Turn ON
2. This allows the assistant to read your screen and perform actions

### Communication Permissions

| Permission | Purpose | Required |
|------------|---------|----------|
| CALL_PHONE | Place phone calls | No (optional) |
| SEND_SMS | Send SMS messages | No (optional) |
| RECEIVE_SMS | Receive SMS messages | No (optional) |
| READ_SMS | Read SMS messages | No (optional) |
| READ_CALL_LOG | Read call history | No (optional) |
| READ_PHONE_STATE | Read phone state | No (optional) |
| READ_CONTACTS | Read contacts | No (optional) |
| WRITE_CONTACTS | Write contacts | No (optional) |
| GET_ACCOUNTS | Access accounts | No (optional) |

### Media Permissions

| Permission | Purpose | Required |
|------------|---------|----------|
| CAMERA | Take photos, scan QR codes | No (optional) |
| READ_MEDIA_IMAGES | Access photos | No (optional) |
| READ_MEDIA_VIDEO | Access videos | No (optional) |
| READ_MEDIA_AUDIO | Access audio | No (optional) |
| PROJECT_MEDIA | Capture screen | No (optional) |

### Microphone

| Permission | Purpose | Required |
|------------|---------|----------|
| RECORD_AUDIO | Voice input | No (optional) |

## Permission Dashboard
Access via Settings > Permission Manager. Each permission shows:
- Purpose and explanation
- Current status (granted/denied)
- Option to request/deny
- Revocation detection

## Important Notes
- AntiGem never requires root
- All permissions are optional except core network permissions
- You can revoke any permission at any time
- AntiGem will gracefully degrade when permissions are denied
- AntiGem never silently records calls or accesses data without permission
