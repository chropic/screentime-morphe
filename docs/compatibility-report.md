# Compatibility report

## Status

No experiments have been executed. Every claim below is untested until an evidence row is added.

The policy-core JVM tests cover calculation only; they do not validate APK injection, Android lifecycle behavior, storage, notifications, or blocking.

Current runtime status: the manifest patch declares a dedicated-process activity/provider and an `AppComponentFactory` wrapper. The provider persists an explicit initial policy and deduplicated monotonic-session checkpoints in private SQLite, atomically writes a state snapshot, and represents replaceable 24-hour policy relaxations. The activity provides initial budget/reset/mode entry and state display. Snapshot reads by early host startup, usage lifecycle instrumentation, process stop, notification removal, and background-work suppression have not been tested or implemented. Full blocking is therefore untested and unavailable.

## Required evidence fields

| Field | Value |
| --- | --- |
| Device / GrapheneOS build | Pending device run |
| Android API level | Pending device run |
| APK package, version, SHA-256 | Pending fixture/target run |
| Screen-time bundle version and selected patches | Pending build |
| Reference patch bundle and selected patches | Pending baseline |
| Toolchain versions | Pending build |
| Procedure, logs, screenshots, timestamps | Pending run |
| Result | Untested / pass / fail |

## Fixture build evidence

| Field | Value |
| --- | --- |
| Fixture | `lifecycle` |
| APK package / version | `dev.screentime.fixture.lifecycle` / `1.0.0` (version code 1) |
| APK SHA-256 | `8204330727E3F1F3F4338CDB726E2B6878E1CDA07B908262DC9C4091613123ED` |
| Build result | Built successfully; not installed, patched, or executed |
| Android SDK | Platform API 37.0; Build Tools 37.0.0 |
| Fixture build toolchain | AGP 9.4.0; Gradle 9.7.1; Temurin JDK 25.0.4.1; Windows 11 amd64 |

This is build evidence only. It is not compatibility, startup, or blocking evidence.

## Continuous-integration evidence

GitHub Actions run `34778932830` completed successfully on 2026-09-13. It compiled the patch bundle, ran the Android-independent policy tests, installed `platforms;android-37.0` and Build Tools 37.0.0, and compiled the lifecycle fixture. The configured-fixture patching job was skipped because no immutable fixture URL or patch command has been configured. CI success is build evidence only; it does not establish device compatibility or full blocking.

## Required result matrix

Document focus, rotation, activities, split screen, PiP, lock, shortcut conflicts; expiry and blocked entry routes; settings access; playback, services, notifications (including delegated), jobs, alarms, native/isolated processes, custom loaders; reboot, process death, duplicate checkpoints, unavailable storage, snapshot disagreement; resets, DST, time zones, pending changes; and initialization, settings, package-renaming, and split-APK coexistence.

Surviving host work or alert after exhaustion is a full-blocking failure. Four successful apps would not establish universal compatibility.
