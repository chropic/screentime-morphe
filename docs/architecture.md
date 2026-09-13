# Architecture

## Goal

Inject screen-time enforcement into patched APKs while retaining their package identity, SDK declarations, application class, and existing component factory. The design is a feasibility hypothesis for GrapheneOS 2026091000 / Android 17 (API 37), not a compatibility promise.

## Ownership and processes

`dev.screentime.runtime` is injected into each target. A non-exported control process owns the settings UI, blocking UI, SQLite database, and authoritative decisions. It must not initialize the host `Application`. Host processes read only a small atomically written `EnforcementSnapshot` during early startup. Later state operations cross a non-exported provider contract to the control process.

## Startup hypothesis

1. An injected, delegating `AppComponentFactory` receives component-creation and class-loader events.
2. It preserves the existing factory and normal host initialization if the snapshot permits use.
3. If blocked, it substitutes screen-time components and gates host activities, services, receivers, and providers.
4. At exhaustion, the control process persists the decision, opens its interface, signals instrumented processes to stop, and removes notifications.
5. At reset, later creation is permitted; stale host application objects are never revived.

Whether the factory approach works with custom loaders and each target's initialization is an explicit experiment.

The current implementation delegates through `ApplicationInfo.metaData` at the class-loader hook when Android supplies the injected original-factory value. It has no device evidence yet. The control provider owns private SQLite policy/checkpoint state and atomically writes a small snapshot after a state read or usage commit. Startup gating and host-work termination are not implemented.

## Contracts

All internal contracts are versioned: `Policy`, `UsageCheckpoint`, `PendingPolicyChange`, and `EnforcementSnapshot`. Provider operations are read state, commit usage, request/cancel a policy change, and open controls. Usage commits include installation/session identity and monotonic sequence; the database deduplicates them. Snapshot/database disagreement or storage failure must open a diagnostic blocking screen, never grant time.

## Dependency rules

`core` depends on no Android UI, storage, or networking API. Android runtime adapters depend on core; patches depend on the runtime contract, not host settings internals. No companion app, root API, server, or app-specific workaround enters this milestone.
