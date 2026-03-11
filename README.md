# TrueSpend

> **TrueSpend** is a privacy-first, lightning-fast Android finance tracker built natively with Jetpack Compose and Room SQLite. 

TrueSpend parses local bank statement XLS files directly on-device, organizing transactions into relational Monthly Upload batches with real-time Net Flow calculation. It is 100% offline, highly performant, and completely secure.

## Features

- **Offline-First Privacy:** All parsing and database storage happens locally on your device using `Room`. No data is ever sent to the cloud.
- **Smart XLS Parsing:** Uses Apache POI to scan, clean, and parse raw bank `.xls` statements instantly.
- **Monthly Upload Batches:** Groups massive imports into cleanly separated 'Upload Cards' rather than a giant continuous list.
- **Reactive Net Flow:** Calculates your isolated Net Flow live through reactive Kotlin `StateFlow` Coroutines. Editing a transaction updates your Dashboard immediately with zero latency.
- **Custom Metadata:** Dynamically name your uploads via Compose `AlertDialogs` (e.g. "September 2026 Statement").
- **Manual Entries:** Easily add ad-hoc manual transactions directly into an isolated batch.

## Tech Stack

- **UI:** Jetpack Compose (Material 3)
- **Language:** Kotlin
- **Asynchronous Flow:** Coroutines & StateFlow
- **Local Database:** Room (SQLite)
- **Build System:** Gradle (KTS)
- **Document Parsing:** Apache POI

## Project Architecture

TrueSpend uses the latest Android architecture guidelines:
* **UI Layer:** Reactive state drawn strictly from `StateFlow` observation natively through Jetpack Compose elements (`LazyColumn`, `Scaffold`).
* **ViewModel Layer:** The `TransactionViewModel` safely runs database transactions strictly on the `Dispatchers.IO` background thread to maintain a 60FPS UI.
* **Data Layer:** `TransactionDao` hooks deeply into the `AppDatabase` triggering live emission updates on row manipulation (Insert, Update, Delete).
* **Parser:** A purely decoupled `SmartParser.kt` implementation that safely strips bad characters and maps raw string streams into type-safe Kotlin Data Classes.

## Environment & Setup Requirements

Before you can build or contribute to this repository, ensure your local environment meets the following tooling requirements:

1. **Java Development Kit (JDK)**  
   - **Version:** JDK 17  
   - **Note:** Required by Gradle 8.2 and the modern Kotlin compiler. Ensure your `JAVA_HOME` environment variable points to a valid JDK 17 installation.

2. **Android Studio**  
   - **Recommended Version:** Android Studio Iguana (2023.2.1) or newer.
   - **SDK Platforms:** Android API 34 (UpsideDownCake)
   - **Build Tools:** `34.0.0` or higher

3. **Gradle Build Tool**  
   - **Version:** 8.2  
   - *Note: You do not need to install Gradle manually. The included `gradlew` (Gradle Wrapper) will automatically download and utilize version `8.2`.*

4. **Kotlin**  
   - **Version:** `1.9.0` (Configured automatically via the `build.gradle.kts` plugin)
   - **KSP (Kotlin Symbol Processing):** `1.9.0-1.0.13` (Used by Room Database for fast compile-time schema generation)

## Installation & Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/Pratyush-Srivastava/TrueSpend.git
   cd TrueSpend
   ```
2. Open the project folder in **Android Studio**.
3. Allow Gradle to automatically sync the dependencies (Room, Compose, POI).
4. Connect an Android device via USB (ensure *USB Debugging* is enabled in Developer Options) or start a local Android Emulator.
5. Press the green **▶ Run** button in Android Studio to deploy it directly.

Alternatively, compile the `.apk` via the Gradle CLI:
```bash
./gradlew assembleDebug
```
The compiled App Bundle will be waiting securely at `app/build/outputs/apk/debug/app-debug.apk`.

## License
MIT License. Feel free to fork and adapt TrueSpend for your own custom banking formats!
