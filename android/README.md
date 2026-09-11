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
    - `ui/screens/` — one placeholder composable per bottom-nav tab (Browse,
      Collection, Stats, Settings), plus the real `CardDetailScreen` +
      `CardDetailViewModel` (#26): shows a card's image placeholder, name,
      set, card number, rarity, cost, power, attribute and type, with an
      "Owned" switch and a quantity stepper backed directly by
      `CardRepository` so every change persists immediately (survives
      navigating away and app relaunch, since it's writing through Room).
      Reached via the `cardDetail/{cardId}` route (`navigation/
      CardDetailRoute.kt`) — not a bottom-nav tab itself, so it isn't wired
      into Browse/Collection yet; that lands with #28/#29 when those tabs
      have real rows to tap.
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
      repeating. `fetchCard(id)` / `fetchOwnedCard(cardId)` (added for #26)
      look up a single card and its owned-card entry for the detail screen.
    - `data/catalog/` — `CatalogLoader`, which discovers every catalog-set
      JSON file under `assets/catalog/` and seeds the `cards`/`card_sets`
      tables from them. Idempotent per set (matches existing cards by `id`,
      so relaunching never duplicates data); a set file that fails to load
      or parse is logged and skipped rather than blocking the others or
      crashing. Mirrors the iOS `CatalogLoader`
      (`../OnePieceTCG/OnePieceTCG/Catalog/CatalogLoader.swift`). Wired up in
      `MainActivity.onCreate` via `lifecycleScope`, so the catalog seeds once
      on first launch.
  - `src/main/assets/catalog/OP01.json` — the OP-01 "Romance Dawn" card
    catalog (121 cards), reused byte-for-byte from the iOS dataset at
    `../OnePieceTCG/OnePieceTCG/Catalog/OP01.json` (#4) rather than
    re-sourced. Note: a handful of card names in this file have a known
    formatting bug (periods instead of spaces, e.g. "Monkey.D.Luffy") —
    tracked separately in #20 and intentionally left as-is here so both
    platforms stay byte-for-byte identical until that ticket fixes it in one
    place.
  - `src/test/` — JVM unit tests, including `RoomCardRepositoryTest`
    (Robolectric + in-memory/file-backed Room DB), `CatalogLoaderTest`
    (Robolectric, so `assets/` and `org.json` behave as they would
    on-device: dataset parsing, card count, known-card field spot-checks,
    malformed/unknown-resource error paths, and idempotent seeding), and
    `CardDetailViewModelTest` (Robolectric + in-memory Room DB +
    `UnconfinedTestDispatcher` on `Dispatchers.Main`): loading an
    owned/unowned card, toggling owned on/off, incrementing/decrementing
    quantity (setting the exact value rather than accumulating), and
    quantity never going negative.

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

A bottom-navigation shell with 4 tabs: Browse, Collection, Stats, Settings
— mirroring the iOS shell in `../OnePieceTCG/`. App icon is a minimal
placeholder to be refined later. A Room-backed local persistence layer
(`data/`) exists underneath and seeds itself from the bundled OP-01 catalog
on first launch. Browse/Collection/Stats/Settings are still placeholders
(#23, #27–#29 remaining); the card detail screen (#26) is real and reachable via
the `cardDetail/{cardId}` route, wired to persistence end-to-end — Browse
and Collection just don't navigate to it yet.

## Note on this layer's history

The Room persistence layer, catalog loader, and card detail screen
documented above were implemented correctly at least five times across
prior sessions before actually landing on `main` — each earlier attempt
was real, complete work that sat on an orphaned branch and was never
merged (see the comment history on #22/#25/#26 for the full account).
PR #45 finally merged the most recent, verified version of the stack. If
a future scheduled review reports these features as "missing" again,
check whether a fix genuinely regressed versus whether it just needs
merging — this repo has hit the latter far more often than the former.

While reviewing PR #45 before merge, found that `RoomCardRepository
.addOwnedCard` matches an existing owned-card row by `cardId` alone,
ignoring `condition` — the same bug class as #38 on iOS (still unfixed
there as of this writing). Left unfixed here since it's outside #22's own
scope and not yet reachable from any Android UI path; tracked as #46.

## Known sandbox limitation

Some CI/agent sandboxes block outbound access to `dl.google.com` /
`maven.google.com`, which the Android Gradle Plugin and AndroidX
dependencies resolve through. In that environment `./gradlew` commands
fail during dependency resolution (not from a code problem) and code
changes can only be hand-verified, not compiled or run. GitHub Actions
runners have normal internet access and are the real compile/test gate —
see #24.
