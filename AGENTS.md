# Agent instructions

## Deploy after every change

A change is not done until it is running on the Android emulator. After each change that touches the app (code, resources, assets, Gradle config):

1. **Deploy to the emulator.** Run `./run.sh`. It builds a debug APK and installs and launches it on the emulator. `.deploy_config` should be in emulator mode (`./deploy.sh --emulator`). If no emulator is running, `run.sh` starts the first AVD and waits for it to boot.
2. **Deploy to the device.** Run `./deploy.sh -s` to install the same APK on the connected phone (USB or WiFi) without rebuilding. If no phone is connected, say so in your report instead of skipping it silently.
3. **Check the result.** Make sure the app launched without crashing (`adb logcat -s StepCounterService:* CharmingsApp:* AndroidRuntime:E`). For UI changes, take a screenshot (`adb exec-out screencap -p > <scratch>/screen.png`) and look at the changed screen.
4. **Report it.** Tell the user it was deployed and what you checked. If the build, install or launch failed, report the error output instead of claiming the change is done.

Changes that touch only docs or scripts unrelated to the app do not need a deploy.
