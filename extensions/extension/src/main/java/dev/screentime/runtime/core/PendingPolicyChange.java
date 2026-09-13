package dev.screentime.runtime.core;

import java.time.Instant;
import java.util.Objects;

/** A replaceable delayed relaxation. Replacing it establishes a new activation deadline. */
public final class PendingPolicyChange {
    public static final int CONTRACT_VERSION = 1;
    private final Policy policy;
    private final Instant activateAt;
    public PendingPolicyChange(Policy policy, Instant activateAt) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.activateAt = Objects.requireNonNull(activateAt, "activateAt");
    }
    public Policy policy() { return policy; }
    public Instant activateAt() { return activateAt; }
}
