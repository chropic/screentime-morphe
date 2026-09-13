# Fixtures

Fixtures are source-built Android apps that isolate lifecycle, notifications, media, process, and custom-initialization behavior. Each fixture must document its build command, package/version, expected events, APK SHA-256, and supported test cases before use.

| Fixture | Build command | Coverage |
| --- | --- | --- |
| `lifecycle` | `../../../gradlew -p tests/fixtures/lifecycle :app:assembleDebug` | Custom `Application` and `AppComponentFactory`, two activities, PiP, foreground-service notification in `:fixture_aux`, receiver, provider, and observable lifecycle events. |

The fixture uses AGP 9.4.0 / compile SDK 37, which supports API 37. Record the generated APK SHA-256 in the compatibility report before patching.
