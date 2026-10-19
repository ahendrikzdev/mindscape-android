# Mindscape — Android Conversion Plan

**Status:** approved · **Source:** `mindscape.html` prototype · **Package:** `com.mindscape.app`

## 1. Goal & approach

Rewrite the single-file HTML prototype as a **native Kotlin + Jetpack Compose Android app**
(not a WebView wrapper). The port preserves the forest/glass/neon visual language, the three
panels, quiz scoring, XP, node mastery/unlocking, the constellation map, the import flow, and
both card swipe gestures.

| | Native Compose (chosen) | WebView wrapper (rejected) |
|---|---|---|
| Gestures | Native pager + pointer input | Depends on page JS |
| Offline | Fonts/images bundled, no CDN | Tailwind, Font Awesome, Google Fonts CDNs |
| Integration | Edge-to-edge, back handling, haptics | Limited |
| Maintenance | Typed state + testable logic | HTML/JS string |

Import stays a simulated 750 ms "AI extraction", exactly like the prototype. No backend.

## 2. Pinned toolchain

- **AGP 8.13.2**, **Gradle 8.13**, **JDK 17** (AGP 8.13 requires Gradle 8.13+, JDK 17, max API 36.1)
- **Kotlin 2.2.21** + `org.jetbrains.kotlin.plugin.compose`
- **Compose BOM 2025.12.01**, Material 3, Foundation Pager, `material-icons-extended:1.7.8`
- `compileSdk`/`targetSdk` = **36**, `minSdk` = **24**
- `activity-compose 1.11.0`, `lifecycle-* 2.9.4`, `core-ktx 1.17.0`
- JUnit 4.13.2, `kotlinx-coroutines-test 1.10.2`
- No Coil/Retrofit: all art is bundled, so the app needs **no INTERNET permission**.

## 3. Project layout

```
/home/projects/prodapp
├── PLAN.md · README.md · .gitignore
├── settings.gradle.kts · build.gradle.kts · gradle.properties
├── gradle/libs.versions.toml · gradle/wrapper/…  · gradlew · gradlew.bat
├── .github/workflows/android.yml
└── app/
    ├── build.gradle.kts · proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/mindscape/app/
        │   │   ├── MainActivity.kt · MindscapeViewModel.kt
        │   │   ├── data/{Models.kt, MindscapeRepository.kt}
        │   │   ├── domain/MindscapeEngine.kt
        │   │   ├── ui/MindscapeApp.kt
        │   │   ├── ui/theme/{Color.kt, Type.kt, Theme.kt}
        │   │   ├── ui/components/{Glass.kt, GradientButton.kt,
        │   │   │                   ProgressRing.kt, SkillTree.kt, VideoCard.kt}
        │   │   └── ui/screens/{LearnScreen.kt, WatchScreen.kt,
        │   │                    MapScreen.kt, ImportDialog.kt}
        │   └── res/
        │       ├── font/ (Plus Jakarta Sans + Space Grotesk, already bundled)
        │       ├── drawable-nodpi/thumb_*.jpg (5 Unsplash images, ~325 KB)
        │       ├── drawable/ic_launcher_foreground.xml
        │       ├── mipmap-*/ic_launcher.png (+ adaptive v26 XML)
        │       ├── values/{strings.xml, colors.xml, themes.xml}
        │       └── xml/backup_rules.xml
        └── test/java/com/mindscape/app/domain/MindscapeEngineTest.kt
```

## 4. HTML → Compose mapping

| HTML | Compose |
|---|---|
| `.viewport-wrapper` 3-panel carousel | `HorizontalPager` (3 pages, starts on Watch, user scroll disabled; header/dots/footer animate it) |
| Fixed header, dot indicators, glass footer | Overlay rows with translucent glass surfaces and gradient scrims |
| Snap vertical feed, 480 px cards | `VerticalPager`, full-bleed cards |
| Card swipe L/R | `detectHorizontalDragGestures` + alpha "Learn"/"Map" indicators, 60 dp threshold, haptics |
| Lesson / Quiz tabs | Custom segmented glass pill + scrollable column |
| Quiz options/feedback | State-driven rows, locked after answering, lime/rose feedback, +100 XP |
| SVG constellation + nodes | `Canvas` edges + percentage-positioned node composables |
| Progress ring | `Canvas.drawArc` with animated sweep |
| Realm `<select>` | Glass dropdown pill + `DropdownMenu` |
| Node video carousel | `LazyRow` of 112 dp cards |
| Import modal | Compose `Dialog`, URL field, node dropdown, spinner, 750 ms delay |
| `openVideoLearning` / `highlightVideoInMap` | ViewModel selection + animate pager |
| Tailwind glass/colors/fonts | Theme tokens, bundled fonts, translucent fills/borders (no backdrop blur) |

Theme: `#030A06` background, `#091A11` cards, `#122A1D` borders, `#A3E635` / `#10B981` /
`#06B6D4` accents. Plus Jakarta Sans for UI, Space Grotesk for labels. Dark-only,
edge-to-edge, content capped at ~460 dp and centered on tablets.

## 5. State & logic

- `MindscapeRepository` seeds the two realms and five videos from the HTML.
- `MindscapeViewModel` exposes `StateFlow<MindscapeUiState>` (domains, selected node, active
  artifact, feed, XP, answers, learn tab) and action methods.
- `MindscapeEngine` is pure Kotlin: answer evaluation, awarding XP, marking tested, node
  mastery (`tested >= required`), unlocking children, delete fallback, import insertion, and
  map-completion percentage.
- Intentional fixes: XP awarded once per video; blank imports rejected; deleting the active
  video falls back gracefully; realm switching resets selection coherently.
- State is in-memory to match prototype reload behavior; persistence is a documented follow-up.

## 6. GitHub Actions

`.github/workflows/android.yml` triggers on push to `main`/`v*` tags, pull requests, and
`workflow_dispatch`, with concurrency cancellation. Steps:

1. `actions/checkout@v4`
2. `actions/setup-java@v4` (Temurin 17)
3. `gradle/actions/setup-gradle@v4` (dependency/build cache)
4. `android-actions/setup-android@v3`, install `platforms;android-36` + `build-tools;35.0.0`
5. `chmod +x gradlew`
6. `./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug`
7. `actions/upload-artifact@v4` → `mindscape-debug-apk` (`app-debug.apk`, error if missing)

The debug APK is debug-signed and installable. Optional signed-release/AAB workflow is a
follow-up if a keystore is supplied.

## 7. Tests & validation

- Unit tests for the engine: XP awarded once, wrong answer doesn't progress, mastery unlocks
  children, progress percentage, import selects the new video, delete fallback, realm switch.
- Static checks: XML parsing, workflow YAML parsing, resource/font reference checks.
- Local build: JDK 17 + Android SDK 36 + Gradle wrapper, then
  `./gradlew testDebugUnitTest lintDebug assembleDebug` until green.
- No emulator run (headless environment); visual behavior is validated by build + code review.

## 8. Risks / notes

- Extended Material icons increase build time; R8 strips unused icons in release.
- CSS backdrop blur is approximated with translucent layers, borders, and shadows.
- Back: dismiss modal first, then return non-Watch panels to Watch.
- Non-goals: real video playback, real AI summarization, accounts/backend, cross-restart persistence.

## 9. Execution order

1. Gradle skeleton, wrapper, manifest, resources, launcher icons.
2. Bundle thumbnails/fonts, theme.
3. Models, seed data, engine, ViewModel.
4. App shell → Learn → Watch → Map → Import dialog.
5. Unit tests, README, GitHub Actions workflow.
6. Local build/test/lint validation and fixes.

**Defaults:** native Compose, debug-APK artifact (no release signing), in-memory state,
English-only, phone-first with a 460 dp cap on larger screens.
