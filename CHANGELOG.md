# Changelog

All notable changes to **Kirana Store Manager** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

All prices in the product are in **Indian Rupees (₹)**.

---

## [1.0.0] – 2026-06-18

First stable release. A modern reimagining of `vincentbecker/Foodventory` (2018) as a
voice-first, offline-capable price manager for Indian kirana (grocery) stores.

### Added
- **Dashboard screen** – 2-column product grid with live search, pinned items, and a
  bottom-sheet quick-add dialog. The shopkeeper's primary screen.
- **Prices screen** – linear list of every product with inline price editing.
- **Scanner screen** – real-time receipt / price-tag scanner powered by CameraX 1.4.2
  and ML Kit Text Recognition v2 (on-device Latin + Devanagari OCR). Detects ₹ / Rs. /
  "rupees" price patterns and applies them in bulk.
- **History screen** – last-50 price-change audit feed, tagged by source
  (`🎤 Voice`, `📷 OCR Scan`, `✏️ Manual`).
- **Voice AI** – `KiranaAiAgent` parses Hindi/Hinglish/English voice commands into
  structured JSON (`update_price` / `add_product` / `query_price` / `unknown`) using
  **Gemini 2.0 Flash** via the Firebase AI Logic SDK, with TTS readback.
- **Voice interface** – `VoiceManager` wraps native `SpeechRecognizer` (hi-IN + en-IN
  fallback) and `TextToSpeech` (Indian English). Zero cloud cost.
- **Persistence** – Room 2.8.4 with `products` and append-only `price_history` tables
  (`ON DELETE CASCADE`, `productId` index). `ProductRepository.updatePrice()` is the
  canonical price-change path for all three input sources.
- **Fuzzy matching** – `FuzzyMatcher` utility (substring-then-Levenshtein, threshold ≤5)
  resolves spoken/OCR'd product names against the catalog.
- **Navigation** – Single-Activity architecture with Jetpack Navigation Component,
  BottomNavigationView, and a global floating voice button.
- **Theming** – Material 3 with a saffron brand palette, day/night themes, ₹ currency
  symbol throughout.
- **Firebase integration** – Firestore rules (open-read products, auth-gated writes,
  immutable `price_history`), Analytics, and Firebase AI Logic.
- **Convention plugins** – `build-logic/` composite build providing
  `kirana.android.application` and `kirana.android.room` for centralised build config.
- **Test suite**
  - Unit tests (JVM): `Product`, `PriceHistory`, `DateConverter`, `FuzzyMatcher`,
    `KiranaAiAgent` JSON parsing, `VoiceManager` error mapping.
  - Instrumented tests (device/emulator): `ProductDao`, `PriceHistoryDao` (incl.
    cascade delete), and an Espresso test for the Dashboard add-product flow.
- **Tooling** – gcloud/Firebase bootstrap scripts (`scripts/setup_firebase.{sh,bat}`),
  `local.defaults.properties` API-key template, and this `CHANGELOG.md`.

### Changed
- Migrated the entire legacy stack to AndroidX (Jetifier disabled).
- `compileSdk`/`targetSdk` 28 → **34**, `minSdk` 24 → **26**, Java **17**.
- All dependency versions centralised in `gradle/libs.versions.toml`; no hardcoded
  versions remain in any `build.gradle`.
- Legacy Camera API → **CameraX 1.4.2**.
- `android.arch.persistence.room:1.1.1` → **androidx.room:2.8.4**.
- `com.android.support:appcompat-v7:28` → **androidx.appcompat:1.7.0**.
- Gradle 4.x → **Gradle 8.6** with configuration cache, parallel builds, and
  non-transitive R classes.
- `firebase-ml-vision:18.0.1` → **ML Kit `text-recognition:17.3.0`** (standalone,
  on-device, no Firebase project required for OCR).

### Removed
- `com.google.ai.client.generativeai:0.9.0` – reached end-of-life **2025-11-30**;
  replaced by `firebase-ai` (Firebase AI Logic), BOM-managed.
- `firebase-vertexai` – deprecated; replaced by `firebase-ai`.
- `firebase-ml-vision` – deprecated 2018 Firebase vision API; replaced by standalone ML Kit.

### Notes – Zero Cost Architecture
Every runtime dependency runs on a free tier: Room and ML Kit are on-device (₹0),
Android STT/TTS is native (₹0), Gemini 2.0 Flash uses the Firebase AI Logic free tier,
and Firestore sits on the Spark plan. The app is fully functional offline; Firebase is
optional cloud backup/sync.

---

[1.0.0]: https://semver.org/spec/v2.0.0.html
