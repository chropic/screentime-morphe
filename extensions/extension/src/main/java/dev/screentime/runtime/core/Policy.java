package dev.screentime.runtime.core;

import java.time.LocalTime;
import java.util.Objects;

/** Immutable, versioned policy. Duration is stored as milliseconds, not wall-clock time. */
public final class Policy {
    public static final int CONTRACT_VERSION = 1;
    private final long budgetMillis;
    private final LocalTime resetTime;
    private final PolicyMode mode;
    private final long revision;

    public Policy(long budgetMillis, LocalTime resetTime, PolicyMode mode, long revision) {
        if (budgetMillis <= 0 || revision < 0) throw new IllegalArgumentException("invalid policy value");
        this.budgetMillis = budgetMillis;
        this.resetTime = Objects.requireNonNull(resetTime, "resetTime");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.revision = revision;
    }
    public long budgetMillis() { return budgetMillis; }
    public LocalTime resetTime() { return resetTime; }
    public PolicyMode mode() { return mode; }
    public long revision() { return revision; }
}
