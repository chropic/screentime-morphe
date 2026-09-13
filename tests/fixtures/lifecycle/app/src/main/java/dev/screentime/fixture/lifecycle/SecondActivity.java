package dev.screentime.fixture.lifecycle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public final class SecondActivity extends Activity {
    @Override protected void onCreate(Bundle state) { super.onCreate(state); EventLog.record(this, "second:onCreate"); TextView text = new TextView(this); text.setText("Second activity"); setContentView(text); }
    @Override protected void onResume() { super.onResume(); EventLog.record(this, "second:onResume"); }
}
