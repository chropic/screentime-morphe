package dev.screentime.runtime;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import dev.screentime.runtime.core.EnforcementSnapshot;
import dev.screentime.runtime.core.Policy;
import dev.screentime.runtime.core.PolicyEngine;
import dev.screentime.runtime.core.PolicyChangePlan;
import dev.screentime.runtime.core.PolicyChangePlanner;
import dev.screentime.runtime.core.PolicyMode;
import dev.screentime.runtime.core.PendingPolicyChange;
import dev.screentime.runtime.core.UsageCheckpoint;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Non-exported, versioned control-process endpoint. Callers use call(), never direct table access.
 */
public final class StateProvider extends ContentProvider {
    public static final int CONTRACT_VERSION = 1;
    public static final String OP_READ_STATE = "read_state";
    public static final String OP_COMMIT_USAGE = "commit_usage";
    public static final String OP_REQUEST_POLICY_CHANGE = "request_policy_change";
    public static final String OP_CANCEL_POLICY_CHANGE = "cancel_policy_change";
    public static final String OP_OPEN_CONTROLS = "open_controls";
    public static final String OP_SET_INITIAL_POLICY = "set_initial_policy";
    public static final String KEY_BUDGET_MILLIS = "budget_millis";
    public static final String KEY_RESET_SECONDS = "reset_seconds";
    public static final String KEY_MODE = "mode";
    public static final String KEY_INSTALLATION_ID = "installation_id";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_SEQUENCE = "sequence";
    public static final String KEY_ELAPSED_USAGE_MILLIS = "elapsed_usage_millis";
    private ControlDatabase database;
    private SnapshotStore snapshots;

    @Override public boolean onCreate() {
        database = new ControlDatabase(getContext());
        snapshots = new SnapshotStore(getContext().getNoBackupFilesDir());
        return true;
    }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        if (OP_READ_STATE.equals(method)) return state();
        if (OP_COMMIT_USAGE.equals(method)) {
            Bundle input = requireExtras(extras);
            database.commit(new UsageCheckpoint(input.getString(KEY_INSTALLATION_ID), input.getString(KEY_SESSION_ID), input.getLong(KEY_SEQUENCE), input.getLong(KEY_ELAPSED_USAGE_MILLIS)), dayKey());
            return state();
        }
        if (OP_SET_INITIAL_POLICY.equals(method)) {
            database.setInitialPolicy(policy(requireExtras(extras), 0));
            return state();
        }
        if (OP_REQUEST_POLICY_CHANGE.equals(method)) {
            Policy current = requiredPolicy();
            PolicyChangePlan plan = PolicyChangePlanner.plan(current, policy(requireExtras(extras), current.revision()));
            if (plan.immediatePolicy() != null) database.replacePolicy(plan.immediatePolicy());
            if (plan.pendingPolicy() != null) database.replacePending(new PendingPolicyChange(plan.pendingPolicy(), PolicyEngine.delayedActivation(Instant.now())));
            return state();
        }
        if (OP_CANCEL_POLICY_CHANGE.equals(method)) { database.cancelPending(); return state(); }
        if (OP_OPEN_CONTROLS.equals(method)) {
            Intent intent = new Intent(getContext(), ScreenTimeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            return state();
        }
        throw new UnsupportedOperationException("Unsupported operation: " + method);
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Use ContentProvider.call");
    }
    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("Use ContentProvider.call"); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("Use ContentProvider.call"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { throw new UnsupportedOperationException("Use ContentProvider.call"); }

    private Bundle state() {
        database.activatePendingIfDue(Instant.now());
        Policy policy = database.readPolicy();
        Bundle result = new Bundle();
        if (policy == null) { result.putBoolean("configured", false); return result; }
        long consumed = database.usageFor(dayKey());
        EnforcementSnapshot snapshot = PolicyEngine.evaluate(policy, consumed, Instant.now(), ZoneId.systemDefault());
        try { snapshots.write(snapshot); } catch (IOException exception) { throw new IllegalStateException("Unable to write enforcement snapshot", exception); }
        PendingPolicyChange pending = database.readPending();
        result.putBoolean("configured", true); result.putString("decision", snapshot.decision().name()); result.putLong("consumed_millis", snapshot.consumedMillis()); result.putLong("next_reset_millis", snapshot.nextReset().toEpochMilli()); result.putLong("revision", snapshot.policyRevision());
        if (pending != null) { result.putLong("pending_activation_millis", pending.activateAt().toEpochMilli()); result.putLong("pending_budget_millis", pending.policy().budgetMillis()); result.putString("pending_mode", pending.policy().mode().name()); result.putLong("pending_reset_seconds", pending.policy().resetTime().toSecondOfDay()); }
        return result;
    }
    private static Bundle requireExtras(Bundle extras) { if (extras == null) throw new IllegalArgumentException("Missing operation data"); return extras; }
    private Policy requiredPolicy() { Policy policy = database.readPolicy(); if (policy == null) throw new IllegalStateException("An initial policy is required"); return policy; }
    private static Policy policy(Bundle values, long revision) { return new Policy(values.getLong(KEY_BUDGET_MILLIS), LocalTime.ofSecondOfDay(values.getLong(KEY_RESET_SECONDS)), PolicyMode.valueOf(values.getString(KEY_MODE)), revision); }
    private static String dayKey() { return LocalDate.now(ZoneId.systemDefault()).toString(); }
}
