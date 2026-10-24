# // KMP Dictionary

A high-performance Kotlin Multiplatform (KMP) desktop and mobile application built with Compose Multiplatform. **// KMP Dictionary** offers a dual-source lookup engine that retrieves structured formal definitions from Merriam-Webster alongside real-time internet slang from Urban Dictionary.

---

## Features

* **Dual-Source Search Engine**: Concurrently queries Merriam-Webster (formal definitions) and Urban Dictionary (slang/vernacular) in a single request.
* **Viewer Discretion Slang Matrix**: Urban Dictionary definitions are shielded behind an expandable warning banner (`animateContentSize`) to prevent explicit or unfiltered terms from displaying automatically.
* **Search Ledger & Snippets**: Tracks search history locally, storing word snippets and cached definition data.
* **Instant Offline Reloading**: Selecting any term from the history ledger reloads its full definition instantly from local memory without triggering network calls or consuming API quotas.
* **Pin & Favorites System**: Users can star items (`★`). Favorited entries automatically pin to the top of the history ledger.
* **Sharp Editorial UI**: Built with a high-contrast, sharp-edged monochrome design system tailored for desktop and mobile layouts.

---

## Project Architecture

The application follows Clean Architecture principles decoupled inside `shared/src/commonMain/`:

```
shared/src/commonMain/kotlin/com/example/dictionary/
├── data/               # Network clients, Ktor configuration, and repository implementations
│   └── DictionaryRepository.kt
├── domain/             # Core data classes, models, and UiState sealed classes
│   └── Models.kt
├── presentation/       # StateFlow management, offline caching, and business logic
│   └── DictionaryViewModel.kt
├── ui/                 # Modular Compose Multiplatform UI screens
│   ├── InputScreen.kt
│   ├── ResultScreen.kt
│   └── HistoryScreen.kt
└── App.kt              # App entry point, Material 3 theme, and tab navigation

```

---

## Tech Stack & Dependencies

* **Language**: Kotlin 2.x
* **UI Framework**: Compose Multiplatform (Android / Desktop JVM)
* **Networking**: Ktor Client (`ktor-client-core`, `ktor-client-content-negotiation`)
* **Serialization**: `kotlinx.serialization` (JSON parsing)
* **Asynchronous Flow**: Kotlin Coroutines & `StateFlow`
* **Design System**: Material 3 with customized sharp geometric components

---

## How to Run

### Prerequisites

* JDK 17 or higher
* Android Studio (Ladybug or newer) or IntelliJ IDEA Ultimate with KMP plugins enabled

### Desktop (JVM)

Run the following command in your terminal:

```bash
./gradlew desktopRun -DmainClass=MainKt

```

### Android

1. Open the project in Android Studio.
2. Select the `androidApp` configuration.
3. Launch on a connected Android device or emulator.