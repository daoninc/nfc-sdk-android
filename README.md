# NFC SDK

The NFC SDK can be used to read NFC-chipped travel documents such as biometric passports (ICAO 9303 compliant) on Android mobile devices. The SDK handles the full NFC read lifecycle and returns structured passport data including personal details, MRZ, and face photograph.

## License
The NFC SDK requires a license that is bound to an application identifier. This license may in turn embed licenses that are required for specific authenticators. Contact Daon Support or Sales to request a license.

## Samples

The demo sample includes the following:

- **NFC Reader**: A reference sample application demonstrating passport reading via NFC, including credential entry, chip reading, and display of extracted passport data.

## SDK repository
In your project-level build.gradle file, make sure to include the Daon Maven repository in your buildscript or allprojects sections.

[Daon Maven repository](https://github.com/daoninc/sdk-packages/blob/main/README.md)

## API

Add the following dependencies to the build.gradle file:

```gradle
implementation 'com.daon.sdk:nfc:+'
```

### Initialize

**Kotlin**
```kotlin
val nfcReader = NFCReader(activity, "license.txt", false)
```

### Read a passport

**Kotlin**
```kotlin
nfcReader.read(
    documentNumber = documentNumber,
    dateOfBirth = dateOfBirth,
    dateOfExpiry = dateOfExpiry,
    onProgress = { progress ->
        // Progress update
    },
    onComplete = { tagData ->
        // Passport data read successfully
    },
    onError = { tagError ->
        // Handle error
    }
)
```

See the [NFC SDK Documentation](https://developer.identityx-cloud.com/client/nfc/android/) for more information.
