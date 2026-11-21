# GitHub remote builds

AntiGem uses GitHub Actions for Android compilation and APK generation. The Android app only plans work, shows workflow status, and retrieves artifact links; it does not compile the APK on the phone.

## One-time setup

1. Push this project to a GitHub repository.
2. In GitHub, create an OAuth App and enable device authorization. Use a client ID only; do not put a client secret in the Android app.
3. Open **Coding Agent** in AntiGem and enter that client ID.
4. Complete GitHub's browser/device-code approval. The resulting token is encrypted with Android Keystore.
5. Enter the repository as `owner/name`, then tap **Refresh GitHub Actions**.

## Workflow

`.github/workflows/android-ci.yml` runs on a GitHub-hosted Ubuntu runner. It configures Java 17 and Android API 34, runs debug unit tests, assembles the debug APK, and uploads the APK/AAB outputs as a seven-day artifact.

The token needs only the smallest scopes appropriate to the chosen repository. Use a fine-grained token or an OAuth App restricted to the repository whenever possible.

## Safety boundary

The app may inspect repository and workflow state. Creating branches, committing, opening pull requests, dispatching builds, merging, changing release configuration, publishing releases, or changing repository permissions must remain explicit, user-approved actions. A successful build must be confirmed by GitHub Actions; no on-device status is treated as proof of an APK.
