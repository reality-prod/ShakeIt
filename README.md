# ShakeIt!

ShakeIt! is an offline-first, AGPL-3.0-or-later Android motion-shortcut app for configurable “light / medium / hard” shake recipes.

## Included in v1

- Persistent shortcut editor with action, intensity, and repetition count.
- Calibration screen that records a real accelerometer peak and derives local thresholds.
- Foreground sensor service with a visible notification.
- Flashlight, sound-mode, and Do Not Disturb actions with Android-required access handoffs.
- Wallet shortcut that posts a secure notification handoff and opens a biometric-gated local default-card view.
- Separate data, sensor, action, service, and UI packages; no placeholder TODO action path.

## Research decisions

- Android 9+ throttles continuous sensors for ordinary background apps, so detection uses a user-enabled foreground service with an ongoing notification.
- Android 14+ requires every foreground service to declare a type and permission. Android has no generic “sensor” type; this app uses `specialUse` with an explicit manifest subtype and should be reviewed against the final distribution target before release.
- Wallet actions should call AndroidX `BiometricPrompt` and never store biometric data or pretend to unlock a payment credential. A wallet shortcut should open a local wallet view after authentication; payment execution is out of scope for the core app.
- No Google Play Services, cloud SDK, analytics, ads, or sensor gesture library are used. The detector is a small local heuristic so it can be audited and replaced.
- The UI uses Jetpack Compose and AndroidX, both Apache-2.0 licensed. Keep third-party notices in `THIRD_PARTY_NOTICES.md` as dependencies are added.

## Build

```bash
./gradlew :app:assembleDebug
```

Build artifacts are written to `app/build/outputs/apk/debug/app-debug.apk` and `app/build/outputs/apk/release/app-release-unsigned.apk`.

### Source-built FOSS toolchain

With the FOSS `android-sdk-tools` checkout available, run:

```bash
FOSS_SDK_TOOLS_DIR="$HOME/oss-android-foss/src/android-sdk-tools" ./build.sh
```

The script builds its Docker image, compiles `aapt2` from the mounted SDK-tools
source, runs lint/tests/`assembleDebug`, and writes
`outputs/ShakeIt-debug-foss.apk` plus its SHA-256 file. The installed Android
platform/build-tools directory is mounted read-only; the app itself adds no
Google Play Services or proprietary runtime dependency.
