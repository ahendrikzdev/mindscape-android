# Mindscape — Android

A native Android port of the Mindscape HTML prototype: learn from short videos, open lessons,
pin topics to a constellation-style knowledge map, answer quizzes, earn XP, and unlock skill
nodes.

Built with **Kotlin + Jetpack Compose** and packaged as an installable debug APK by **GitHub
Actions**. Learning content, fonts, and thumbnails are bundled; the AI import flow uses the
network when a backend is configured and otherwise runs a transparent on-device mock analyzer.

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
- **Watch panel** — vertical snap feed of video cards with bundled thumbnails. Each card has
  explicit **Open Lesson & Quiz**, **Show on map**, and delete actions.
- **Map panel** — realm switcher (Physics & Cosmos / Ancient World), XP + animated completion
  ring, interactive constellation skill tree (dashed locked edges, glowing unlocked edges),
  node selection, and a horizontal carousel of that node's videos.
- **Import flow** — YouTube or direct-media URL validation, optional transcript, staged
  progress, error/retry handling, and automatic navigation to the new lesson. The default
  **mock analyzer** builds an extractive summary and cloze quiz on-device from the transcript;
  without one, the video is imported as watch-only. When a backend URL is configured, the same
  UI uses the remote job API.
- **Navigation** — swipe horizontally between Learn, Watch and Map, or use the header pills,
  dots, and floating glass bottom bar (with center collect button). Back returns to Watch.

### Intentional improvements over the prototype

- +100 XP is granted **once per video** (the HTML awarded it on every repeat correct tap).
- URL validation rejects unsupported links and the analyze button is disabled while running.
- Imported videos with no transcript are explicitly watch-only instead of receiving invented
  content; re-import with a transcript to get a summary and quiz.
- Deleting the active video falls back to the next video (or an empty state) instead of leaving
  stale lesson content.
- Switching realms resets node/artifact selection coherently.
- Imported content and quiz progress persist across app restarts via DataStore (schema v1).
- Thumbnails are persisted as stable string keys, never Android resource ids.

The remote AI backend is **not deployed yet**. With no `BACKEND_BASE_URL` configured, the app
runs `MockLessonAnalyzer`: real URL parsing plus deterministic TextRank summary and cloze quiz
generation from pasted text. Video playback is the next phase; seeded items already carry their
YouTube source metadata.

## Architecture

```
app/src/main/java/com/mindscape/app/
├── MainActivity.kt              edge-to-edge host
├── MindscapeViewModel.kt        StateFlow, DataStore restore/save, analyzer wiring
├── analysis/
│   ├── Analysis.kt              request/result contract + stages
│   ├── VideoLinks.kt            YouTube/direct URL parser
│   ├── MockLessonAnalyzer.kt    on-device transcript analysis (current default)
│   ├── RemoteLessonAnalyzer.kt  async job-API client for the future backend
│   └── AnalyzerProvider.kt      remote when configured, otherwise mock
├── data/
│   ├── Models.kt                serializable domains, artifacts, quizzes, UI state
│   ├── MindscapeRepository.kt   seed catalog with stable thumbnail keys
│   ├── MindscapeStateCodec.kt   versioned JSON codec
│   ├── MindscapeStateStore.kt   DataStore persistence
│   └── ThumbnailCatalog.kt      stable key → bundled drawable mapping
├── domain/
│   ├── MindscapeEngine.kt       pure, unit-tested state transitions
│   └── LessonBuilder.kt         TextRank summary + cloze quiz
└── ui/
    ├── MindscapeApp.kt          pager, header, footer, import dialog wiring
    ├── theme/                   forest/glass palette + bundled typography
    ├── components/              glass surfaces, progress ring, skill tree, video cards
    └── screens/                 Learn, Watch, Map, ImportDialog
```

Tests cover the engine (XP eligibility, wrong answers, mastery, selection, deletion), URL
parsing, mock inference, transcript summarization/quiz generation, and JSON state round-trips.

## AI import configuration

### Mock mode (default, no secrets)

- `MockLessonAnalyzer` parses YouTube/direct URLs, uses known titles for the seeded videos, and
  builds a real summary + cloze quiz from the pasted transcript.
- No transcript → watch-only import with an explanatory lesson screen.
- No backend URL or API key is required; this is what the current Actions build uses.

### Remote mode (when the backend exists)

Add these to GitHub → Settings → Secrets and variables → Actions, then rebuild:

| Name | Type | Purpose |
|---|---|---|
| `BACKEND_BASE_URL` | Repository variable | HTTPS base URL of the AI backend |
| `BACKEND_API_KEY` | Secret | Shared `X-API-Key` for the backend |

The client posts `{url, transcript?}` to `/v1/jobs`, polls `/v1/jobs/{job_id}`, and maps
`summary`, `key_points`, `quizzes[]`, and source metadata into the existing model. If either
value is blank, the mock analyzer is selected automatically.

> The build-time API key is extractable from the APK. Use a rate-limited backend and consider
> user auth before public distribution.

## Release signing (optional)

The default artifact is debug-signed and installable. To ship a release build, add a keystore
to the repository's secrets and add a signing config / release job — the workflow is structured
so a second job can decode `KEYSTORE_BASE64` and run `assembleRelease` or `bundleRelease`.
