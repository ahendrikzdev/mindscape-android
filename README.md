# Mindscape — Android

A native Android port of the Mindscape HTML prototype: learn from short videos, swipe them
into lessons or onto a constellation-style knowledge map, answer quizzes, earn XP, and unlock
skill nodes.

Built with **Kotlin + Jetpack Compose** and packaged as an installable debug APK by **GitHub
Actions**. All fonts and thumbnails are bundled, so the app needs no network permission and
works offline.

## Download the APK from GitHub Actions

1. Push this project to GitHub (the workflow lives in `.github/workflows/android.yml`).
2. Open the repository's **Actions** tab → **Android CI** → the latest successful run.
3. Download the **mindscape-debug-apk-&lt;run number&gt;** artifact.
4. Unzip it and install `app-debug.apk`:

```bash
adb install -r app-debug.apk
```

You can also run the workflow manually with **Run workflow** (`workflow_dispatch`). Pushes to
`main`, pull requests, and `v*` tags run the pipeline automatically.

## What the CI job does

- JDK 17 (Temurin), Gradle 8.13 with dependency caching
- Android SDK platform 36 and build-tools 35.0.0
- `./gradlew testDebugUnitTest lintDebug assembleDebug`
- Uploads `app/build/outputs/apk/debug/app-debug.apk` as a workflow artifact
  (fails if the APK is missing)

## Local development

Requirements: JDK 17, Android SDK 36, and `local.properties` pointing at your SDK
(`sdk.dir=/path/to/Android/Sdk`).

```bash
./gradlew testDebugUnitTest   # JVM unit tests
./gradlew lintDebug          # Android lint
./gradlew assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
```

Open the folder in Android Studio (Ladybug or newer) for the Compose previews/tooling.
The project pins AGP 8.13.2, Kotlin 2.2.21, Compose BOM 2025.12.01, `compileSdk`/`targetSdk` 36,
and `minSdk` 24.

## Feature parity

- **Learn panel** — active lesson card, Lesson/Quiz segmented control, key takeaway, quiz
  progress, multiple-choice quiz with explanations, XP updates.
- **Watch panel** — vertical snap feed of video cards with bundled thumbnails. Swipe a card
  **left** to open its lesson, **right** to focus its node on the map; delete or jump to the map
  from the side actions.
- **Map panel** — realm switcher (Physics & Cosmos / Ancient World), XP + animated completion
  ring, interactive constellation skill tree (dashed locked edges, glowing unlocked edges),
  node selection, and a horizontal carousel of that node's videos.
- **Import flow** — link field, topic-node picker, simulated 750 ms AI extraction, spinner, and
  automatic navigation to the new lesson. Imported videos are pinned to the chosen node.
- **Navigation** — header pills + dots, floating glass bottom bar with center collect button,
  back handling (import dialog → Watch panel), haptics on swipe and answer.

### Intentional improvements over the prototype

- +100 XP is granted **once per video** (the HTML awarded it on every repeat correct tap).
- Blank imports are rejected and the extract button is disabled while running.
- Deleting the active video falls back to the next video (or an empty state) instead of leaving
  stale lesson content.
- Switching realms resets node/artifact selection coherently.
- No INTERNET permission: the five Unsplash thumbnails are bundled as drawables.

State is kept in the ViewModel and resets when the app process is recreated, matching the
prototype's reload behavior. Cross-restart persistence is intentionally out of scope.

## Architecture

```
app/src/main/java/com/mindscape/app/
├── MainActivity.kt              edge-to-edge host
├── MindscapeViewModel.kt        StateFlow + simulated import pipeline
├── data/
│   ├── Models.kt                domains, nodes, artifacts, quiz, UI state
│   └── MindscapeRepository.kt   seed catalog from the HTML
├── domain/
│   └── MindscapeEngine.kt       pure, unit-tested state transitions
└── ui/
    ├── MindscapeApp.kt          pager, header, footer, import dialog wiring
    ├── theme/                   forest/glass palette + bundled typography
    ├── components/              glass surfaces, progress ring, skill tree, video cards
    └── screens/                 Learn, Watch, Map, ImportDialog
```

`app/src/test/java/com/mindscape/app/domain/MindscapeEngineTest.kt` covers XP eligibility,
wrong answers, node mastery/unlocking, realm selection, deletion fallback, imports, and
completion math.

## Release signing (optional)

The default artifact is debug-signed and installable. To ship a release build, add a keystore
to the repository's secrets and add a signing config / release job — the workflow is structured
so a second job can decode `KEYSTORE_BASE64` and run `assembleRelease` or `bundleRelease`.
