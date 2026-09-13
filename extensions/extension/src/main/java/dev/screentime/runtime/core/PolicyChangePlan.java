package dev.screentime.runtime.core;

/** Result of splitting one request into immediate stricter enforcement and a delayed relaxation. */
public final class PolicyChangePlan {
    private final Policy immediatePolicy;
    private final Policy pendingPolicy;
    PolicyChangePlan(Policy immediatePolicy, Policy pendingPolicy) { this.immediatePolicy = immediatePolicy; this.pendingPolicy = pendingPolicy; }
    public Policy immediatePolicy() { return immediatePolicy; }
    public Policy pendingPolicy() { return pendingPolicy; }
}
