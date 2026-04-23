# NFC Reader Sample App

A sample Android application demonstrating how to integrate Daon's NFC SDK to read NFC-chipped travel documents such as biometric passports (ICAO 9303 compliant). The app guides a user through entering their document credentials, then reads the chip over NFC and displays the extracted passport data — including the face photograph — alongside any validation issues.

---

## Table of Contents

- [Purpose](#purpose)
- [Requirements](#requirements)
- [NFC SDK Dependency Setup](#nfc-sdk-dependency-setup)
- [Dependencies](#dependencies)
- [Building the App](#building-the-app)
- [Running the Tests](#running-the-tests)
- [Signing a Release Build](#signing-a-release-build)

---

## Purpose

This project serves as a reference implementation for integrating the `com.daon.sdk:nfc` library into an Android app. It demonstrates:

- Initialising `NFCReader` from the Daon NFC SDK with a license file
- Collecting passport credentials (document number, date of birth, date of expiry) via a Compose UI
- Handling the full NFC read lifecycle: foreground dispatch, progress callbacks, success, and error states
- Displaying structured passport data (personal details, MRZ, face image) returned by the SDK as `TagData`
- Mapping SDK error codes (`TagError`) to user-facing messages
- A clean MVVM pattern that keeps all SDK interaction inside a `ViewModel`, making the UI and business logic independently testable

---
## Requirements

| Component | Version |
|---|---|
| Kotlin | 2.2.20 |
| Minimum Android SDK | 23 (Android 6.0) |

A physical Android device with NFC hardware is required to perform actual passport reads. The UI and ViewModel are fully exercisable on an emulator, but NFC tag detection will not fire without real hardware.

---

## NFC SDK Dependency Setup

The Daon NFC SDK (`com.daon.sdk:nfc`) is published to a private GitHub Packages Maven repository. You must provide GitHub credentials before Gradle can resolve the dependency.

### 1. Create a GitHub fine-grained personal access token

Follow the instructions at [https://github.com/daoninc/sdk-packages](https://github.com/daoninc/sdk-packages) to create a token with the required access.

### 2. Add credentials to `local.properties`

Open (or create) `local.properties` in the project root and add:

```properties
daon.repository.username=your-github-username
daon.repository.token=your-fine-grained-token
```

`settings.gradle` reads these values at sync time and passes them to the `maven` credentials block. `local.properties` is excluded from source control by `.gitignore`.

### 3. SDK license file

The SDK validates a license at runtime. The license file is read from the app's `assets` directory. A pre-bundled `license.txt` is located at:

```
app/src/main/assets/license.txt
```

If you need to use your own license, replace this file. The file path is passed to `NFCReader` during initialisation:

```kotlin
NFCReader(activity, "license.txt", false)
```

The NFC SDK requires a license that is bound to an application identifier. This license may in turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to request a license.

---

## Dependencies

All dependency versions are centralised in `gradle/libs.versions.toml`. The `app/build.gradle` references them via Gradle version catalog accessors (`libs.*`).

### Daon NFC SDK

| Artifact | Version                                  | Purpose |
|---|------------------------------------------|---|
| `com.daon.sdk:nfc` | `+` (using the latest available version) | Core NFC passport reading library |

---

## Building the App

### Debug build (APK)

```bash
./gradlew assembleDebug
```

The output APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release build (APK)

A signing configuration is required for release builds (see [Signing a Release Build](#signing-a-release-build)).

```bash
./gradlew assembleRelease
```

### Install directly to a connected device

```bash
# Debug
./gradlew installDebug

# Release
./gradlew installRelease
```

### Clean the build

```bash
./gradlew clean
```

---

## Running the Tests

The project contains JVM unit tests only (no instrumented tests). Run them with:

```bash
./gradlew test
```

Or for the app module specifically:

```bash
./gradlew :app:testDebugUnitTest
```

Test results are written to:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

The three test classes cover:

| Class | Coverage |
|---|---|
| `NfcViewModelTest` | ViewModel state machine: `startScan`, `cancelScan`, `retryScan`, `showResults`, `resetResult`, lifecycle callbacks |
| `NfcUiStateTest` | `NfcUiState` data class defaults, copy semantics, equality, and progress field boundaries |
| `UtilityFunctionTest` | `validate()`, `formatDisplayDate()`, `formatShortDate()`, and the `meaningful()` string extension |

---

## Signing a Release Build

Release signing credentials are loaded from a properties file whose path is configured in `gradle.properties`:

```properties
localSigning.properties=../signing.properties
```

Create a `signing.properties` file at the path referenced above (by default, one directory above the project root) with the following keys:

```properties
keystore=/absolute/path/to/your/keystore.jks
keystore.password=your_keystore_password
key.alias=your_key_alias
key.password=your_key_password
```

This file is excluded from source control. If it does not exist, the release signing config will use a placeholder path and the release build will fail to sign — which is the intended behaviour to prevent accidental unsigned releases.
