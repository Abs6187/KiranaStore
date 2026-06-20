# Changelog

All notable changes to **Kirana Store Manager** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

All prices in the product are in **Indian Rupees (₹)**.

---

## [1.1.0] – 2026-06-20

### Added – KiranaAiAgent googleAI() backend (Phase 2)
- `ai/GeminiConfig.java` — utility class that creates a secondary `FirebaseApp` named
  `"gemini"` whose API key is the Gemini Developer key (free tier). Resolves the runtime
  Gemini key override from `AppPreferences` first, falling back to
  `BuildConfig.GEMINI_API_KEY`.
- `KiranaAiAgent` now calls `FirebaseAI.getInstance(geminiApp, GenerativeBackend.googleAI())`
  instead of the old `FirebaseAI.getInstance()` path which silently resolved to the paid
  Vertex AI backend. The `Content` object is constructed via `new Content.Builder().addText()`
  (correct API) rather than the deprecated `Content.text()` static factory.
- `SettingsFragment.testGeminiConnection()` wires up the "Test Gemini Connection" button to
  call `KiranaAiAgent` with a ping and report success/failure in colour.

### Added – ScannerFragment robustness (Phase 3)
- **Google Play Services check** on `onViewCreated`: if `isGooglePlayServicesAvailable()`
  ≠ `SUCCESS`, the fragment shows an error message and hides the camera buttons instead
  of crashing or silently failing.
- **Model-download retry**: `MlKitException` code 14 ("model downloading") is caught in
  `handleOcrFailure()`; the scanner stays live and auto-retries every frame, showing a
  `"⏳ Downloading OCR model…"` status. Codes 9/13 (model unavailable) halt scanning
  gracefully.
- **Frame guard**: `isAnalysing` is now `volatile` and a new `AtomicBoolean isProcessingFrame`
  prevents overlapping `TextRecognizer.process()` calls on the CameraX executor thread,
  eliminating data-visibility races and redundant OCR work.
- `textRecognizer` initialisation is wrapped in `try/catch` so a failed `getClient()` call
  shows an error message rather than crashing.

### Added – AppPreferences (Phase 4)
- `util/AppPreferences.java` — singleton `SharedPreferences` wrapper with:
  - `getGeminiKeyOverride()` / `setGeminiKeyOverride(String)` — runtime API key override
    (takes priority over `BuildConfig`).
  - `isVoiceAiEnabled()` / `setVoiceAiEnabled(boolean)` — persisted feature toggle.
  - `isScannerEnabled()` / `setScannerEnabled(boolean)` — persisted feature toggle.
  - `clearAll()` — diagnostics/reset helper.

### Added – Settings screen (Phases 5 & 6)
- `ui/settings/SettingsFragment.java` — full settings screen with four Material 3 card
  sections: **Gemini API Key** (paste/save/clear + masked display), **Features** (voice AI
  and scanner switches), **Diagnostics** (Play Services status + OCR model check), and
  **About** (version, backend, key source).
- `res/layout/fragment_settings.xml` — ScrollView layout with `MaterialCardView` sections,
  `TextInputLayout`, `SwitchMaterial`, and `MaterialButton` components.
- `res/drawable/ic_settings.xml` — Material gear vector icon (24 dp), tinted with
  `?attr/colorControlNormal`.
- `res/navigation/nav_graph.xml` — `navigation_settings` destination added.
- `ui/MainActivity.java` — `destination_settings` hidden from bottom nav (bottom bar and
  voice FAB are hidden when on scanner or settings screens).
- `ui/dashboard/DashboardFragment.java` — `btn_settings` click navigates to
  `R.id.navigation_settings` via `Navigation.findNavController`.
- `res/values/strings.xml` — added `title_settings`, `cd_settings`, and full set of
  `settings_*` strings for all labels, buttons, and hints.

### Changed – Documentation (Phase 7)
- `README.md` — updated feature table (Settings screen row), Quick Start §4 (mentions
  runtime key override via Settings), and project structure tree (shows `ai/GeminiConfig`,
  `util/AppPreferences`, `ui/settings/`).
- `CHANGELOG.md` (this file) — full record of all Phase 2–7 additions.

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
