package dev.screentime.runtime.core;

/** Stateful recognizer for volume up, down, up. Repeat events are ignored by callers. */
public final class VolumeShortcut {
    public static final long WINDOW_MILLIS = 2_000L;
    private int state;
    private long startedAtMillis;
    public boolean accept(boolean volumeUp, boolean repeat, long elapsedRealtimeMillis) {
        if (repeat) return false;
        if (state != 0 && elapsedRealtimeMillis - startedAtMillis > WINDOW_MILLIS) state = 0;
        if (state == 0) { if (volumeUp) { state = 1; startedAtMillis = elapsedRealtimeMillis; } return false; }
        if (state == 1) { state = volumeUp ? 1 : 2; if (state == 1) startedAtMillis = elapsedRealtimeMillis; return false; }
        boolean completed = volumeUp; state = 0; return completed;
    }
}
