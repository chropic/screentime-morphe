package dev.screentime.runtime.core;

import java.util.Objects;

/** Applies restrictive fields at once and defers every relaxation/reset-schedule change. */
public final class PolicyChangePlanner {
    private PolicyChangePlanner() { }
    public static PolicyChangePlan plan(Policy current, Policy requested) {
        Objects.requireNonNull(current, "current"); Objects.requireNonNull(requested, "requested");
        boolean tighterBudget = requested.budgetMillis() < current.budgetMillis();
        boolean tighterMode = current.mode() == PolicyMode.WARNING && requested.mode() == PolicyMode.BLOCKING;
        boolean immediate = tighterBudget || tighterMode;
        Policy applied = immediate ? new Policy(
            tighterBudget ? requested.budgetMillis() : current.budgetMillis(),
            current.resetTime(),
            tighterMode ? PolicyMode.BLOCKING : current.mode(),
            current.revision() + 1) : null;
        Policy baseline = applied == null ? current : applied;
        boolean delay = requested.budgetMillis() > baseline.budgetMillis()
            || requested.mode() == PolicyMode.WARNING && baseline.mode() == PolicyMode.BLOCKING
            || !requested.resetTime().equals(baseline.resetTime());
        Policy pending = delay ? new Policy(requested.budgetMillis(), requested.resetTime(), requested.mode(), baseline.revision() + 1) : null;
        return new PolicyChangePlan(applied, pending);
    }
}
