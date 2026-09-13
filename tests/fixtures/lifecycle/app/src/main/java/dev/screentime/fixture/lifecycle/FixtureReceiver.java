package dev.screentime.fixture.lifecycle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class FixtureReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) { EventLog.record(context, "receiver:onReceive"); }
}
