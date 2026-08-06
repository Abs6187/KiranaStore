# 🛒 Kirana Store Manager

> **Modernised from** [`vincentbecker/Foodventory`](https://github.com/vincentbecker/Foodventory) (2018)  
> **Pivoted to**: Price-centric Kirana (Indian grocery) store management with Voice AI + OCR  
> **🌐 Web PWA Companion App**: [https://kirana-store-abs6187.web.app](https://kirana-store-abs6187.web.app)  
> **📦 Latest Release**: [v1.2.0 GitHub Release](https://github.com/Abs6187/KiranaStore/releases/tag/v1.2.0)

---

## ✨ Features

| Feature | Tech | Notes |
|---|---|---|
| **🌐 Web PWA Companion** | Vite + React 18 + PWA | Installable web app synced live with Android app |
| **⚡ Firestore Realtime Sync** | Firebase Cloud Firestore | Multi-device live catalogue & price sync |
| **Price Dashboard** | Room + LiveData / React | Grid of products with ₹ prices, pinning, inline edit |
| **Voice Commands** | Android STT / Web Speech + Gemini 2.0 Flash | "Mustard oil 175 rupees karo" → auto-updates price |
| **AI NLP** | Gemini 2.0 Flash API / Firebase AI Logic | Parses Hindi/Hinglish/English → JSON command |
| **TTS Feedback** | Android / Web SpeechSynthesis | Reads back confirmation aloud |
| **OCR Receipt Scan** | CameraX + ML Kit Text Recognition v2 | Fully on-device, no cloud, ₹ price regex extraction |
| **Price Audit Logs** | Room / Firestore PriceHistory | Auto-timestamped history (manual/voice/ocr) |
| **Settings screen** | SharedPreferences / LocalStorage | Runtime Gemini key override, Voice AI/Scanner toggles, diagnostics |
| **Dark Mode** | Material Design 3 / Glassmorphic UI | System auto-switch light/dark theme |

---

## 🔧 Legacy → Modern Dependency Migration

| Old (Foodverty 2018) | New (Kirana Store 2026) |
|---|---|
| `compileSdkVersion 28` | `compileSdk 34` |
| `com.android.support:*:28` | `androidx.*` (Jetifier disabled) |
| `android.arch.persistence.room:1.1.1` | `androidx.room:2.8.4` |
| `com.google.firebase:firebase-ml-vision:18.0.1` ❌ deprecated | `com.google.mlkit:text-recognition:17.3.0` ✅ |
| Legacy Camera API | CameraX 1.4.2 |
| No AI | Gemini 2.0 Flash via **Firebase AI Logic** (`firebase-ai`) & REST API |
| No voice | Native STT/TTS (free, on-device & Web Speech) |
| `gradle-wrapper` → Gradle 4.x | Gradle 8.6 + `libs.versions.toml` + convention plugins |
| Native Only | **Android Native + Web PWA Companion** |

> ⚠️ The legacy `com.google.ai.client.generativeai:0.9.0` SDK reached **end-of-life on 2025-11-30** and has been removed. `firebase-vertexai` (also deprecated) was replaced by `firebase-ai`. Both are now managed by Firebase BOM 34.15.0.

---

## 🚀 Quick Start

### 1. Web PWA Live URL
Access the live Web PWA immediately without installation:
👉 **[https://kirana-store-abs6187.web.app](https://kirana-store-abs6187.web.app)**

### 2. Prerequisites & Firebase Setup

```bash
# Install gcloud CLI
# https://cloud.google.com/sdk/docs/install

# Install Firebase CLI
npm install -g firebase-tools
```

### 3. Firebase + gcloud Setup (Windows)

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
2. Create/set your GCP project (`kirana-store-abs6187`)
3. Enable `firebase.googleapis.com`, `firestore.googleapis.com`
4. `firebase login` + link project
5. Deploy Firestore security rules
6. Deploy Web PWA to Firebase Hosting

### 4. Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create a **free-tier** Gemini API key
3. Create `local.properties` in project root:
   ```properties
   GEMINI_API_KEY=AIzaSy...your_key_here
   ```
   > ⚠️ `local.properties` is in `.gitignore` – **never commit your key**

   **Alternative**: launch the app (or Web PWA Settings modal), tap the ⚙️ gear icon, and paste the key in **Settings → Gemini API Key**.

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
├── app/                          # Native Android App
│   ├── src/main/java/com/kirana/store/
│   │   ├── ai/KiranaAiAgent.java # Gemini 2.0 Flash NLP
│   │   ├── voice/VoiceManager.java
│   │   ├── data/                 # Room DB entities, DAOs, Repository
│   │   └── ui/                   # Dashboard, Prices, Scanner, History, Settings
├── web/                          # Web PWA Companion App
│   ├── src/
│   │   ├── components/           # Navbar, ProductCard, Modals, VoiceFab
│   │   ├── services/             # Firestore, Gemini AI, Web Speech STT/TTS
│   │   ├── firebase.js           # Firestore init & persistence
│   │   └── App.jsx               # PWA React Application
│   ├── vite.config.js            # Vite + Workbox PWA builder
│   └── package.json
├── firestore.rules               # Firestore security rules
├── firebase.json                 # Firebase CLI & Hosting config
├── scripts/
│   ├── deploy_web.bat            # Windows PWA build & deploy
│   └── deploy_web.sh             # Linux/Mac PWA build & deploy
├── CHANGELOG.md                  # Release history
└── README.md
```

---

## 🆓 Zero Cost Architecture

All core features run completely free:
- **Database**: Room & Cloud Firestore Free Tier – ₹0
- **OCR**: ML Kit on-device text recognition – ₹0
- **STT/TTS**: Native Android & Web Speech API – ₹0
- **AI**: Gemini 2.0 Flash (free tier: 15 requests/min) – ₹0
- **Hosting**: Firebase Hosting (Spark plan) – ₹0

---

## 📜 License

Built upon [Foodventory](https://github.com/vincentbecker/Foodventory) by vincentbecker.  
Modernised and pivoted to Kirana Store Manager.
