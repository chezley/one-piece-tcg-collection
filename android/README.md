# OnePieceTCG (Android)

Kotlin + Jetpack Compose Android app (minSdk 26 / compileSdk 35) for logging
a One Piece TCG collection. This ticket only scaffolds the project shell —
no data/persistence yet.

## Structure

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml` —
  Gradle project setup using a version catalog.
- `app/` — the single application module.
  - `src/main/java/com/chezley/onepiecetcg/` — app sources.
    - `MainActivity.kt` — entry point, hosts the Compose content.
    - `navigation/` — `OnePieceDestination` (tab definitions) and
      `RootScaffold` (bottom navigation + `NavHost`).
    - `ui/screens/` — `SettingsScreen` is real (version, About, Open Source
      Licenses); Browse/Collection/Stats are still placeholders pending
      #22/#25/#26's data layer.
    - `ui/theme/` — Material 3 theme (color, type, dynamic color support).
  - `src/test/` — JVM unit tests.

## Build & run

```bash
cd android
./gradlew assembleDebug
```

Open the `android/` directory in Android Studio to run on a device or
emulator directly.

## Test

```bash
cd android
./gradlew test
```

## What's here

A bottom-navigation shell with 4 tabs, mirroring the iOS shell in
`../OnePieceTCG/`: Browse, Collection, and Stats are still placeholders
(they need the Room data layer from #22/#25/#26); Settings is a real,
data-driven list (app version read from `PackageInfo`, About and Open
Source Licenses dialogs) with no persistence dependency, so future prefs
like price-tracking currency or theme can be added as new list entries
without restructuring the screen. App icon is a minimal placeholder to be
refined later.
