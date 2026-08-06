#!/bin/bash
set -e

echo "==================================================="
echo "  Building KiranaStore PWA Web App & Deploying"
echo "==================================================="

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/../web"

npm install
npm run build

cd "$SCRIPT_DIR/.."
echo "Deploying to Firebase Hosting (kirana-store-abs6187)..."
firebase deploy --only hosting

echo "==================================================="
echo "  Deployment Complete!"
echo "==================================================="
