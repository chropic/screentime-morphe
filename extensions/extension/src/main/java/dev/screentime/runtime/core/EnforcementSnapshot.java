package dev.screentime.runtime.core;

import java.time.Instant;
import java.util.Objects;

/** Small startup-safe projection; SQLite remains authoritative after process startup. */
public final class EnforcementSnapshot {
    public static final int CONTRACT_VERSION = 1;
    public enum Decision { ALLOW, WARN, BLOCK, DIAGNOSTIC_BLOCK }
    private final Decision decision;
    private final long consumedMillis, policyRevision;
    private final Instant nextReset;
    public EnforcementSnapshot(Decision decision, long consumedMillis, Instant nextReset, long policyRevision) {
        this.decision = Objects.requireNonNull(decision, "decision");
        if (consumedMillis < 0 || policyRevision < 0) throw new IllegalArgumentException("negative snapshot value");
        this.consumedMillis = consumedMillis;
        this.nextReset = Objects.requireNonNull(nextReset, "nextReset");
        this.policyRevision = policyRevision;
    }
    public Decision decision() { return decision; }
    public long consumedMillis() { return consumedMillis; }
    public Instant nextReset() { return nextReset; }
    public long policyRevision() { return policyRevision; }
}
