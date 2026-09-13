# 0001 — Retain a patches-only feasibility boundary

Status: accepted for the prototype; production direction pending review.

## Verified facts

- The repository starts from the official Morphe patches template.
- The patcher supports extension DEX merging and standard-DOM manifest resource edits.
- The policy core compiles and passes its JVM calculation tests.

## Hypotheses

- `AppComponentFactory.instantiateClassLoader` receives the injected `ApplicationInfo` metadata early enough to create and preserve an original factory.
- A non-exported provider/activity in `:screentime_control` can be used without host application initialization.
- General instrumentation can stop host work and suppress notifications after exhaustion.

## Rejected for this milestone

Root, privileged APIs, companion applications, servers, and automatic target-specific adapters are out of scope. A visual overlay does not count as full blocking.

## Consequence

No release or compatibility claim is authorized. Device/fixture evidence must decide whether to keep general patches-only enforcement, add targeted adapters, or reconsider external enforcement.
