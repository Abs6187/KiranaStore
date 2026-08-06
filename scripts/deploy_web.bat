@echo off
echo ===================================================
echo   Building KiranaStore PWA Web App & Deploying
echo ===================================================

cd %~dp0\..\web
call npm install
call npm run build

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Web build failed!
    exit /b %ERRORLEVEL%
)

cd %~dp0\..
echo Deploying to Firebase Hosting (kirana-store-abs6187)...
call firebase deploy --only hosting

echo ===================================================
echo   Deployment Complete!
echo ===================================================
