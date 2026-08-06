# Changelog

All notable changes to **Kirana Store Manager** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

All prices in the product are in **Indian Rupees (₹)**.

---

## [1.2.0] – 2026-08-06

### Added – Web PWA Companion App & Firestore Sync
- **Vite + React PWA (`KiranaStore/web`)**: Fully installable Progressive Web App with offline service worker support (`vite-plugin-pwa` + Workbox caching).
- **Firestore Native Realtime Cloud Database (`kirana-store-abs6187` in `asia-south1`)**: Real-time catalogue synchronization across Android native app and Web PWA.
- **Gemini 2.0 Flash Voice AI (Web)**: Web Speech API STT (`hi-IN` / `en-IN`) integrated with Gemini 2.0 Flash REST API and Levenshtein fuzzy product matching.
- **SpeechSynthesis TTS Readback**: Audio readback confirmation for voice commands on browser clients.
- **Price Audit Log Modal**: Immutable price history audit trail viewer in PWA.
- **Firebase Hosting**: Production PWA deployed to [https://kirana-store-abs6187.web.app](https://kirana-store-abs6187.web.app).

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
