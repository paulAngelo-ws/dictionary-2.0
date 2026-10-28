# Simple Dictionary

A beginner-oriented Kotlin Multiplatform app for Merriam-Webster definitions, spelling suggestions, search history, and favorites.

## Find the code quickly

Start in `shared/src/commonMain/kotlin/com/example/dictionary/`.

| What you want to change | File |
| --- | --- |
| Definitions and spelling suggestions | `ui/ResultScreen.kt` |
| Saved words and favorite buttons | `ui/HistoryScreen.kt` |
| Screen navigation and app setup | `App.kt` |
| Loading, errors, and search actions | `presentation/DictionaryViewModel.kt` |
| Data classes | `domain/Models.kt` |
| API calls and cached definitions | `data/DictionaryRepositoryImpl.kt` |
| Database tables and queries | `data/local/` |

Follow a search in this order: **InputScreen → App → DictionaryViewModel → DictionaryRepositoryImpl**. Results return through `UiState` to `ResultScreen`.

The ViewModel calls the repository interfaces directly. The three forwarding use-case classes have been removed. `UiState` lives beside the ViewModel in the same file. The domain repository files are interfaces; the data files contain their implementations.

Screen files keep comments brief. For detailed Kotlin and Compose explanations, use the beginner study guide linked below. Its line numbers refer to the earlier source snapshot; search by function name in the current code.

## Team and submission documents

Course: BSIT-SD, 2nd year. Section: IDB1-SD.
Repository: https://github.com/paulAngelo-ws/dictionary-2.0.git

| Member name | Role | Actual contribution / commit references |
| --- | --- | --- |
| ____________________ | Developer | To be confirmed |
| ____________________ | BA | To be confirmed |
| ____________________ | QA 1 | To be confirmed |
| ____________________ | QA 2 | To be confirmed |

The reported team has four members; the exam specifies three. Confirm the arrangement with the instructor. Role responsibilities and actual contribution evidence must be completed by the team before submission.

Draft [requirements](docs/Requirements.md) and [QA package](docs/QA.md) are in `docs/`, with editable Word and PDF copies. See [the document checklist](docs/README.md) before submitting. Android runtime testing and manual case results remain outstanding; a reported desktop test session is not evidence that every acceptance criterion passed.

## How a search works

1. `InputScreen` sends the typed word to `App`.
2. `App` opens the result route and calls `DictionaryViewModel.searchWord`.
3. The ViewModel shows Loading and asks `DictionaryRepositoryImpl` for definitions.
4. The repository checks Room first. If there is no useful cached result, it requests Merriam-Webster definitions.
5. Useful definitions are saved in Room and returned as `DictionaryResult`.
6. The ViewModel updates `UiState`, and `ResultScreen` displays definitions or a spelling suggestion.
7. Cached definitions display immediately and refresh in the background. Failed background refreshes keep the saved result visible.

History observes Room changes automatically. Favorites survive refreshes and app restarts. Older saved results remain readable; history displays only entries with formal definitions.

## Platforms and storage

Shared UI and logic live in `shared/src/commonMain`. Each platform creates its HTTP client and Room database. `AppStorage` holds only the database; no settings store is needed.

Networking uses Ktor ContentNegotiation and kotlinx.serialization. Requests have a 10-second timeout. Request logging is disabled because the API URL contains a key.

Android uses OkHttp, desktop uses CIO, iOS uses Darwin, and browsers use the JS engine. iOS builds require macOS and Xcode.

Browser storage is isolated in `browserStorage/` and uses SQLite WASM with OPFS. Its Apache-2.0 license is retained in `browserStorage/LICENSE.txt`. Serve the browser app on HTTPS or localhost with these headers:

```text
Cross-Origin-Opener-Policy: same-origin
Cross-Origin-Embedder-Policy: require-corp
```

Desktop data is stored in `~/.kmp-dictionary/`; Android uses its private database directory, iOS uses Documents, and browsers use OPFS. Browser API access also depends on the service's CORS policy.

## Run and check

Use JDK 17 or newer and configure the Android SDK for Android builds.

```bash
./gradlew :desktopApp:run
./gradlew :shared:jvmTest :desktopApp:test
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDevelopmentRun
```

The JVM tests use temporary databases and mocked HTTP responses. They check persistence, favorites, refreshes, invalid responses, spelling suggestions, compatibility with older saved results, and that requests go only to Merriam-Webster.

The files in `docs/` describe earlier app snapshots and may include features that have since been removed. The code map and search flow above describe the current app.
