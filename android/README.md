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
    - `ui/screens/` — one composable per tab (`StatsScreen`/`StatsViewModel`
      implemented; Browse/Collection/Settings still placeholders).
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
  - `src/test/` — JVM unit tests: `RoomCardRepositoryTest` and
    `StatsViewModelTest` (Robolectric + in-memory Room DB).

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

A bottom-navigation shell with 4 tabs: Browse, Collection, Stats, Settings —
mirroring the iOS shell in `../OnePieceTCG/`. App icon is a minimal
placeholder to be refined later. Browse, Collection, and Settings are still
placeholders (separate tickets: #26/#28/#29, #23).

Local persistence (Room) backs the collection data: `Card`/`CardSet`
catalog entities and `OwnedCard` records, behind a `CardRepository`
abstraction the screens depend on.

**Stats** (`ui/screens/StatsScreen.kt` + `StatsViewModel`) is implemented:
total copies owned, total unique cards owned, and a per-set completion
percentage (owned unique cards / total cards in that set, keyed by
`Card.setCode` — `CardRepository` doesn't expose `CardSet` metadata like
display names yet, so sets are shown by code). An empty state covers zero
owned cards. The screen recomputes via `StatsViewModel.refresh()` every
time it re-enters composition (e.g. switching back to the tab), which is
how it reflects changes made on other screens without an app relaunch.

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
