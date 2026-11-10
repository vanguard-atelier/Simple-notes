#!/usr/bin/env bash
set -euo pipefail

echo "==================================================="
echo "  Building SimpleNotes (com.shumail.simplenotes)   "
echo "==================================================="

if [ ! -f "debug.keystore" ] && [ -f "debug.keystore.base64" ]; then
    echo "Decoding debug.keystore from debug.keystore.base64..."
    base64 -d debug.keystore.base64 > debug.keystore
fi

if [ -x "./gradlew" ]; then
    ./gradlew assembleDebug --stacktrace
else
    gradle assembleDebug --stacktrace
fi

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
mkdir -p dist

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "dist/SimpleNotes-debug.apk"
    echo "Debug APK successfully generated:"
    echo " - $APK_PATH"
    echo " - dist/SimpleNotes-debug.apk"
else
    echo "Error: Expected debug APK not found at $APK_PATH"
    exit 1
fi
