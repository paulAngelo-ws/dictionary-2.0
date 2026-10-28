# Simple Dictionary — QA Package

DRAFT — test plan, test cases/results and defect log. Manual outcomes have not been invented.

| Item | Details |
| --- | --- |
| Course / section | BSIT-SD, 2nd year / IDB1-SD |
| Repository | https://github.com/paulAngelo-ws/dictionary-2.0.git |
| QA 1 / QA 2 | ____________________ / ____________________ |
| Test dates / final revision | ____________________ / ____________________ |
| Current readiness | Not ready for final sign-off: Android runtime and documented manual results are outstanding. |

## 1. Test plan

### Purpose and scope

Verify the Requirements document's AC-01 to AC-12 and the application of Modules 6–9. Test normal search, input validation, loading/errors, navigation, suggestions, history, favorites, offline cache, background/manual refresh and the saved slang preference. Android is the required examination platform; desktop is a supporting platform.

Use a separate test profile/emulator where possible. Clearing app data removes saved words and preferences. Record the actual build/commit, environment and time before each execution. Run cases independently and restore their preconditions.

### Environment record

| Environment | Known information | Missing information / status |
| --- | --- | --- |
| Desktop manual tests | User reports having tested the desktop version. | OS/version, JDK, exact build, steps and outcomes have not been supplied. No individual manual case is marked Pass. |
| Automated JVM tests | Existing local JUnit XML reports show six tests passing with zero failures/errors on 27 October 2026. | These are earlier recorded runs, not a fresh execution against the final frozen submission. Rerun after the final code freeze. |
| Android runtime | No device/emulator testing has been reported. | Device/AVD, Android version/API, resolution, APK/build revision and results: Not run. |
| Browser runtime | Browser builds were previously checked; no completed manual runtime report is supplied. | Runtime behavior is unverified. |
| iOS | Source set retained; no macOS/Xcode runtime evidence supplied. | Not run. |

Android environment to complete: Device/AVD __________; Android version/API __________; screen size __________; revision __________; test date __________; tester __________.

### Test approach and responsibilities

QA 1 is proposed to lead desktop functional checks and maintain the defect log. QA 2 is proposed to lead Android, offline and restart checks. Both should review automated tests and cross-review results. Actual ownership and completed contributions must be confirmed by the team; these allocations do not establish past work.

Use hello for an initial online lookup. For a misspelling/suggestion case, first confirm an input that currently produces a suggestion; external service responses can change. If a case cannot reach its required precondition, record Blocked rather than claiming Pass. For data persistence tests, fully close and reopen the application, rather than only switching tabs or hot reloading.

### Status and evidence rules

Not run = no documented execution. Pass = actual behavior matches the criterion. Fail = mismatch with reproducible evidence. Blocked = setup or dependency prevents execution. Every manual result should include platform, date, tester, revision, actual outcome and a screenshot/log reference.

Evidence naming: TC-01_android_search.png, TC-07_desktop_offline.png, BUG-001_navigation_log.txt. Store evidence in docs/evidence and reference the filenames. Do not include credentials or API keys in screenshots/logs.

## 2. Manual test cases and results

All cases below are planned. The report of general desktop testing does not establish a result for a particular case. Complete each case on Android, and record desktop outcomes separately if also executed there.

### TC-01 — Valid lookup (AC-01)

Preconditions: Internet available; API credentials valid; chosen word not already cached.

Steps: Open Input; enter hello; select Search; inspect Result.

Expected: The selected word appears with at least one returned definition and a source label. Display part of speech/example if provided by the API.

Result: Desktop Not run (documented case); Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-02 — Blank input (AC-02)

Steps: Try Search with an empty input; repeat with spaces only; inspect History.

Expected: No lookup/navigation caused by blank input and no blank history entry.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-03 — Loading and failed uncached lookup (AC-03)

Preconditions: Choose a word absent from History. Use a throttled connection for Loading if available.

Steps: Start a lookup and observe Loading. Disable connectivity, then search an uncached word.

Expected: Loading is represented while waiting. The failed uncached lookup ends with a readable error and no application crash. Allow the configured request timeouts to complete.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-04 — Spelling suggestion (AC-04)

Preconditions: Record an input for which the formal dictionary returns a suggestion. The app must have API access.

Steps: Search that input; tap the suggestion; inspect the new result and History.

Expected: The suggested word is passed to its result screen. A suggestion without any definition is not saved as a definition/history record. If the original input also has valid slang definitions, it may legitimately be saved.

Result: Desktop Not run; Android Not run. Input used / actual outcome / evidence / tester / date: ____________________.

### TC-05 — Navigation and selected word (AC-05)

Steps: Open each bottom tab; search a word; open History and select the saved word. On Android, use system Back.

Expected: All three screens work, Result identifies the selected word, and Android Back returns through the stack without a crash.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-06 — History after restart (AC-06)

Steps: Complete an online lookup; confirm it in History; fully close the app; reopen it and select the word from History.

Expected: The word, snippet and saved definitions remain available after restarting.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-07 — Offline saved word (AC-07)

Preconditions: Save a successful search while online.

Steps: Close the app; disable connectivity; reopen it; select the saved word. Wait for the background refresh attempt to fail.

Expected: Saved definitions are displayed and remain usable. A refresh failure may show a message but does not replace cached results with a failure screen.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-08 — Favorites and ordering (AC-08)

Preconditions: At least two saved words.

Steps: Star a word; inspect history ordering; restart; inspect again; refresh that word online; unstar it.

Expected: Favorites appear before nonfavorites. The flag survives restart and a successful refresh; unstarring updates the list.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-09 — Saved slang preference (AC-09)

Preconditions: Use fresh test app data to verify the initial default. Choose a word returning slang.

Steps: Confirm the setting starts off; enable it; fully restart; inspect the setting and a slang result. Disable it and repeat.

Expected: The saved value is restored. Slang expansion follows the chosen default; the result still permits manual show/hide.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-10 — Manual refresh (AC-10)

Preconditions: A displayed successful saved result.

Steps: Select Refresh while online; verify the result and favorite. Disable connectivity and refresh again.

Expected: Online refresh completes and retains the favorite. Failed refresh leaves the previous successful result usable. Identical online definitions are acceptable; a refresh need not change the text.

Result: Desktop Not run; Android Not run. Actual outcome / evidence / tester / date: ____________________.

### TC-11 — Rapid successive searches (AC-11)

Steps: With a slow connection, search a word; promptly return to Input and search a different word; wait for requests to settle.

Expected: The final result belongs to the latest selected word; an older request does not overwrite it.

Result: Desktop Not run; Android Not run. Words / actual outcome / evidence / tester / date: ____________________.

### TC-12 — Required Android installation and end-to-end flow (AC-12)

Steps: Record the device/AVD and revision; install/build the app using the README; launch it; execute TC-01 and TC-05 through TC-10.

Expected: Installation and launch succeed; the main flows work on Android without a crash. Record individual case results, not only a general launch success.

Result: Android Not run. Environment / actual outcome / evidence / tester / date: ____________________.

### TC-13 — Android deep link (additional implemented feature)

Steps: With the app stopped, open dictionary://word/hello on the test Android device. If using adb, use the command in section 5.

Expected: Android opens the app at the hello result destination. With no saved hello entry and no connectivity, a readable network error is an acceptable lookup result; navigation must still target hello.

Result: Android Not run. Actual outcome / evidence / tester / date: ____________________.

## 3. Recorded automated results

The rows below are transcribed from existing local reports and the corresponding test functions. They do not represent manual device testing. The report timestamps are UTC; times below are converted to Asia/Manila. Copies of the original XML files are included in docs/evidence.

| ID | Automated check | Recorded result |
| --- | --- | --- |
| AT-01 | Definitions and favorites survive reopening the database; a cached lookup requires no network call. | Pass — shared JVM report, 27 Oct 2026, 20:33:04 PHT suite start |
| AT-02 | Failed forced refresh keeps the stored word and favorite. | Pass — same shared JVM report |
| AT-03 | Successful refresh changes definitions without clearing favorites. | Pass — same shared JVM report |
| AT-04 | Spelling-only and malformed responses are not saved as definitions. | Pass — same shared JVM report |
| AT-05 | The slang preference survives recreating DataStore. | Pass — same shared JVM report |
| AT-06 | A rendered navigation host can open a word route, read its argument and navigate back. | Pass — desktop navigation report, 27 Oct 2026, 20:34:43 PHT suite start |

Sources: shared/src/jvmTest/kotlin/com/example/dictionary/PersistenceTest.kt and desktopApp/src/test/kotlin/com/example/dictionary/NavigationCompatibilityTest.kt. Five shared tests plus one desktop test; zero recorded failures/errors. Network responses in persistence tests are mocked, not live API calls. The final submission revision has not been recorded; rerun these tests and update evidence after the code freeze.

## 4. Defect log

### BUG-001 — Desktop navigation crashes during startup/tab navigation

Reported evidence: The supplied desktop Hot Run log showed NoSuchMethodError for SavedStateRegistryController.performRestore when NavHost created/updated a navigation entry.

Severity: High — prevents a normal navigation demo.

Reproduction: Run the desktop app with Navigation 2.8.0-alpha10 and the newer saved-state/lifecycle dependencies; create the initial navigation host or select a navigation tab.

Expected: The app opens and moves between screens without a crash.

Actual: A saved-state method is missing at runtime; navigation throws an exception.

Cause: Binary incompatibility between the old Navigation artifact and newer saved-state dependencies.

Fix: Use Navigation 2.10.0-rc01 from the version catalog and the compatible SavedState argument-reading API. Restart the running Hot Run process after changing dependency versions.

Verification: Existing automated desktop navigation regression test passed. Manual retest on the team's desktop setup and Android remains to be recorded.

Status: Fixed in code; automated regression passed; manual closure pending. Reporter / fixer / retester / final commit: ____________________.

### Additional defect entry template

ID / title: ____________________
Platform / environment / revision: ____________________
Reporter / date / severity: ____________________
Preconditions and steps: ____________________
Expected / actual behavior: ____________________
Evidence reference: ____________________
Fix and responsible member: ____________________
Retest outcome / tester / date / status: ____________________

## 5. Execution commands and evidence

Run from the repository root. Use the JDK/Android SDK setup described in the README.

```bash
./gradlew :shared:jvmTest :desktopApp:test
./gradlew :desktopApp:run
./gradlew :androidApp:assembleDebug
```

Expected APK location: androidApp/build/outputs/apk/debug/androidApp-debug.apk. Verify the actual generated filename before recording it in the submission.

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
adb shell am start -W -a android.intent.action.VIEW -d 'dictionary://word/hello' com.example.dictionary
```

Alternatively, select androidApp in Android Studio and run on the recorded emulator/device. The automated HTML reports are generated under shared/build/reports/tests/jvmTest and desktopApp/build/reports/tests/test. Copy final XML/HTML results or screenshots into the submission evidence after executing the frozen revision.

## 6. Exit criteria and sign-off

Final sign-off requires all acceptance criteria tested on Android, no unresolved crash/blocking defects in the main flow, passing automated tests against the final revision, documented defect retests, and an APK or confirmed emulator instructions. Any exception must be recorded explicitly.

Current disposition: Pending. General desktop testing has been reported and six earlier automated results are available, but Android runtime evidence and case-specific manual results are outstanding.

QA 1: ____________________  QA 2: ____________________
BA acceptance: ____________________  Developer review: ____________________
Final revision / date: ____________________ / ____________________
