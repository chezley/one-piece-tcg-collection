# OnePieceTCG

SwiftUI iOS app (iOS 17+) for logging a One Piece TCG collection.

## Structure

- `project.yml` — [XcodeGen](https://github.com/yonaskolb/XcodeGen) spec; the
  source of truth for the project. `OnePieceTCG.xcodeproj` is generated from
  it and committed so the project builds without XcodeGen installed.
- `OnePieceTCG/` — app sources:
  - `App/` — app entry point, attaches the SwiftData `ModelContainer`.
  - `Views/` — tab screens.
  - `Models/` — SwiftData models: `Card`, `CardSet`, `OwnedCard`, `CardCondition`.
  - `Persistence/` — `PersistenceController` (builds the `ModelContainer`)
    and `CardRepository` (the API views use to read/write owned cards —
    never touch SwiftData directly from a view).
  - `Catalog/` — `OP01.json` (bundled OP-01 "Romance Dawn" card data, 121
    cards) and `CatalogLoader`. The loader discovers every bundled
    catalog-set `.json` file (not just OP01) and seeds each one
    idempotently on first launch, so a future set just needs its JSON file
    added to `Catalog/` — no loader changes. A set file that fails to
    parse is logged and skipped rather than blocking the others.
  - `Assets.xcassets`.
- `OnePieceTCGTests/` — unit test target.

If you change `project.yml`, regenerate the project:

```bash
brew install xcodegen   # once
cd OnePieceTCG
xcodegen generate
```

## Build & run

```bash
open OnePieceTCG.xcodeproj   # then Cmd+R in Xcode, or:
xcodebuild -project OnePieceTCG.xcodeproj -scheme OnePieceTCG \
  -destination 'platform=iOS Simulator,name=iPhone 17' build
```

## Test

```bash
xcodebuild -project OnePieceTCG.xcodeproj -scheme OnePieceTCG \
  -destination 'platform=iOS Simulator,name=iPhone 17' test
```

## Validate the catalog

`Scripts/validate_catalog.py` is a standalone script (Python 3, no
Xcode/Swift toolchain needed) that checks the bundled catalog data in
`OnePieceTCG/Catalog/*.json` for the same integrity rules `CatalogLoader`
assumes: every set's card count matches the documented official count, every
card has a non-empty `name`/`rarity`/`cardNumber`, and there are no duplicate
`(setCode, cardNumber)` pairs across the whole catalog. Failures name the
offending set file and card rather than a generic assertion.

Run it locally (or in CI) before shipping a catalog change:

```bash
python3 OnePieceTCG/Scripts/validate_catalog.py
```

Optionally pass a different catalog directory (e.g. to check a scratch copy)
as the first argument. When a new set is bundled, add its official card
count to `OFFICIAL_SET_COUNTS` in the script -- a set missing from that dict
fails the check loudly instead of being skipped silently.

## What's here

A tab-based navigation shell with 4 placeholder tabs: Browse, Collection,
Stats, Settings. App icon and launch screen are minimal placeholders to be
refined later.
