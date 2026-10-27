# KMP Dictionary — Modules 6–9

A beginner-oriented dictionary app using the architecture and libraries taught in the four supplied MOBAPP1 modules. The existing formal definitions, slang, spelling suggestions, search history, and favorites are retained.

## Module 6: project structure and architecture

Shared code is in `shared/src/commonMain/kotlin/com/example/dictionary/`:

| Folder/file | Responsibility |
| --- | --- |
| `domain/Models.kt` | Plain dictionary and history data classes. |
| `domain/DictionaryRepository.kt` | Interface hiding the network and database. |
| `domain/SearchWordUseCase.kt` | Trims/lowercases a word and requests its definitions. |
| `domain/HistoryUseCase.kt` | Observes history and changes favorites through the interface. |
| `domain/SlangPreferenceUseCase.kt` | Reads and saves the slang-display preference. |
| `data/DictionaryRepository.kt` | Ktor requests, local cache lookup, and database updates. |
| `data/local/` | Room entity, DAO, and database. |
| `data/DictionaryPreferences.kt` | DataStore preference read/write functions. |
| `presentation/` | Shared ViewModel-like state holder and sealed UI states. |
| `ui/` | Compose screens that render state and send callbacks. |
| `App.kt` | Constructs dependencies and connects screens to navigation. |

Dependencies are passed through constructors, as in the module. There is no DI framework. The ViewModel depends on use cases and has no Ktor, Room, or Android imports.

`expect fun createHttpClient()` lives in commonMain. Its `actual` implementations use OkHttp on Android, Darwin on iOS, CIO on desktop, and the JS engine in browsers. Engines are dependencies of their platform source sets. Common networking plugins are configured once.

Storage factories also live in the relevant platform source sets because the platforms use different file locations. Platform entry points create storage and pass it to `App`.

## Module 7: shared state

`UiState` is a sealed class with Idle, Loading, Success, and Error. The UI renders it with an exhaustive `when`.

The ViewModel keeps private `MutableStateFlow` values and exposes public `StateFlow` values for results, history, and the preference. Coroutines perform asynchronous work. History originates from Room's `Flow`, so saving a search or changing a favorite updates the screen automatically.

A `SharedFlow<String>` carries one-time snackbar messages, such as a failed refresh or a saved preference. It does not hold screen state. Starting a new search cancels the old search; cancellation is rethrown rather than treated as a network error. The Compose-owned scope is cancelled when App leaves composition.

## Module 8: UI and navigation

Compose Multiplatform shares all three screens. `collectAsState()` connects the ViewModel's flows to Compose.

`Destination` is a sealed class. `NavHost` uses the simple string routes demonstrated in the module:

- `input`
- `history`
- `result` for the empty result screen
- `result/{word}` for a selected word

The word is encoded into the route and read from the destination arguments. It is no longer supplied by an unrelated active-word variable. The navigation controller maintains a back stack.

The result destination declares `dictionary://word/{word}` as its deep-link pattern. Android's manifest registers the URL scheme, so a cold launch with `dictionary://word/hello` can open the matching destination. Native OS link registration/forwarding for iOS and desktop is not included; declaring a shared route alone does not register it with those operating systems.

SwiftUI adapters are discussed as an alternative in the module. This app uses shared Compose UI on iOS, so it does not add an unused SwiftUI wrapper.

## Module 9: networking and persistence

### Typed responses and errors

Ktor uses ContentNegotiation and kotlinx.serialization. API response models have `@Serializable`; unknown fields are ignored. HTTP statuses are checked, and network/JSON failures are caught. The other dictionary can still supply definitions when one service fails. When no usable response is available, the repository returns null and the ViewModel chooses the error UI.

Merriam-Webster returns either definition objects or strings containing spelling suggestions. One array-item check distinguishes these formats; typed serialization handles the definition fields. This small adaptation is required by the existing API's response format.

The Logging plugin uses `LogLevel.INFO`. Only Urban Dictionary requests are logged, because Merriam-Webster puts its API key in the URL.

### Room database and caching

The previous memory-only cache has been replaced with Room/SQLite. Each `search_history` row stores a word, snippet, serialized result, favorite flag, and search order. JSON serialization stores the lists of definitions in a text column; no custom Room type converter is needed.

A lookup checks the local database first. A cached result appears immediately, then the ViewModel requests fresh definitions in the background. A failed refresh keeps the displayed and stored definition. The Refresh definitions button explicitly bypasses the cache. Useful responses are saved; empty results and spelling-only responses are not saved as history. Refreshing a word preserves its favorite flag.

History is observed directly from the database, with favorites first and newer searches next. Definitions and favorites survive app restarts and can be reopened without a successful network request.

### DataStore preferences

The input screen has a Show slang by default checkbox. It is initially off. `DictionaryPreferences` uses the module's `booleanPreferencesKey`, `edit`, and `data.map` pattern to save and observe this value. The preference survives app restarts. Definitions are in Room, rather than being treated as preferences.

### Room compared with SQLDelight

| Room — chosen for this app | SQLDelight — alternative in the module |
| --- | --- |
| Annotated Kotlin entity/DAO classes describe the database. | Hand-written `.sq` files describe the database and queries. |
| Generates the database implementation with KSP. | Generates Kotlin APIs from SQL files. |
| Fits this app's Kotlin-first structure. | Fits a project that prefers working primarily in SQL. |

They are alternatives; this project does not install two databases for the same history.

## Browser infrastructure

Both JavaScript and Wasm targets are retained. Browser-only worker code is isolated in `browserStorage/`, outside the beginner-facing app layers. Room uses SQLite WASM in a worker with Origin Private File System (OPFS) storage; DataStore uses localStorage for the preference.

The worker is adapted from the [Room web demo linked by Android's documentation](https://github.com/danysantiago/room-web-demo). Its Apache-2.0 license is retained in `browserStorage/LICENSE.txt`. The setup follows [Room's web support](https://developer.android.com/jetpack/androidx/releases/room3) and [DataStore's KMP setup](https://developer.android.com/kotlin/multiplatform/datastore).

The browser database needs a modern browser, HTTPS (or localhost), and these response headers. The development webpack configuration supplies them; a production host must supply them too:

```text
Cross-Origin-Opener-Policy: same-origin
Cross-Origin-Embedder-Policy: require-corp
```

Storage is local to the browser origin/profile. Clearing site data removes it. API availability in browsers also depends on the dictionary services' CORS policies.

## Running and checking

Use JDK 17 or newer. Android builds require the SDK configured by the project; iOS builds require macOS and Xcode.

```bash
./gradlew :desktopApp:run
./gradlew :shared:jvmTest
./gradlew :webApp:wasmJsBrowserDevelopmentRun
./gradlew :webApp:jsBrowserDevelopmentRun
```

When changing dependencies, stop the running desktop/Hot Run process and start it again. Hot reload does not replace the process's dependency classpath. The navigation version is kept in `gradle/libs.versions.toml` and aligned with the current Compose/saved-state APIs; the module's basic string-route pattern is retained.

On Android, run the `androidApp` configuration from Android Studio.

Desktop storage is in `~/.kmp-dictionary/`; Android uses the app's private files/database directories, iOS uses Documents, and browsers use OPFS/localStorage.

The JVM tests use a temporary SQLite database and mocked HTTP responses. They check reopening saved definitions/favorites without network access, failed and successful refreshes, invalid responses, spelling-only results, and a DataStore preference reopened from disk. They do not call the live dictionary services.
