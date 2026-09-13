package dev.screentime.runtime.core;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/** JVM-only regression tests for the Android-independent policy core. */
public final class PolicyEngineTest {
    private static final ZoneId DENVER = ZoneId.of("America/Denver");

    public static void main(String[] args) {
        enforcesAtExactBudget();
        warnsAtExactBudget();
        calculatesResetAcrossDayBoundary();
        distinguishesStrictAndRelaxedChanges();
        delaysRelaxationsForExactlyOneDay();
        splitsMixedPolicyChanges();
        recognizesOnlyTheRequiredShortcut();
    }

    private static void enforcesAtExactBudget() {
        Policy policy = new Policy(60_000, LocalTime.of(8, 0), PolicyMode.BLOCKING, 7);
        EnforcementSnapshot snapshot = PolicyEngine.evaluate(policy, 60_000, Instant.parse("2026-09-13T14:00:00Z"), DENVER);
        equal(EnforcementSnapshot.Decision.BLOCK, snapshot.decision(), "blocking exact budget");
        equal(7L, snapshot.policyRevision(), "revision preserved");
    }

    private static void warnsAtExactBudget() {
        Policy policy = new Policy(60_000, LocalTime.of(8, 0), PolicyMode.WARNING, 1);
        EnforcementSnapshot snapshot = PolicyEngine.evaluate(policy, 60_000, Instant.parse("2026-09-13T14:00:00Z"), DENVER);
        equal(EnforcementSnapshot.Decision.WARN, snapshot.decision(), "warning exact budget");
    }

    private static void calculatesResetAcrossDayBoundary() {
        Policy policy = new Policy(1, LocalTime.of(8, 0), PolicyMode.WARNING, 0);
        Instant before = Instant.parse("2026-09-13T13:59:00Z"); // 07:59 in Denver daylight time.
        equal(Instant.parse("2026-09-13T14:00:00Z"), PolicyEngine.nextReset(policy, before, DENVER), "same day reset");
        Instant at = Instant.parse("2026-09-13T14:00:00Z");
        equal(Instant.parse("2026-09-14T14:00:00Z"), PolicyEngine.nextReset(policy, at, DENVER), "next day at boundary");
    }

    private static void distinguishesStrictAndRelaxedChanges() {
        Policy current = new Policy(60_000, LocalTime.NOON, PolicyMode.WARNING, 1);
        check(PolicyEngine.isStricter(current, new Policy(59_000, LocalTime.NOON, PolicyMode.WARNING, 2)), "smaller budget strict");
        check(PolicyEngine.isStricter(current, new Policy(60_000, LocalTime.NOON, PolicyMode.BLOCKING, 2)), "blocking strict");
        check(!PolicyEngine.isStricter(current, new Policy(61_000, LocalTime.NOON, PolicyMode.WARNING, 2)), "larger budget relaxed");
    }

    private static void delaysRelaxationsForExactlyOneDay() {
        Instant requested = Instant.parse("2026-09-13T14:00:00Z");
        equal(Instant.parse("2026-09-14T14:00:00Z"), PolicyEngine.delayedActivation(requested), "24-hour delay");
    }

    private static void splitsMixedPolicyChanges() {
        Policy current = new Policy(60_000, LocalTime.of(8, 0), PolicyMode.WARNING, 4);
        Policy requested = new Policy(120_000, LocalTime.of(9, 0), PolicyMode.BLOCKING, 99);
        PolicyChangePlan plan = PolicyChangePlanner.plan(current, requested);
        equal(60_000L, plan.immediatePolicy().budgetMillis(), "budget relaxation deferred");
        equal(PolicyMode.BLOCKING, plan.immediatePolicy().mode(), "blocking immediate");
        equal(5L, plan.immediatePolicy().revision(), "immediate revision");
        equal(120_000L, plan.pendingPolicy().budgetMillis(), "pending requested budget");
        equal(LocalTime.of(9, 0), plan.pendingPolicy().resetTime(), "pending reset");
        equal(6L, plan.pendingPolicy().revision(), "pending revision");
    }

    private static void recognizesOnlyTheRequiredShortcut() {
        VolumeShortcut shortcut = new VolumeShortcut();
        check(!shortcut.accept(true, false, 0), "first up");
        check(!shortcut.accept(true, true, 10), "repeat ignored");
        check(!shortcut.accept(false, false, 50), "down");
        check(shortcut.accept(true, false, 100), "up-down-up completes");
        check(!shortcut.accept(true, false, 0), "new first up");
        check(!shortcut.accept(false, false, 2_001), "expired sequence cannot complete");
    }

    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void equal(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
    }
}
