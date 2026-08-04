# OnePieceTCG (Android)

Kotlin + Jetpack Compose Android app (minSdk 26 / compileSdk 35) for logging
a One Piece TCG collection.

## Structure

- `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml` —
  Gradle project setup using a version catalog.
- `app/` — the single application module.
  - `src/main/java/com/chezley/onepiecetcg/` — app sources.
    - `MainActivity.kt` — entry point, hosts the Compose content.
    - `navigation/` — `OnePieceDestination` (tab definitions) and
      `RootScaffold` (bottom navigation + `NavHost`).
    - `ui/screens/` — one placeholder composable per tab.
    - `ui/theme/` — Material 3 theme (color, type, dynamic color support).
    - `data/model/` — plain Kotlin domain models: `Card`, `CardSet`,
      `OwnedCard`, `CardCondition`. Mirror the iOS SwiftData models in
      `../OnePieceTCG/OnePieceTCG/Models/` so both platforms share the same
      shape.
    - `data/db/` — Room persistence: `CardEntity`/`CardSetEntity`/
      `OwnedCardEntity` (the latter has a FK to `CardEntity` with cascade
      delete), `CardDao`/`CardSetDao`/`OwnedCardDao`, an enum
      `Converters`, and `AppDatabase` (singleton `RoomDatabase`, database
      file `one_piece_tcg.db` — data persists across relaunch since this is
      not an in-memory store).
    - `data/repository/` — `CardRepository` interface + `RoomCardRepository`
      impl. UI code depends only on the interface, never on the DAOs
      directly, mirroring `CardRepository` on iOS. `addOwnedCard` throws
      `InvalidQuantityException` for quantity <= 0 instead of creating an
      invalid entry, and `updateOwnedCard` removes the entry when quantity
      drops to <= 0 — see #17 for the iOS bug this deliberately avoids
      repeating.
  - `src/test/` — JVM unit tests, including `RoomCardRepositoryTest`
    (Robolectric + in-memory/file-backed Room DB).

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

A bottom-navigation shell with 4 placeholder tabs: Browse, Collection,
Stats, Settings — mirroring the iOS shell in `../OnePieceTCG/`. App icon is
a minimal placeholder to be refined later. A Room-backed local persistence
layer (`data/`) exists underneath but isn't wired into the screens yet —
that lands with the catalog-seeding and per-screen tickets.

## Known sandbox limitation

Some CI/agent sandboxes block outbound access to `dl.google.com` /
`maven.google.com`, which the Android Gradle Plugin and AndroidX
dependencies resolve through. In that environment `./gradlew` commands
fail during dependency resolution (not from a code problem) and code
changes can only be hand-verified, not compiled or run. GitHub Actions
runners have normal internet access and are the real compile/test gate —
see #24.
