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
    - `data/model/` — plain Kotlin domain models used by UI/repository code:
      `Card`, `CardSet`, `OwnedCard`, `CardCondition`.
    - `data/db/` — Room persistence: `CardEntity`/`CardSetEntity`/
      `OwnedCardEntity`, their DAOs, `Converters` (enum type converter), and
      `AppDatabase` (the `RoomDatabase` singleton). Mirrors the SwiftData
      schema on iOS (`OnePieceTCG/OnePieceTCG/Models`,
      `OnePieceTCG/OnePieceTCG/Persistence`) so both platforms share the
      same shape.
    - `data/repository/` — `CardRepository` interface + `RoomCardRepository`
      impl. UI code should depend on `CardRepository` only, never on Room
      DAOs directly, so the persistence framework can be swapped later
      without touching screens. `addOwnedCard` rejects zero/negative
      quantities and `updateOwnedCard` removes the entry when quantity
      drops to zero or below (see iOS issue #17, which this intentionally
      avoids repeating).
    - `data/catalog/` — `CatalogLoader`, which discovers every catalog-set
      JSON file bundled under `assets/catalog/` and seeds the `cards`/
      `card_sets` Room tables from them. Mirrors the iOS `CatalogLoader`
      (`OnePieceTCG/OnePieceTCG/Catalog/CatalogLoader.swift`): seeding is
      idempotent per set (existing card `id`s are left untouched, so
      relaunching or adding a new set file only inserts what's missing), and
      a set file that fails to load/parse is logged and skipped rather than
      blocking the others or crashing. Uses `org.json` (already on the
      Android platform classpath — no extra dependency) rather than a
      serialization library, to keep this in step with the plain-JSON
      approach on iOS. `MainActivity.onCreate` calls
      `CatalogLoader.seedCatalog` once via `lifecycleScope` on launch.
  - `src/main/assets/catalog/` — bundled catalog-set JSON, currently
    `OP01.json` (121 cards), reused byte-for-byte from the iOS catalog
    (`OnePieceTCG/OnePieceTCG/Catalog/OP01.json`, #4) rather than
    re-sourced, so both platforms ship identical card data.
  - `src/test/` — JVM unit tests: `RoomCardRepositoryTest`
    (Robolectric-backed, in-memory Room DB) covering add/increment/update/
    remove and fetch-by-set; `CatalogLoaderTest` (also Robolectric, so
    `assets/` and `org.json` resolve exactly as they do on-device) covering
    parsing, known-card fields, malformed input, and idempotent seeding
    (a second `seedCatalog`/`seedDataset` run inserts 0 new cards).

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
a minimal placeholder to be refined later. Data models and a Room-backed
persistence/repository layer exist under `data/`, and the bundled OP-01
catalog now seeds into it automatically on first launch via
`CatalogLoader`; screens do not yet read from or write to the repository
(wiring that up is separate, per-screen ticket work).

## Note on this sandbox's build verification

As with the initial scaffold (#21) and the Room persistence layer (#22),
`./gradlew test`/`assembleDebug` could not be executed in this
environment — outbound access to `dl.google.com`/`maven.google.com`
(needed to resolve the Android Gradle Plugin) is blocked by egress policy
here, confirmed via a direct `curl` (403). The Room/KSP/Robolectric wiring,
the persistence code in `data/`, and the new `CatalogLoader` +
`CatalogLoaderTest` were hand-verified (types, Room annotations, DAO
signatures, JSON parsing logic, imports) but never actually compiled in
this sandbox. The CI workflow from #24 (which runs on a GitHub-hosted
runner with normal internet access) is the first real compile/test gate
for this code — fix forward from there if it turns up an issue.
