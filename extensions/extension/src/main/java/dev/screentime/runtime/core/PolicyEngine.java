package dev.screentime.runtime.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/** Deterministic policy calculation. Callers supply time and persistence; this class performs neither. */
public final class PolicyEngine {
    public static final long CHANGE_DELAY_MILLIS = 24L * 60L * 60L * 1000L;
    private PolicyEngine() { }
    public static EnforcementSnapshot evaluate(Policy policy, long consumedMillis, Instant now, ZoneId zone) {
        Objects.requireNonNull(policy, "policy"); Objects.requireNonNull(now, "now"); Objects.requireNonNull(zone, "zone");
        if (consumedMillis < 0) throw new IllegalArgumentException("consumedMillis must not be negative");
        EnforcementSnapshot.Decision decision = consumedMillis < policy.budgetMillis() ? EnforcementSnapshot.Decision.ALLOW
            : policy.mode() == PolicyMode.BLOCKING ? EnforcementSnapshot.Decision.BLOCK : EnforcementSnapshot.Decision.WARN;
        return new EnforcementSnapshot(decision, consumedMillis, nextReset(policy, now, zone), policy.revision());
    }
    public static Instant nextReset(Policy policy, Instant now, ZoneId zone) {
        ZonedDateTime localNow = now.atZone(zone);
        ZonedDateTime candidate = ZonedDateTime.of(localNow.toLocalDate(), policy.resetTime(), zone);
        if (!candidate.isAfter(localNow)) candidate = ZonedDateTime.of(LocalDate.from(localNow).plusDays(1), policy.resetTime(), zone);
        return candidate.toInstant();
    }
    public static boolean isStricter(Policy current, Policy requested) {
        return requested.budgetMillis() < current.budgetMillis()
            || current.mode() == PolicyMode.WARNING && requested.mode() == PolicyMode.BLOCKING;
    }
    public static Instant delayedActivation(Instant requestedAt) { return Objects.requireNonNull(requestedAt, "requestedAt").plusMillis(CHANGE_DELAY_MILLIS); }
}
