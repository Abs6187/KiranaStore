@echo off
REM ============================================================
REM Kirana Store Manager – Firebase + gcloud Setup (Windows)
REM ============================================================
REM Prerequisites: gcloud CLI, firebase CLI (npm install -g firebase-tools)
REM ============================================================

echo ============================================
echo  Kirana Store Manager - Firebase Setup
echo ============================================

REM Step 1: Auth
echo [1/5] Authenticating with Google Cloud...
gcloud auth login
if %ERRORLEVEL% NEQ 0 ( echo ERROR: gcloud auth failed & pause & exit /b 1 )

REM Step 2: Set project
set /p PROJECT_ID="Enter your GCP Project ID: "
gcloud config set project %PROJECT_ID%

REM Step 3: Enable APIs
echo [2/5] Enabling APIs...
gcloud services enable firebase.googleapis.com firestore.googleapis.com firebaseappcheck.googleapis.com
echo APIs enabled

REM Step 4: Firebase login + link
echo [3/5] Firebase login...
firebase login
firebase use %PROJECT_ID%

REM Step 5: Deploy Firestore rules
echo [4/5] Deploying Firestore rules...
firebase deploy --only firestore:rules --project %PROJECT_ID%

echo.
echo [5/5] NEXT STEPS:
echo  1. Firebase Console: https://console.firebase.google.com/project/%PROJECT_ID%/settings/general
echo     - Add Android app: com.kirana.store
echo     - Download google-services.json -> place in app/
echo.
echo  2. Gemini API Key: https://aistudio.google.com/app/apikey
echo     - Add to local.properties: GEMINI_API_KEY=your_key_here
echo.
echo ============================================
echo Done! Open in Android Studio and Sync Gradle
echo ============================================
pause
