#!/bin/bash

# Build signed release APK for Charmings Android app
set -e

echo "=== Building Charmings Release APK ==="

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo "Error: ANDROID_HOME is not set."
        exit 1
    fi
fi

cd "$(dirname "$0")"

# --bundle: build a signed AAB for Google Play instead of an APK to sideload.
BUNDLE=0
[ "$1" = "--bundle" ] && BUNDLE=1

# Check for keystore
KEYSTORE="charmings-release.keystore"
if [ ! -f "$KEYSTORE" ]; then
    echo "Keystore not found. Creating one..."
    echo "You'll be prompted to create a password and enter certificate details."
    keytool -genkey -v -keystore "$KEYSTORE" -alias charmings -keyalg RSA -keysize 2048 -validity 10000
    echo ""
    echo "Keystore created! IMPORTANT: Keep this file and password safe!"
    echo ""
fi

# Prompt for passwords if not set (or put release.keystore.key=... in git-ignored local.properties)
if [ -z "$KEYSTORE_PASSWORD" ] && ! grep -q "^release.keystore.key=" local.properties 2>/dev/null; then
    read -sp "Enter keystore password: " KEYSTORE_PASSWORD
    echo ""
    export KEYSTORE_PASSWORD
fi

if [ -n "$KEYSTORE_PASSWORD" ] && [ -z "$KEY_PASSWORD" ]; then
    read -sp "Enter key password (press Enter if same as keystore): " KEY_PASSWORD
    echo ""
    if [ -z "$KEY_PASSWORD" ]; then
        KEY_PASSWORD="$KEYSTORE_PASSWORD"
    fi
    export KEY_PASSWORD
fi

# Check for gradle wrapper jar
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p gradle/wrapper
    curl -L -o "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

chmod +x ./gradlew

if [ "$BUNDLE" = 1 ]; then
    ./gradlew bundleRelease
    AAB=app/build/outputs/bundle/release/app-release.aab
    [ -f "$AAB" ] || { echo "Error: Build failed."; exit 1; }
    echo "=== AAB: $AAB ==="
    exit 0
fi

echo "Building release APK..."
./gradlew assembleRelease

APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo "=== Build Successful ==="
    echo "APK location: $APK_PATH"
    echo ""
    echo "Users can download and install this APK on their phones."
    echo "(They need to enable 'Install from unknown sources' in settings)"
else
    echo "Error: Build failed."
    exit 1
fi

./serve-release.sh