#!/bin/bash

# Build script for Charmings Android app
# This script builds the APK

set -e

echo "=== Building Charmings Android App ==="

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    # Try common locations
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo "Error: ANDROID_HOME is not set. Please set it to your Android SDK location."
        exit 1
    fi
fi

echo "Using ANDROID_HOME: $ANDROID_HOME"

# Add platform-tools to PATH
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH"

# Navigate to project directory
cd "$(dirname "$0")"

# Check for gradle wrapper jar
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Gradle wrapper..."
    mkdir -p gradle/wrapper
    curl -L -o "$WRAPPER_JAR" "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
fi

# Make gradlew executable
chmod +x ./gradlew
GRADLE_CMD="./gradlew"

# Clean and build
echo "Cleaning previous build..."
$GRADLE_CMD clean

echo "Building debug APK..."
$GRADLE_CMD assembleDebug

# Check if build was successful
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo "=== Build Successful ==="
    echo "APK location: $APK_PATH"
    echo ""
else
    echo "Error: Build failed. APK not found."
    exit 1
fi
