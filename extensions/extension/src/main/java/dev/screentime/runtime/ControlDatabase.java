package dev.screentime.runtime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import dev.screentime.runtime.core.Policy;
import dev.screentime.runtime.core.PolicyMode;
import dev.screentime.runtime.core.PendingPolicyChange;
import dev.screentime.runtime.core.UsageCheckpoint;
import java.time.LocalTime;
import java.time.Instant;

/** Private control-process database. All mutating methods are transactionally idempotent. */
final class ControlDatabase extends SQLiteOpenHelper {
    private static final String NAME = "screentime-control.db";
    private static final int VERSION = 1;

    ControlDatabase(Context context) { super(context, NAME, null, VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE policy_state (id INTEGER PRIMARY KEY CHECK(id=1), budget_ms INTEGER NOT NULL, reset_seconds INTEGER NOT NULL, mode TEXT NOT NULL, revision INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE usage_checkpoint (installation_id TEXT NOT NULL, session_id TEXT NOT NULL, sequence INTEGER NOT NULL, day_key TEXT NOT NULL, elapsed_ms INTEGER NOT NULL, PRIMARY KEY(installation_id, session_id, sequence))");
        db.execSQL("CREATE TABLE daily_usage (day_key TEXT PRIMARY KEY, consumed_ms INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE pending_policy (id INTEGER PRIMARY KEY CHECK(id=1), budget_ms INTEGER NOT NULL, reset_seconds INTEGER NOT NULL, mode TEXT NOT NULL, revision INTEGER NOT NULL, activate_at_ms INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { throw new IllegalStateException("No migrations defined"); }

    Policy readPolicy() {
        try (Cursor cursor = getReadableDatabase().query("policy_state", null, "id=1", null, null, null, null)) {
            return cursor.moveToFirst() ? policy(cursor) : null;
        }
    }

    void setInitialPolicy(Policy policy) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (readPolicyLocked(db) != null) throw new IllegalStateException("Initial policy already exists");
            db.insertOrThrow("policy_state", null, values(policy));
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    /** Returns false when this exact installation/session/sequence was already committed. */
    boolean commit(UsageCheckpoint checkpoint, String dayKey) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            try (Cursor existing = db.query("usage_checkpoint", new String[] { "sequence" }, "installation_id=? AND session_id=? AND sequence=?", new String[] { checkpoint.installationId(), checkpoint.sessionId(), String.valueOf(checkpoint.sequence()) }, null, null, null)) {
                if (existing.moveToFirst()) return false;
            }
            long previous = 0;
            try (Cursor cursor = db.rawQuery("SELECT elapsed_ms FROM usage_checkpoint WHERE installation_id=? AND session_id=? ORDER BY sequence DESC LIMIT 1", new String[] { checkpoint.installationId(), checkpoint.sessionId() })) {
                if (cursor.moveToFirst()) previous = cursor.getLong(0);
            }
            long delta = Math.max(0, checkpoint.elapsedUsageMillis() - previous);
            ContentValues checkpointValues = new ContentValues();
            checkpointValues.put("installation_id", checkpoint.installationId()); checkpointValues.put("session_id", checkpoint.sessionId());
            checkpointValues.put("sequence", checkpoint.sequence()); checkpointValues.put("day_key", dayKey); checkpointValues.put("elapsed_ms", checkpoint.elapsedUsageMillis());
            db.insertOrThrow("usage_checkpoint", null, checkpointValues);
            addDailyUsage(db, dayKey, delta);
            db.setTransactionSuccessful();
            return true;
        } finally { db.endTransaction(); }
    }

    long usageFor(String dayKey) {
        try (Cursor cursor = getReadableDatabase().query("daily_usage", new String[] { "consumed_ms" }, "day_key=?", new String[] { dayKey }, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0;
        }
    }

    void replacePolicy(Policy policy) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try { db.update("policy_state", values(policy), "id=1", null); db.setTransactionSuccessful(); } finally { db.endTransaction(); }
    }

    PendingPolicyChange readPending() {
        try (Cursor cursor = getReadableDatabase().query("pending_policy", null, "id=1", null, null, null, null)) {
            if (!cursor.moveToFirst()) return null;
            return new PendingPolicyChange(policy(cursor), Instant.ofEpochMilli(cursor.getLong(cursor.getColumnIndexOrThrow("activate_at_ms"))));
        }
    }

    void replacePending(PendingPolicyChange pending) {
        ContentValues values = values(pending.policy()); values.put("activate_at_ms", pending.activateAt().toEpochMilli());
        getWritableDatabase().insertWithOnConflict("pending_policy", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    void cancelPending() { getWritableDatabase().delete("pending_policy", "id=1", null); }

    void activatePendingIfDue(Instant now) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            PendingPolicyChange pending = readPendingLocked(db);
            if (pending != null && !pending.activateAt().isAfter(now)) { db.update("policy_state", values(pending.policy()), "id=1", null); db.delete("pending_policy", "id=1", null); }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    private static void addDailyUsage(SQLiteDatabase db, String dayKey, long delta) {
        try (Cursor cursor = db.query("daily_usage", new String[] { "consumed_ms" }, "day_key=?", new String[] { dayKey }, null, null, null)) {
            ContentValues values = new ContentValues();
            if (cursor.moveToFirst()) {
                values.put("consumed_ms", cursor.getLong(0) + delta);
                db.update("daily_usage", values, "day_key=?", new String[] { dayKey });
            } else {
                values.put("day_key", dayKey); values.put("consumed_ms", delta);
                db.insertOrThrow("daily_usage", null, values);
            }
        }
    }

    private Policy readPolicyLocked(SQLiteDatabase db) {
        try (Cursor cursor = db.query("policy_state", null, "id=1", null, null, null, null)) { return cursor.moveToFirst() ? policy(cursor) : null; }
    }
    private PendingPolicyChange readPendingLocked(SQLiteDatabase db) {
        try (Cursor cursor = db.query("pending_policy", null, "id=1", null, null, null, null)) {
            return cursor.moveToFirst() ? new PendingPolicyChange(policy(cursor), Instant.ofEpochMilli(cursor.getLong(cursor.getColumnIndexOrThrow("activate_at_ms")))) : null;
        }
    }
    private static Policy policy(Cursor cursor) {
        return new Policy(cursor.getLong(cursor.getColumnIndexOrThrow("budget_ms")), LocalTime.ofSecondOfDay(cursor.getLong(cursor.getColumnIndexOrThrow("reset_seconds"))), PolicyMode.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("mode"))), cursor.getLong(cursor.getColumnIndexOrThrow("revision")));
    }
    private static ContentValues values(Policy policy) {
        ContentValues values = new ContentValues(); values.put("id", 1); values.put("budget_ms", policy.budgetMillis()); values.put("reset_seconds", policy.resetTime().toSecondOfDay()); values.put("mode", policy.mode().name()); values.put("revision", policy.revision()); return values;
    }
}
