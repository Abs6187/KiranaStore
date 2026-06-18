# 🛒 Kirana Store Manager

> **Modernised from** [`vincentbecker/Foodventory`](https://github.com/vincentbecker/Foodventory) (2018)
> **Pivoted to**: Price-centric Kirana (Indian grocery) store management with Voice AI + OCR

---

## ✨ Features

| Feature | Tech | Notes |
|---|---|---|
| **Price Dashboard** | Room + LiveData | Grid of products with ₹ prices, pinning, inline edit |
| **Voice Commands** | Android STT (hi-IN) + Gemini 2.0 Flash | "Mustard oil 175 rupees karo" → auto-updates price |
| **AI NLP** | Gemini 2.0 Flash API | Parses Hindi/Hinglish/English → JSON command |
| **TTS Feedback** | Android TextToSpeech (en-IN) | Reads back confirmation aloud |
| **OCR Receipt Scan** | CameraX + ML Kit Text Recognition v2 | Fully on-device, no cloud, ₹ price regex extraction |
| **Price History** | Room PriceHistory table | Auto-timestamped every change (manual/voice/ocr) |
| **Dark Mode** | Material Design 3 | System auto-switch light/dark theme |
| **Firestore Sync** | Firebase free-tier | Optional cloud backup of product catalogue |

---

## 🔧 Legacy → Modern Dependency Migration

| Old (Foodverty 2018) | New (Kirana Store 2026) |
|---|---|
| `compileSdkVersion 28` | `compileSdk 34` |
| `com.android.support:*:28` | `androidx.*` (Jetifier disabled) |
| `android.arch.persistence.room:1.1.1` | `androidx.room:2.8.4` |
| `com.google.firebase:firebase-ml-vision:18.0.1` ❌ deprecated | `com.google.mlkit:text-recognition:17.3.0` ✅ |
| Legacy Camera API | CameraX 1.4.2 |
| No AI | Gemini 2.0 Flash via **Firebase AI Logic** (`firebase-ai`) |
| No voice | Android native STT/TTS (free, on-device) |
| `gradle-wrapper` → Gradle 4.x | Gradle 8.6 + `libs.versions.toml` + convention plugins |

> ⚠️ The legacy `com.google.ai.client.generativeai:0.9.0` SDK reached **end-of-life on 2025-11-30** and has been removed. `firebase-vertexai` (also deprecated) was replaced by `firebase-ai`. Both are now managed by Firebase BOM 34.15.0.

---

## 🚀 Quick Start

### 1. Prerequisites

```bash
# Install gcloud CLI
# https://cloud.google.com/sdk/docs/install

# Install Firebase CLI
npm install -g firebase-tools
```

### 2. Firebase + gcloud Setup (Windows)

```bat
cd KiranaStore
scripts\setup_firebase.bat
```

Or on Mac/Linux:
```bash
bash scripts/setup_firebase.sh
```

**This script will:**
1. `gcloud auth login` – authenticate
2. Create/set your GCP project
3. Enable `firebase.googleapis.com`, `firestore.googleapis.com`
4. `firebase login` + link project
5. Deploy Firestore security rules
6. Guide you to download `google-services.json`

### 3. Manual gcloud Commands

```bash
# Authenticate
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# Enable APIs
gcloud services enable firebase.googleapis.com \
  firestore.googleapis.com \
  firebaseappcheck.googleapis.com

# Firebase init + deploy rules
firebase login
firebase use YOUR_PROJECT_ID
firebase deploy --only firestore:rules
```

### 4. Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create a **free-tier** Gemini API key
3. Create `local.properties` in project root:
   ```properties
   GEMINI_API_KEY=AIzaSy...your_key_here
   ```
   > ⚠️ `local.properties` is in `.gitignore` – **never commit your key**

### 5. Add google-services.json

1. [Firebase Console](https://console.firebase.google.com) → Your project → Project Settings
2. Add Android app with package: `com.kirana.store`
3. Download `google-services.json` → place in `app/`

### 6. Open in Android Studio

- Open folder `KiranaStore/` in Android Studio Hedgehog or later
- Click **Sync Project with Gradle Files**
- Build → Run on device (API 26+)

---

## 🧪 Testing

The project ships with a JVM unit-test suite and an instrumented (device/emulator) suite.
All assertions use ₹ prices and Indian product names.

### Unit tests (no device, no Firebase required)

Pure-logic tests that run on the JVM in seconds:

```bash
./gradlew :app:testDebugUnitTest
```

Covers: `Product` / `PriceHistory` entity defaults, `DateConverter` round-trips,
`FuzzyMatcher` (substring + Levenshtein matching), `KiranaAiAgent` JSON parsing
(update_price / add_product / query_price / unknown, markdown-fence stripping), and
`VoiceManager` STT error-code mapping.

### Instrumented tests (device or emulator required)

Run against an in-memory Room DB — **no `google-services.json` needed**:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Covers: `ProductDao` insert/update/delete, `PriceHistoryDao` latest-entry + `ON DELETE
CASCADE`, and an Espresso test of the Dashboard add-product → ₹175 flow.

---

## 🎤 Voice Command Examples

| Say (Hindi/Hinglish/English) | Action |
|---|---|
| "Mustard oil 175 rupees karo" | Update Mustard Oil price to ₹175 |
| "Basmati rice add karo 280 rupees" | Add Basmati Rice at ₹280 |
| "Atta price kitna hai?" | Query Atta price |
| "Change mustard oil to 175 rupees" | English command works too |

---

## 📁 Project Structure

```
KiranaStore/
├── app/
│   ├── build.gradle              # Convention plugins + catalog aliases only
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/java/com/kirana/store/
│       │   ├── KiranaApp.java           # Application class
│       │   ├── ai/KiranaAiAgent.java    # Gemini 2.0 Flash NLP
│       │   ├── voice/VoiceManager.java  # STT (hi-IN) + TTS
│       │   ├── util/FuzzyMatcher.java   # Levenshtein product-name matching
│       │   ├── data/
│       │   │   ├── model/               # Product, PriceHistory
│       │   │   ├── db/                  # Room DAOs, KiranaDatabase
│       │   │   └── repository/          # ProductRepository
│       │   └── ui/
│       │       ├── MainActivity.java    # Nav + Voice FAB
│       │       ├── dashboard/           # Price grid
│       │       ├── prices/              # Full list + inline edit
│       │       ├── scanner/             # CameraX + ML Kit OCR
│       │       └── history/             # Timestamped price log
│       ├── main/res/                    # Layouts, drawables, themes
│       ├── test/                        # JVM unit tests
│       └── androidTest/                 # Instrumented (device) tests
├── build-logic/                  # Composite build – convention plugins
│   └── convention/src/main/groovy/
│       ├── kirana.android.application.gradle
│       └── kirana.android.room.gradle
├── gradle/libs.versions.toml     # Centralised version catalog
├── firestore.rules               # Firestore security rules
├── firebase.json                 # Firebase CLI config
├── scripts/
│   ├── setup_firebase.sh         # Linux/Mac setup
│   └── setup_firebase.bat        # Windows setup
├── build.gradle                  # Root Gradle (plugin declarations only)
├── settings.gradle               # includeBuild("build-logic")
├── CHANGELOG.md                  # Release history
└── local.defaults.properties     # API key template
```

---

## 🆓 Zero Cost Architecture

All core features run completely free:
- **Database**: Room (on-device SQLite) – ₹0
- **OCR**: ML Kit on-device text recognition – ₹0
- **STT**: Android native SpeechRecognizer – ₹0
- **TTS**: Android TextToSpeech – ₹0
- **AI**: Gemini 2.0 Flash (free tier: 15 requests/min, 1M tokens/day) – ₹0
- **Firebase**: Spark (free) plan – ₹0

---

## 📜 License

Built upon [Foodventory](https://github.com/vincentbecker/Foodventory) by vincentbecker.
Modernised and pivoted to Kirana Store Manager.
