# One Piece TCG — Expo preview

A React Native build of the collection app that runs inside **Expo Go**, so
you can try the UI live on a real iPhone without Xcode, a Mac, or a
provisioning profile.

This is a **testing surface, not the product.** The shipping apps stay native
(Swift in `OnePieceTCG/`, Kotlin in `android/`). This preview shares their card
catalog so what you see on the phone matches what the native apps load.

## Run it on your iPhone

1. **On the phone:** install **Expo Go**. This project targets **Expo SDK 54**,
   so you need an SDK 54 build of Expo Go — if the App Store version has moved
   past it, grab the matching build from the
   [Expo Go archive](https://expo.dev/go?sdkVersion=54&platform=ios&device=true).
   It runs on iPhone 17 Pro Max on the current public iOS release.
2. **On your computer** (Node 18+ installed), from the repo root:

   ```sh
   cd expo-preview
   npm install
   npm start
   ```

3. A QR code appears in the terminal. Open the **Camera** app on the iPhone,
   point it at the QR code, and tap the banner — it opens the app in Expo Go.

Phone and computer must be on the same Wi-Fi. If your network blocks
device-to-device traffic (common on guest, corporate, and university Wi-Fi),
use a relay instead:

```sh
npm run tunnel
```

That routes through Expo's servers and works on any connection, just slower to
load.

### While it's running

- Edits to the source reload on the phone within a second or two.
- **Shake the phone** (or press `m` in the terminal) for the dev menu.
- Press `r` in the terminal to force a reload, `j` to open the debugger.

## What you can do in the app

- **Browse** — all 121 cards from OP-01 Romance Dawn with images, search by
  name / card number / attribute, and filter by card type or rarity. Cards you
  own are outlined in red with a copy count.
- **Card detail** — tap any card for the full image, cost, power, attribute,
  and rarity, then set how many copies you own and their condition.
- **Collection** — everything you own, with quick +/- steppers and delete.
- **Stats** — unique cards, total copies, and completion bars broken out by
  set, rarity, and card type.
- **Settings** — what's loaded, device info, and a reset button.

Your collection is saved on the device (AsyncStorage) and survives app
restarts. It is separate from whatever the native apps store — this preview
does not sync with them. Light and dark mode both follow the iPhone's system
setting.

## Keeping the catalog in sync

`src/data/OP01.json` is a copy of the iOS app's catalog. After changing the
card data in `OnePieceTCG/OnePieceTCG/Catalog/`, re-copy it:

```sh
npm run sync-catalog
```

## Checks

```sh
npm run typecheck     # TypeScript
npx expo export --platform ios   # confirms the app bundles
```

## Versions

Expo SDK 54 / React Native 0.81 / React 19.1.

The App Store build of Expo Go tracks the newest SDK, so an SDK 54 project may
need an **SDK 54 build of Expo Go** rather than whatever is currently on the
store. If the app fails to open with a message about an unsupported SDK
version, install the matching build from the
[Expo Go archive](https://expo.dev/go?sdkVersion=54&platform=ios&device=true).
Keep this project's SDK and the phone's Expo Go on the same major version.
