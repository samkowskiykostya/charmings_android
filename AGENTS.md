# Agent instructions

## Deploy after every change

A change is not done until it is running on the Android emulator. After each change that touches the app (code, resources, assets, Gradle config):

1. **Deploy to the emulator.** Run `./run.sh`. It builds a debug APK and installs and launches it on the emulator. `.deploy_config` should be in emulator mode (`./deploy.sh --emulator`). If no emulator is running, `run.sh` starts the first AVD and waits for it to boot.
2. **Deploy to the device.** Run `./deploy.sh -s` to install the same APK on the connected phone (USB or WiFi) without rebuilding. If no phone is connected, say so in your report instead of skipping it silently.
3. **Check the result.** Make sure the app launched without crashing (`adb logcat -s StepCounterService:* CharmingsApp:* AndroidRuntime:E`). For UI changes, take a screenshot (`adb exec-out screencap -p > <scratch>/screen.png`) and look at the changed screen.
4. **Report it.** Tell the user it was deployed and what you checked. If the build, install or launch failed, report the error output instead of claiming the change is done.

Changes that touch only docs or scripts unrelated to the app do not need a deploy.

## Google Play release

- Play Console app: Чарівнятка, `com.charmings.app`, console app id `4972097676786640939`. Free game, default language `uk`.
- Internal testing track testers: email list **ST-T1**.
- Build the AAB with `./build-release.sh --bundle`. It signs with `charmings-release.keystore` (the Play upload key). The passwords go in git-ignored `local.properties` as `release.keystore.key=` (and `release.key.password=` if the key password is different), or in the `KEYSTORE_PASSWORD`/`KEY_PASSWORD` env vars.
- Upload through the `google-play-release` skill. Until the app is first published, Play accepts only `--status draft`; roll the draft out in Console (Test and release → Internal testing).
- targetSdk must meet Play's minimum (36 since Aug 2026). Bump `versionCode` for every upload.
- A new app's first internal release can take about 1.5 hours before testers can install it. Until then they get "Item not found".
- Before the first review (see the skill's pre-review checklist; Sleep Tracker was rejected for each of these):
  - Done on 14 Nov 2026:
    - The default store listing (uk) is saved and "Ready to send for review", not yet sent. It has the 512 px icon (`launcher_pet` on `#FFE7CF`), a feature graphic, and 3 screenshots, all labelled as AI-generated.
    - The privacy policy names "Чарівнятка".
    - 1.2 (code 4) adds a "Політика конфіденційності" link on the dashboard and is on internal testing.
  - App content saved on 14 Nov 2026 (not sent for review):
    - Advertising ID: No. Government apps: No. Financial features: none.
    - Health apps: "Activity and fitness", with an `ACTIVITY_RECOGNITION` description (steps catch pets, step data stays on the device).
    - Data safety: only approximate location is collected (`WeatherApi` sends it, rounded to 2 decimals, to weatherapi.com). It is optional, used for app functionality, not shared, not marked ephemeral, and encrypted in transit. There are no accounts. Deletion is by email via the privacy policy URL. The Families-policy badge is shown. If you add any SDK or upload, update this form.
  - Trader status: "applies", because the account is a trader. "No" would tell EEA and UK users that consumer law doesn't apply.
  - Foreground service permissions: `StepCounterService` is a `health` foreground service with `START_STICKY`, and `BootReceiver` restarts it after reboot. The declaration "Health data sync, step counter, exercise tracker" was saved on 15 Nov 2026. Its video link is https://drive.google.com/file/d/1B9OBtDkOdalFX_fwhX137qMEW1sAlLuN/view, shared with anyone who has the link, in aiinject@gmail.com's Drive. Don't unshare it or delete it.
    - The video was recorded on the emulator on 14 Nov 2026 (`~/Desktop/charmings_fgs_step_counter.mp4`, 24 s): the app with tracking on, Home, the shade with the expanded "Шукаємо чарівняток / Сьогодні: N кроків" notification and its "Зупинити" action, then back in the app.
    - The emulator has no virtual step sensor, so the count stays at 0.
  - "Google Play Games on PC" is opted in by default.
  - Store settings: **Game**, category Casual, contact aiinject@gmail.com and https://thetix.org (saved 14 Nov 2026). Pricing: Free.
  - Dashboard setup and all App content declarations are complete. Publishing overview unlocks once a release exists on a reviewed track (closed, open or production); creating it is the owner's call.
