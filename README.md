# One Piece TCG Collection

A mobile app for One Piece Card Game collectors: track what you own, build
and validate decks, browse the full card catalog, and keep an eye on the
market value of your collection.

> **Status:** early development. The iOS app has a SwiftUI navigation shell
> scaffolded in `OnePieceTCG/`, and the Android app has a Jetpack Compose
> navigation shell scaffolded in `android/` (see below); everything else on
> this page is still the product plan guiding what gets built next.

## Platform

Native mobile:

- **iOS** — Swift
- **Android** — Kotlin

## Planned features

- **Collection tracking** — add and remove cards you own, with quantity,
  condition, and foil/parallel (alt-art, manga, etc.) variant tracked
  separately per card.
- **Deck building** — build decks and validate them against official
  deck-building rules (single leader, color restrictions, 50-card deck,
  card-copy limits).
- **Card database & search** — browse and search the full card catalog by
  set, color, card type, cost, power, and ability text.
- **Price tracking** — track market value of individual cards and your whole
  collection over time, sourced from a card-pricing provider.

## Tech stack

| Layer          | Choice                        |
|----------------|--------------------------------|
| iOS            | Swift, SwiftUI                |
| Android        | Kotlin, Jetpack Compose        |
| Card data      | TBD — evaluating card-database and pricing APIs |
| Backend/sync   | TBD                            |

## Getting started

The iOS shell lives in `OnePieceTCG/` — see [`OnePieceTCG/README.md`](OnePieceTCG/README.md)
for build/run/test instructions. The Android shell lives in `android/` — see
[`android/README.md`](android/README.md) for build/run/test instructions.

## Roadmap

1. ~~Scaffold iOS project.~~ ~~Scaffold Android project.~~
2. Integrate a card-database source and seed the local card catalog.
3. Ship collection tracking (v1).
4. Add deck building + deck validation.
5. Add price tracking.
