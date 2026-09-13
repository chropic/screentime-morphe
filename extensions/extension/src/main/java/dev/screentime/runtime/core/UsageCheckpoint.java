package dev.screentime.runtime.core;

import java.util.Objects;

/** A database commit is idempotent by installation, session, and sequence. */
public final class UsageCheckpoint {
    public static final int CONTRACT_VERSION = 1;
    private final String installationId, sessionId;
    private final long sequence, elapsedUsageMillis;
    public UsageCheckpoint(String installationId, String sessionId, long sequence, long elapsedUsageMillis) {
        this.installationId = identifier(installationId, "installationId");
        this.sessionId = identifier(sessionId, "sessionId");
        if (sequence < 0 || elapsedUsageMillis < 0) throw new IllegalArgumentException("negative checkpoint value");
        this.sequence = sequence;
        this.elapsedUsageMillis = elapsedUsageMillis;
    }
    public String installationId() { return installationId; }
    public String sessionId() { return sessionId; }
    public long sequence() { return sequence; }
    public long elapsedUsageMillis() { return elapsedUsageMillis; }
    private static String identifier(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isEmpty()) throw new IllegalArgumentException(name + " must not be empty");
        return value;
    }
}
