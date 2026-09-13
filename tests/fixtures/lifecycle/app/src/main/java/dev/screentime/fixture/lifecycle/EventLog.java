package dev.screentime.fixture.lifecycle;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

final class EventLog {
    private static final String KEY = "events";
    static void record(Context context, String event) {
        String prior = context.getSharedPreferences("fixture", Context.MODE_PRIVATE).getString(KEY, "");
        context.getSharedPreferences("fixture", Context.MODE_PRIVATE).edit().putString(KEY, prior + event + "\n").apply();
    }
    static String read(Context context) { return context.getSharedPreferences("fixture", Context.MODE_PRIVATE).getString(KEY, ""); }
}
