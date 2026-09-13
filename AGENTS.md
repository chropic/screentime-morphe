# Screen-time patches contributor guide

## Reading order

1. `docs/architecture.md`
2. `docs/behavior.md`
3. `docs/development.md`
4. `docs/compatibility-report.md`
5. `docs/decisions/README.md`
6. `docs/sync-roadmap.md`

## Commands

- Build the bundle: `./gradlew buildAndroid`
- Build and generate the patch list: `./gradlew generatePatchesList`
- Build fixture APKs: each fixture declares its command in `tests/fixtures/README.md`.
- Run repeatable validation: follow `tests/integration/README.md`.

Do not manually edit generated release files: `patches-list.json`, `patches-bundle.json`, or `CHANGELOG.md`. Preserve the template release workflow.

## Module boundaries

- `patches/`: Kotlin patch definitions, manifest changes, instrumentation, diagnostics.
- `extensions/runtime/`: injected Java runtime. The template extension module remains the bundle packaging entry point until runtime wiring is implemented.
- Runtime `core`: Android-independent policy calculations; clocks are injected.
- `tests/fixtures/`: source-built Android test apps.
- `tests/integration/`: reproducible patch and device procedures.
- `docs/`: specifications, decisions, and evidence.

Use `dev.screentime`; retain upstream notices and record copied-code provenance. Expose one **Screen time** patch with internal dependencies. Never couple policy to a host app's settings framework.

## Agreed requirements

Screen-time policy requires an explicit initial budget, reset time, and warning/blocking choice. Usage is focused, unlocked host-app time only; monotonic time is authoritative. SQLite in the dedicated control process is authoritative, and an atomically written snapshot supports early process gates. Settings remain available while blocked. Full blocking means host interaction, notifications, playback, and background work are tested—not merely hidden behind an overlay.

## Feasibility stop condition

Do not present this as production-ready, claim untested compatibility, add privileged APIs, or advance beyond feasibility after the experiments. Update documentation for each behavioral or architectural change. Stop at the review deliverable: measured results, failures, and a user decision between general patches-only design, targeted adapters, or external enforcement.
