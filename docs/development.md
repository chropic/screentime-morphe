# Development and validation

## Toolchain baseline

Record the exact JDK, Android SDK/build tools, Gradle, Morphe patcher/bundle, fixture APK, and device-build versions for every run. The feasibility device baseline is GrapheneOS `2026091000`, Android 17 / API 37.

The local fixture toolchain currently resolves API 37 platform files and Build Tools 37.0.0 under `C:\Users\charlie\AppData\Local\Android\Sdk`. Set `ANDROID_HOME` and `ANDROID_SDK_ROOT` to that path when running fixture builds from a shell that has not inherited Android Studio's environment.

## Build

Run `./gradlew buildAndroid`; the bundle is produced beneath `patches/build/libs/`. Run `./gradlew generatePatchesList` only when intentionally generating the release list. Release activation is deferred.

Run `bash scripts/test-policy-core.sh` to compile and execute the Android-independent policy tests with the installed JDK. CI runs this test and performs a non-release bundle compile. Fixture-patching CI is intentionally conditional on immutable fixture input and an exact patch command; until configured, it is skipped rather than reported as a passing compatibility test.

CI also installs the `platforms;android-37.0` SDK package and Build Tools 37.0.0 before compiling the lifecycle fixture. This proves the fixture source builds; it does not patch, install, or exercise the APK.

## Fixture workflow

Build each source fixture, calculate its APK hash, patch it, install it, execute its declared case, and preserve logs/screenshots/timestamps in the integration evidence. Compare a reference-patched baseline with the same build plus Screen time for Sync for Reddit/Patcheddit, X/Piko, Instagram/Piko, and YouTube/official Morphe patches.

## Debugging

Collect `logcat`, component/process state, notification state, and fixture-visible events. Verify control-process isolation by confirming no host application initialization occurs there. On storage failure or snapshot/database mismatch, expect diagnostic blocking—not replenished time.

The current provider API accepts `set_initial_policy`, `read_state`, `commit_usage`, `request_policy_change`, and `cancel_policy_change`. A mixed change applies strict fields first and queues relaxed fields or reset-time changes for 24 hours. Its bundle keys are defined in `StateProvider`; treat it as an internal version-1 contract until a device test establishes migration and error-handling behavior.
