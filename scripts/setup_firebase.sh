#!/bin/bash
# =============================================================================
# Kirana Store Manager – Firebase + gcloud CLI Setup Script
# =============================================================================
# Prerequisites:
#   1. gcloud CLI installed: https://cloud.google.com/sdk/docs/install
#   2. Firebase CLI installed: npm install -g firebase-tools
#   3. Android Studio installed with the app open
# =============================================================================

set -e  # Exit on any error

echo "============================================"
echo " Kirana Store Manager – Firebase Setup"
echo "============================================"

# ── Step 1: gcloud authentication ────────────────────────────────────────────
echo ""
echo "[1/8] Authenticating with Google Cloud..."
gcloud auth login

# ── Step 2: Set or create GCP project ────────────────────────────────────────
echo ""
echo "[2/8] Setting up GCP project..."
read -p "Enter your GCP Project ID (or press Enter to create new): " PROJECT_ID

if [ -z "$PROJECT_ID" ]; then
    PROJECT_ID="kirana-store-$(date +%s)"
    echo "Creating project: $PROJECT_ID"
    gcloud projects create "$PROJECT_ID" --name="Kirana Store Manager"
fi

gcloud config set project "$PROJECT_ID"
echo "✅ Project set to: $PROJECT_ID"

# ── Step 3: Enable required APIs ─────────────────────────────────────────────
echo ""
echo "[3/8] Enabling required Google Cloud APIs..."
gcloud services enable \
    firebase.googleapis.com \
    firestore.googleapis.com \
    firebaseappcheck.googleapis.com \
    cloudresourcemanager.googleapis.com
echo "✅ APIs enabled"

# ── Step 4: Firebase login ────────────────────────────────────────────────────
echo ""
echo "[4/8] Logging into Firebase CLI..."
firebase login

# ── Step 5: Link Firebase to GCP project ─────────────────────────────────────
echo ""
echo "[5/8] Linking Firebase to project $PROJECT_ID..."
firebase use "$PROJECT_ID" 2>/dev/null || firebase use --add

# ── Step 6: Initialize Firestore ─────────────────────────────────────────────
echo ""
echo "[6/8] Initialising Cloud Firestore..."
firebase init firestore --project "$PROJECT_ID"

# ── Step 7: Deploy Firestore rules ───────────────────────────────────────────
echo ""
echo "[7/8] Deploying Firestore security rules..."
firebase deploy --only firestore:rules --project "$PROJECT_ID"
echo "✅ Firestore rules deployed"

# ── Step 8: Download google-services.json ────────────────────────────────────
echo ""
echo "[8/8] IMPORTANT: Download google-services.json"
echo ""
echo "  1. Go to: https://console.firebase.google.com/project/$PROJECT_ID/settings/general"
echo "  2. Add an Android app with package: com.kirana.store"
echo "  3. Download google-services.json"
echo "  4. Place it in: app/google-services.json"
echo ""
echo "──────────────────────────────────────────────"
echo " Gemini API Key Setup"
echo "──────────────────────────────────────────────"
echo "  1. Go to: https://aistudio.google.com/app/apikey"
echo "  2. Create a free Gemini API key"
echo "  3. Add to local.properties (this file is in .gitignore):"
echo "     GEMINI_API_KEY=your_key_here"
echo ""
echo "============================================"
echo "✅ Setup complete! Open in Android Studio"
echo "   and click 'Sync Project with Gradle Files'"
echo "============================================"
