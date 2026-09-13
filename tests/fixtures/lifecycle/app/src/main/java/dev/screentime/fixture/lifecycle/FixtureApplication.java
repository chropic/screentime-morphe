package dev.screentime.fixture.lifecycle;

import android.app.Application;
import android.util.Log;

public final class FixtureApplication extends Application {
    @Override public void onCreate() { super.onCreate(); EventLog.record(this, "application:onCreate"); }
}
