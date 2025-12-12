# Чарівнятка (Charmings) - Native Android App

A native Android port of the Charmings Expo app. This is a gamified step counter that rewards users with collectible magical creatures ("чарівнятка") based on their walking activity and various conditions.

## Features

- **Step Counting**: Background step tracking using Android's step counter sensor
- **Pet Collection**: Catch magical creatures by meeting specific requirements (steps, time of day, weather, holidays, etc.)
- **Foreground Service**: Continues tracking in background with notification
- **Stop from Drawer**: Stop tracking directly from the notification
- **Weather Integration**: Some pets require specific weather conditions
- **Holiday Support**: Special pets appear on Ukrainian holidays

## Requirements

- Android 11 (API 30) or higher
- Android SDK with build tools
- Java 17+
- Step counter sensor (most modern phones have this)

## Project Structure

```
_charmings_android/
├── app/
│   └── src/main/
│       ├── java/com/charmings/app/
│       │   ├── data/           # Data models, repositories, API
│       │   ├── domain/         # Business logic (PetCatcher, HolidayCalculator)
│       │   ├── receiver/       # Broadcast receivers
│       │   ├── service/        # Step counter foreground service
│       │   └── ui/             # Compose UI screens and components
│       ├── res/                # Resources (drawables, values)
│       └── assets/             # Lottie animations
├── build.sh                    # Build script
├── run.sh                      # Build and run script
└── copy_assets.sh              # Copy images from Expo project
```

## Setup

1. **Set ANDROID_HOME** environment variable:
   ```bash
   export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
   # or
   export ANDROID_HOME=$HOME/Android/Sdk          # Linux
   ```

2. **Copy assets** from the Expo project:
   ```bash
   chmod +x copy_assets.sh
   ./copy_assets.sh
   ```

3. **Build the app**:
   ```bash
   chmod +x build.sh
   ./build.sh
   ```

4. **Run on emulator/device**:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

## Permissions

The app requires the following permissions:
- `ACTIVITY_RECOGNITION` - For step counting
- `FOREGROUND_SERVICE` - For background tracking
- `POST_NOTIFICATIONS` - For notifications (Android 13+)
- `INTERNET` - For weather API

## How Pet Catching Works

1. The app tracks steps in the background
2. After 400 steps, it checks if any pet's requirements are met
3. Requirements can include:
   - Minimum steps walked
   - Specific day of week
   - Specific month
   - Time of day range
   - Weather conditions
   - Temperature range
   - Holidays
4. If requirements are met, there's a probability roll
5. Caught pets are saved and displayed in the "Знайдені" tab

## Architecture

- **UI**: Jetpack Compose with Material 3
- **Navigation**: Compose Navigation
- **State Management**: ViewModel + StateFlow
- **Persistence**: DataStore Preferences
- **Background Work**: Foreground Service with step sensor
- **Network**: OkHttp for weather API
- **Animations**: Lottie Compose

## Building Release APK

```bash
./gradlew assembleRelease
```

The release APK will be at `app/build/outputs/apk/release/app-release-unsigned.apk`

## Debugging

View logs:
```bash
adb logcat -s StepCounterService:* CharmingsApp:*
```

## License

Same as the original Expo project.
