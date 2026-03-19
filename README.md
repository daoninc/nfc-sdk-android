# nfc-sdk-android

A sample Android application demonstrating how to integrate Daon's NFC SDK to read NFC-chipped travel documents such as biometric passports (ICAO 9303 compliant). The app guides a user through entering their document credentials, then reads the chip over NFC and displays the extracted passport data — including the face photograph — alongside any validation issues.

---

## Table of Contents

- [Purpose](#purpose)
- [Repository Structure](#repository-structure)
- [Architecture](#architecture)
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

## Repository Structure

```
nfc-sdk-android/
├── app/
│   ├── build.gradle               # App-level Gradle config, dependencies, signing
│   ├── proguard-rules.pro         # ProGuard/R8 rules for release builds
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   └── license.txt    # Daon SDK license file (loaded at runtime)
│       │   ├── java/com/daon/sdk/nfc/sample/
│       │   │   ├── MainActivity.kt          # Single-activity host; NFC lifecycle bridge
│       │   │   ├── NfcViewModel.kt          # MVVM ViewModel; owns NFCReader; all SDK state
│       │   │   └── ui/
│       │   │       ├── ScanScreen.kt        # Input form (doc number, DOB, DOE)
│       │   │       ├── ResultScreen.kt      # Passport data display after a successful read
│       │   │       ├── NfcScanDialog.kt     # Animated bottom-sheet scan progress dialog
│       │   │       └── theme/
│       │   │           ├── Theme.kt         # Material 3 light/dark colour schemes
│       │   │           └── Dimensions.kt    # Centralised dimension token system (AppDimens)
│       │   └── res/                         # Drawables, launcher icons, strings
│       └── test/
│           └── java/com/daon/sdk/nfc/sample/
│               ├── NfcViewModelTest.kt      # ViewModel state-machine unit tests
│               ├── NfcUiStateTest.kt        # Data-class default/copy/equality tests
│               └── UtilityFunctionTest.kt   # Tests for validation and date-format helpers
├── build.gradle                   # Top-level build file; classpath dependencies
├── settings.gradle                # Module list; Maven repository configuration
├── gradle.properties              # JVM args, AndroidX flags, signing properties path
└── gradle/wrapper/
    └── gradle-wrapper.properties  # Gradle 8.14.3 distribution
```

---

## Architecture

The app follows a strict **MVVM + Unidirectional Data Flow** pattern built on Jetpack Compose:

```
MainActivity (FragmentActivity)
   │  Lifecycle callbacks: onResume / onPause / onNewIntent → ViewModel
   │  Compose host: ScanScreen ↔ ResultScreen  (state-driven navigation)
   ▼
NfcViewModel (AndroidViewModel)
   │  Owns NFCReader (Daon SDK)
   │  Exposes three StateFlows consumed by the UI:
   │    - dialogVisible: StateFlow<Boolean>
   │    - uiState:       StateFlow<NfcUiState>   (state, message, progress, result)
   │    - showResult:    StateFlow<Boolean>       (drives top-level navigation)
   │
   │  TagReaderCallback translates SDK events into StateFlow emissions:
   │    tagReaderInformation  →  IN_PROGRESS + progress value
   │    tagReaderDidFinish    →  SUCCESS + TagData result
   │    tagReaderFailed       →  FAIL + mapped error message
   ▼
Compose UI
   ScanScreen          – document credential form + date pickers
   NfcScanDialog       – animated bottom sheet (ripple / check / error)
   ResultScreen        – passport fields, face photo, issue list
   NfcSdkAndroidTheme  – Material 3 with Daon brand colours
```

`NfcViewModel` initialises `NFCReader` lazily (via `initReader`) and guards every SDK call with null-safety (`?.`), meaning the ViewModel and all its unit tests work correctly without real NFC hardware.

---

## Requirements

| Component | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or later |
| Android Gradle Plugin | 8.12.3 |
| Gradle | 8.14.3 |
| Kotlin | 2.2.20 |
| Minimum Android SDK | 21 (Android 5.0) |
| Target / Compile SDK | 36 |
| JVM target | 1.8 |

A physical Android device with NFC hardware is required to perform actual passport reads. The UI and ViewModel are fully exercisable on an emulator, but NFC tag detection will not fire without real hardware.

---

## NFC SDK Dependency Setup

The Daon NFC SDK (`com.daon.sdk:nfc`) is published to a private GitHub Packages Maven repository. You must provide GitHub credentials before Gradle can resolve the dependency.

### 1. Create a GitHub fine-grained personal access token

1. Log in to GitHub and go to **Settings → Developer settings → Personal access tokens → Fine-grained tokens**.
2. Click **Generate new token**.
3. Set the **Resource owner** to `daoninc`.
4. Under **Repository access**, select **Only select repositories** and choose `sdk-packages`.
5. Under **Permissions → Repository permissions**, grant **Contents: Read-only**.
6. Generate and copy the token — it will only be shown once.

### 2. Add credentials to `settings.gradle`

Open `settings.gradle` and update the `maven` block inside `dependencyResolutionManagement`:

```groovy
maven {
    url = uri("https://maven.pkg.github.com/daoninc/sdk-packages/")
    credentials {
        username = "your-github-username"
        password = "your-fine-grained-token"
    }
}
```

### 3. SDK license file

The SDK validates a license at runtime. The license file is read from the app's `assets` directory. A pre-bundled `license.txt` is located at:

```
app/src/main/assets/license.txt
```

If you need to use your own license, replace this file. The file path is passed to `NFCReader` during initialisation:

```kotlin
NFCReader(activity, "license.txt", false)
```

---

## Dependencies

### Daon NFC SDK

| Artifact | Version  | Purpose |
|---|----------|---|
| `com.daon.sdk:nfc` | `1.3.57` | Core NFC passport reading library |

### NFC SDK transitive dependencies

These libraries are required by the Daon NFC SDK and must be declared explicitly in the app's `build.gradle`. BouncyCastle `jdk15on` variants are excluded to avoid duplication with the `jdk18on` variant used here.

| Artifact | Version | Purpose |
|---|---|---|
| `org.jmrtd:jmrtd` | `0.8.2` | Java Machine Readable Travel Documents — ICAO LDS parsing |
| `net.sf.scuba:scuba-sc-android` | `0.0.26` | Smart card USB/NFC low-level communication |
| `org.bouncycastle:bcprov-jdk18on` | `1.82` | Cryptography provider for BAC/PACE and chip authentication |

### AndroidX

| Artifact | Version | Purpose |
|---|---|---|
| `androidx.core:core-ktx` | `1.16.0` | Kotlin extensions for core Android APIs |
| `androidx.activity:activity-compose` | `1.10.1` | Compose integration for `ComponentActivity` |
| `androidx.fragment:fragment-ktx` | `1.8.8` | Fragment Kotlin extensions (required by `FragmentActivity`) |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.9.1` | `viewModel()` and `collectAsStateWithLifecycle()` helpers |
| `androidx.lifecycle:lifecycle-runtime-ktx` | `2.9.1` | `lifecycleScope` and `repeatOnLifecycle` coroutine extensions |

### Jetpack Compose

The Compose dependency versions are managed via the BOM:

| Artifact | Version / BOM | Purpose |
|---|---|---|
| `androidx.compose:compose-bom` | `2024.12.01` | BOM that aligns all Compose library versions |
| `androidx.compose.ui:ui` | (BOM) | Core Compose UI toolkit |
| `androidx.compose.ui:ui-tooling-preview` | (BOM) | `@Preview` composable support |
| `androidx.compose.material3:material3` | (BOM) | Material Design 3 components and theming |
| `androidx.compose.material:material-icons-extended` | (BOM) | Extended Material icon set (includes `Nfc`, `Badge`, etc.) |
| `androidx.compose.ui:ui-tooling` | (BOM, `debugImplementation`) | Layout inspector and preview tools |

### Testing

| Artifact | Version | Purpose |
|---|---|---|
| `junit:junit` | `4.13.2` | JUnit 4 test runner |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `1.8.1` | `StandardTestDispatcher` / `runTest` for coroutine testing |
| `io.mockk:mockk` | `1.13.12` | Kotlin-idiomatic mocking library |

### Build plugins

| Plugin | Version | Purpose |
|---|---|---|
| `com.android.tools.build:gradle` | `8.12.3` | Android Gradle Plugin |
| `org.jetbrains.kotlin:kotlin-gradle-plugin` | `2.2.20` | Kotlin compilation |
| `org.jetbrains.kotlin:compose-compiler-gradle-plugin` | `2.2.20` | Compose compiler integration for Kotlin 2.x |

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
