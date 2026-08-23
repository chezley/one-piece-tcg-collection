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
    - `data/` — persistence layer, mirroring the iOS SwiftData layer in
      `../OnePieceTCG/OnePieceTCG/{Models,Persistence}`:
      - `model/` — plain Kotlin domain models: `Card`, `CardSet`,
        `OwnedCard`, `CardCondition`.
      - `db/` — Room entities (`CardEntity`, `CardSetEntity`,
        `OwnedCardEntity`), DAOs, an enum `Converters`, and `AppDatabase`
        (file-backed singleton via `AppDatabase.getInstance(context)`).
      - `repository/` — `CardRepository` interface + `RoomCardRepository`
        impl. UI code should depend only on the interface, never on Room
        DAOs directly. `addOwnedCard` throws `InvalidQuantityException` on
        quantity ≤ 0; `updateOwnedCard` removes the entry when quantity
        drops to ≤ 0 (see #17 for the iOS bug this avoids repeating).
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
a minimal placeholder to be refined later.

Local persistence (Room) backs the collection data: `Card`/`CardSet`
catalog entities and `OwnedCard` records, behind a `CardRepository`
abstraction the screens depend on. Screens themselves are still separate
tickets (#26-#29).

## Sandbox build/test caveat

This project has repeatedly been developed in sandboxes where outbound
access to `dl.google.com`/`maven.google.com`/Maven Central is blocked by
egress policy, so the Android Gradle Plugin and library dependencies (Room,
Robolectric, etc.) cannot be resolved and `./gradlew assembleDebug`/
`./gradlew test` cannot actually be run there — confirmed again while
building the Room persistence layer (`com.android.application` plugin
resolution fails against Google/MavenRepo/Gradle Central Plugin Repository
alike). Code in such sessions is hand-verified (Room annotations, DAO/
entity signatures, FK wiring, imports, Kotlin syntax) but not compiled.
The GitHub Actions workflow (`#24`) runs on a normal-internet runner and is
the real compile/test gate — fix forward from there if it turns something
up.
