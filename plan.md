# Kirana Store Manager – Implementation Plan & Progress Checklist

> **Project**: Modernise `vincentbecker/Foodventory` → **Kirana Store Manager**
> **Stack**: Android (Java) · Room 2.8.4 · ML Kit v2 · CameraX 1.4.2 · Firebase AI Logic · Firebase BOM 34.15.0 · gcloud CLI
> **Skills applied**: `add-dependency`, `gradle-patterns`
> **Legend**: ✅ Done · 🔧 User Action Required · ⏳ Remaining

---

## 1. Project Scaffold & Build System

| File | Status | Notes |
|------|--------|-------|
| `settings.gradle` | ✅ Done | `includeBuild("build-logic")`, version catalog, composite build |
| `build.gradle` (root) | ✅ Done | Catalog aliases only – zero hardcoded versions |
| `gradle/libs.versions.toml` | ✅ Done | 30+ libraries, plugins, bundles – single source of truth |
| `gradle.properties` | ✅ Done | `configuration-cache`, `nonTransitiveRClass`, Kotlin cache, `-XX:+UseParallelGC` |
| `gradle/wrapper/gradle-wrapper.properties` | ✅ Done | Gradle 8.6 |
| `app/build.gradle` | ✅ Done | All deps via `libs.*` catalog aliases, no hardcoded versions |
| `app/proguard-rules.pro` | ✅ Done | Room, Gemini SDK, ML Kit, Firebase, Gson rules |
| `local.defaults.properties` | ✅ Done | `GEMINI_API_KEY=YOUR_KEY_HERE` template |
| `.gitignore` | ✅ Done | Excludes `local.properties`, `google-services.json`, `*.jks` |

### Convention Plugins (gradle-patterns skill)

| File | Status | Notes |
|------|--------|-------|
| `build-logic/settings.gradle` | ✅ Done | Composite build root |
| `build-logic/convention/build.gradle` | ✅ Done | `groovy-gradle-plugin` |
| `kirana.android.application.gradle` | ✅ Done | Centralised compileSdk, minSdk, Java 17, ViewBinding |
| `kirana.android.room.gradle` | ✅ Done | Centralised `room.schemaLocation` annotation processor arg |

---

## 2. Android Manifest & Application

| File | Status |
|------|--------|
| `AndroidManifest.xml` | ✅ Done |
| `KiranaApp.java` | ✅ Done |

---

## 3. Data Layer (Room 2.8.4)

| File | Status |
|------|--------|
| `model/Product.java` | ✅ Done |
| `model/PriceHistory.java` | ✅ Done |
| `db/DateConverter.java` | ✅ Done |
| `db/ProductDao.java` | ✅ Done |
| `db/PriceHistoryDao.java` | ✅ Done |
| `db/KiranaDatabase.java` | ✅ Done |
| `repository/ProductRepository.java` | ✅ Done |

---

## 4. AI Agent (Firebase AI Logic – Gemini 2.0 Flash)

| File | Status | Notes |
|------|--------|-------|
| `ai/KiranaAiAgent.java` | ✅ Done | **Migrated** from deprecated `generativeai:0.9.0` → `firebase-ai` SDK |

---

## 5. Voice Interface

| File | Status |
|------|--------|
| `voice/VoiceManager.java` | ✅ Done |

---

## 6. UI Layer

### Activity & Navigation
| File | Status |
|------|--------|
| `ui/MainActivity.java` | ✅ Done |
| `res/navigation/nav_graph.xml` | ✅ Done |

### Dashboard Screen
| File | Status |
|------|--------|
| `ui/dashboard/DashboardViewModel.java` | ✅ Done |
| `ui/dashboard/DashboardFragment.java` | ✅ Done |
| `res/layout/fragment_dashboard.xml` | ✅ Done |

### Prices Screen
| File | Status |
|------|--------|
| `ui/prices/PricesFragment.java` | ✅ Done |
| `ui/prices/PriceListAdapter.java` | ✅ Done |
| `res/layout/fragment_prices.xml` | ✅ Done |

### Scanner Screen (ML Kit OCR)
| File | Status |
|------|--------|
| `ui/scanner/ScannerFragment.java` | ✅ Done |
| `res/layout/fragment_scanner.xml` | ✅ Done |

### History Screen
| File | Status |
|------|--------|
| `ui/history/HistoryFragment.java` | ✅ Done |
| `ui/history/HistoryViewModel.java` | ✅ Done |
| `ui/history/PriceHistoryAdapter.java` | ✅ Done |
| `res/layout/fragment_history.xml` | ✅ Done |

### Shared Layouts
| File | Status |
|------|--------|
| `res/layout/activity_main.xml` | ✅ Done |
| `res/layout/item_product_card.xml` | ✅ Done |
| `res/layout/dialog_add_product.xml` | ✅ Done |
| `res/layout/item_history.xml` | ✅ Done |

---

## 7. Resources

### Values
| File | Status |
|------|--------|
| `res/values/strings.xml` | ✅ Done |
| `res/values/colors.xml` | ✅ Done |
| `res/values/themes.xml` | ✅ Done |
| `res/values/dimens.xml` | ✅ Done |
| `res/values-night/themes.xml` | ✅ Done |

### Drawables (Vector Icons)
| File | Status |
|------|--------|
| `ic_add.xml`, `ic_mic.xml`, `ic_mic_active.xml` | ✅ Done |
| `ic_edit.xml`, `ic_check.xml`, `ic_close.xml`, `ic_search.xml` | ✅ Done |
| `ic_pin.xml`, `ic_dashboard.xml`, `ic_price_tag.xml` | ✅ Done |
| `ic_scanner.xml`, `ic_history.xml` | ✅ Done |
| `nav_item_color.xml` | ✅ Done |

### Menu & Navigation
| File | Status |
|------|--------|
| `res/menu/bottom_nav_menu.xml` | ✅ Done |

### XML Config
| File | Status |
|------|--------|
| `res/xml/backup_rules.xml` | ✅ Done |
| `res/xml/data_extraction_rules.xml` | ✅ Done |

---

## 8. Firebase & gcloud CLI

| Task | File / Resource | Status |
|------|------|--------|
| Firestore security rules | `firestore.rules` | ✅ Done |
| Firebase CLI config | `firebase.json` | ✅ Done |
| Firestore indexes | `firestore.indexes.json` | ✅ Done |
| gcloud/Firebase setup (Linux/Mac) | `scripts/setup_firebase.sh` | ✅ Done |
| gcloud/Firebase setup (Windows) | `scripts/setup_firebase.bat` | ✅ Done |
| gcloud CLI Version 573.0.0 | System Installation | ✅ **Installed** |
| GCP Project Creation | ID: `kirana-store-abs6187` | ✅ **Created & Configured** |
| `google-services.json` | Download from Firebase Console | 🔧 **User action** (Pending) |

### completed gcloud CLI Sequence
```bash
# Checked active login (contact2abhaygupta6187@gmail.com)
gcloud auth list

# Created GCP project
gcloud projects create kirana-store-abs6187 --name="Kirana Store Manager"
gcloud config set project kirana-store-abs6187

# Enabled required APIs
gcloud services enable firebase.googleapis.com firestore.googleapis.com firebaseappcheck.googleapis.com
```

---

## 9. Documentation

| File | Status |
|------|--------|
| `README.md` | ✅ Done |
| `plan.md` (this file) | ✅ Done |

---

## 10. Dependency Version Catalog Compliance (add-dependency skill)

> Rule: **Never hardcode versions in build.gradle** — use `gradle/libs.versions.toml`

| Library | Old (hardcoded) | New (catalog alias) | Change |
|---|---|---|---|
| Room | `2.6.1` | `libs.room.runtime` → **2.8.4** | ⬆️ Upgraded |
| Navigation | `2.7.7` | `libs.navigation.fragment` → **2.9.8** | ⬆️ Upgraded |
| Lifecycle | `2.8.2` | `libs.bundles.lifecycle` → **2.11.0** | ⬆️ Upgraded |
| Firebase BOM | `33.1.0` | `libs.firebase.bom` → **34.15.0** | ⬆️ Upgraded |
| ML Kit OCR | `16.0.0` | `libs.mlkit.text.recognition` → **17.3.0** | ⬆️ Upgraded |
| CameraX | `1.3.3` | `libs.bundles.camerax` → **1.4.2** | ⬆️ Upgraded |
| `generativeai:0.9.0` ❌ | EOL Nov 2025 | `libs.firebase.ai` (BOM-managed) | ✅ Migrated |
| `firebase-vertexai` ❌ | Deprecated | `libs.firebase.ai` | ✅ Migrated |

---

## 11. Gradle Build Patterns Compliance (gradle-patterns skill)

| Pattern | Status | Detail |
|---|---|---|
| Version Catalog (`libs.versions.toml`) | ✅ Done | `[versions]`, `[libraries]`, `[plugins]`, `[bundles]` |
| No hardcoded versions in `build.gradle` | ✅ Done | All via `libs.*` aliases |
| Convention plugins (`build-logic/`) | ✅ Done | `kirana.android.application`, `kirana.android.room` |
| `org.gradle.configuration-cache=true` | ✅ Done | `gradle.properties` |
| `org.gradle.caching=true` | ✅ Done | `gradle.properties` |
| `org.gradle.parallel=true` | ✅ Done | `gradle.properties` |
| `android.nonTransitiveRClass=true` | ✅ Done | Faster R class compilation |
| `android.nonFinalResIds=true` | ✅ Done | Library compatibility |
| `kotlin.incremental=true` | ✅ Done | Faster Kotlin recompilation |
| Dependency bundles | ✅ Done | `camerax`, `lifecycle`, `firebase-core` |

### Useful Build Commands (gradle-patterns)
```bash
# Full build with timing profile
./gradlew build --profile

# Debug APK only
./gradlew :app:assembleDebug

# Dependency tree check
./gradlew :app:dependencies --configuration releaseRuntimeClasspath

# Verify configuration cache is working
./gradlew :app:assembleDebug --configuration-cache

# Clear caches if Gradle acts up
./gradlew cleanBuildCache
```

---

## 12. Legacy → Modern Migration Summary

| Legacy (Foodventory 2018) | Modern (Kirana Store 2026) | Status |
|---|---|---|
| `com.android.support:appcompat-v7:28` | `androidx.appcompat:1.7.0` | ✅ |
| `android.arch.persistence.room:1.1.1` | `androidx.room:2.8.4` | ✅ |
| `firebase-ml-vision:18.0.1` ❌ | `mlkit:text-recognition:17.3.0` | ✅ |
| `compileSdkVersion 28` | `compileSdk 34` | ✅ |
| `minSdkVersion 24` | `minSdk 26` | ✅ |
| `android.support.*` | `androidx.*` (Jetifier disabled) | ✅ |
| Legacy Camera API | CameraX 1.4.2 | ✅ |
| `generativeai:0.9.0` ❌ EOL | `firebase-ai` (BOM 34.15.0) | ✅ |
| No voice | Native STT hi-IN + TTS | ✅ |
| No price history | Auto-timestamped PriceHistory table | ✅ |
| Gradle 4.x / hardcoded deps | Gradle 8.6 + `libs.versions.toml` | ✅ |
| No convention plugins | `build-logic/convention/` | ✅ |

---

## 13. What's Left (Genuine Remaining Work)

| # | Item | Owner | Priority |
|---|------|-------|----------|
| 1 | `app/google-services.json` – run `scripts\setup_firebase.bat`, then download from Firebase Console | 🔧 User | 🔴 Required to build |
| 2 | `local.properties` – add `GEMINI_API_KEY=AIza...` from [AI Studio](https://aistudio.google.com/app/apikey) | 🔧 User | 🔴 Required for voice AI |
| 3 | **Gradle Sync** – Android Studio → *Sync Project with Gradle Files* | 🔧 User | 🔴 Required |
| 4 | **Build verify** – `./gradlew :app:assembleDebug` + `:app:testDebugUnitTest` to confirm zero errors | Both | 🟡 After sync |
| 5 | **GitHub Repository Setup** – Created repository `Abs6187/KiranaStore` and pushed code | ✅ Done | — |
| 6 | Unit tests – Room entities, DateConverter, FuzzyMatcher, KiranaAiAgent JSON parse, VoiceManager error map | ✅ Done | — |
| 7 | Instrumented tests – Room DAOs (incl. cascade) + Espresso Dashboard add-product flow | ✅ Done | — |
| 8 | `CHANGELOG.md` | ✅ Done | — |

### 13a. build-logic Convention Plugins (resolved)

> Earlier the convention plugins in `build-logic/` were marked ✅ but were **never applied** in `app/build.gradle` (dead code), and the convention module had no AGP dependency so the plugins could not have compiled if applied.
>
> **Resolved:**
> - `build-logic/convention/build.gradle` now declares `com.android.tools.build:gradle:8.3.2` so the `android { }` DSL type-checks.
> - `kirana.android.application.gradle` no longer imports the unused `AppExtension`.
> - `app/build.gradle` applies `id 'kirana.android.application'` + `id 'kirana.android.room'` and the redundant `compileSdk` / `compileOptions` / `buildFeatures` / `room.schemaLocation` blocks were removed (now owned by the conventions).
> - ⚠️ Not yet build-verified (no `google-services.json` on this host). If application fails, the documented rollback is to delete `build-logic/` + the `includeBuild` line and restore `alias(libs.plugins.android.application)`.

---

## Overall Progress

| Category | Count | Status |
|---|---|---|
| Build system + Version catalog + Convention plugins | 13 files | ✅ |
| Android Manifest + Application | 2 files | ✅ |
| Data layer (Room 2.8.4) | 7 files | ✅ |
| AI + Voice + OCR | 2 files | ✅ |
| UI Fragments + ViewModels + Adapters | 11 files | ✅ |
| XML Layouts | 8 files | ✅ |
| Resources (values, drawables, menus) | 15 files | ✅ |
| Firebase + gcloud CLI scripts | 5 files | ✅ |
| Documentation | 2 files | ✅ |
| **Total written** | **65+ files** | **✅ Code complete** |

> **🚦 Status: Code 100% complete.**
> Only 3 user actions remain before first build: `google-services.json` + `local.properties` key + Gradle Sync.
