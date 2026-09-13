# Integration validation

For each result: build the fixture/target; hash it; patch the reference baseline; install and test; patch the identical input with Screen time; repeat the case; capture toolchain/device versions and evidence; then append a pass, fail, or untested result to `docs/compatibility-report.md`. Never mark an unavailable test as passing.

## Lifecycle fixture procedure

1. Build `tests/fixtures/lifecycle` with the command in its README and record the resulting APK SHA-256.
2. Install and exercise the unpatched fixture: second activity, PiP, foreground-service start/stop, receiver, and provider startup. Preserve `logcat`, notifications, and event log evidence.
3. Patch the identical APK with only **Screen time** selected. Record the bundle/tool versions and patched APK hash.
4. Repeat the baseline cases, then set a one-second blocking policy and measure exhaustion, controls access, each component restart path, and notification behavior.
5. Mark every unimplemented or unavailable behavior as untested or failed in the compatibility report.
