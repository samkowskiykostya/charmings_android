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

2. **Weather API key** — put it in `local.properties` (git-ignored), or set `WEATHER_API_KEY`:
   ```properties
   weather.apiKey=YOUR_WEATHERAPI_COM_KEY
   ```
   Without a key the app works; weather-dependent pets just can't appear.

> **Images are not bundled.** The APK ships only 24 px thumbnails (`res/drawable-nodpi/thumb_XX.webp`)
> plus Ось's full image. Full pictures (`pet-images/v2/pet_XX.webp`, 768 px, transparent cut-outs) are downloaded on demand
> from `petImageBaseUrl` (default: jsDelivr serving this public GitHub repo) and cached on disk by Coil.
> **Push `pet-images/` before releasing**, or override the URL with `-PpetImageBaseUrl=https://…/`.
> `copy_assets.sh` is obsolete. `pet-images/pet_XX.webp` (white backgrounds) stay for older installs.
> If you change pictures, put them in a new folder (`v3/`…) and bump the URL: jsDelivr and Coil both
> cache by URL, so overwriting files in place would keep showing the old ones.

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
- `INTERNET` - For weather API and pet images
- `ACCESS_COARSE_LOCATION` - Optional; city-level weather from the last known location (falls back to Kyiv)

## How Pet Catching Works

1. Steps are grouped per minute. A minute with ≥ 60 steps is a *walking minute*; a walk starts at the
   first one and survives pauses of up to 5 minutes (traffic lights). Pottering at home never counts.
   Logic: `domain/WalkEngine.kt` (pure, unit-tested).
2. Once a walk has ≥ 400 steps since the last catch, pets are checked every 3 minutes
3. Requirements can include:
   - Minimum steps walked
   - Specific day of week
   - Specific month
   - Time of day range
   - Weather conditions
   - Temperature range
   - Holidays
4. If requirements are met, there's a probability roll — **once per pet per walk**
5. Caught pets are saved and displayed in the "Знайдені" tab

## Architecture

- **UI**: Jetpack Compose with Material 3
- **Navigation**: Compose Navigation
- **State Management**: ViewModel + StateFlow
- **Persistence**: DataStore Preferences
- **Background Work**: Foreground Service with step sensor
- **Network**: OkHttp for weather API
- **Animations**: Compose-only (breathing, squash, confetti, weather particles); respects "Remove animations"

## Building Release APK

```bash
./gradlew assembleRelease
```

The release APK will be at `app/build/outputs/apk/release/app-release-unsigned.apk`

Run the unit tests with `./gradlew testDebugUnitTest`.

## Debugging

View logs:
```bash
adb logcat -s StepCounterService:* CharmingsApp:*
```

## License

Same as the original Expo project.
