#!/bin/bash

# Run script for Charmings Android app
# This script builds the APK and installs it on an emulator or connected device

set -e

echo "=== Running Charmings Android App ==="

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
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/emulator:$PATH"

# Navigate to project directory
cd "$(dirname "$0")"

# Check for connected devices/emulators
echo "Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List" | grep -v "^$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "No devices connected. Starting emulator..."
    
    # List available emulators
    EMULATORS=$(emulator -list-avds 2>/dev/null | head -1)
    
    if [ -z "$EMULATORS" ]; then
        echo "Error: No emulators found. Please create an AVD first."
        echo "You can create one using Android Studio or:"
        echo "  avdmanager create avd -n Pixel_API_34 -k 'system-images;android-34;google_apis;x86_64'"
        exit 1
    fi
    
    echo "Starting emulator: $EMULATORS"
    emulator -avd "$EMULATORS" -no-snapshot-load &
    
    echo "Waiting for emulator to boot..."
    adb wait-for-device
    
    # Wait for boot to complete
    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null)" != "1" ]; do
        echo "Waiting for boot to complete..."
        sleep 2
    done
    
    echo "Emulator is ready!"
fi

# Build the app
echo ""
echo "Building the app..."
./build.sh

# Install the APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "Installing APK..."
adb install -r "$APK_PATH"

# Launch the app
echo ""
echo "Launching app..."
adb shell am start -n com.charmings.app/.MainActivity

echo ""
echo "=== App is running ==="
echo "To view logs: adb logcat -s StepCounterService:* CharmingsApp:*"
