package dev.screentime.fixture.lifecycle;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private TextView events;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); EventLog.record(this, "main:onCreate");
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL);
        add(layout, "Open second activity", view -> startActivity(new Intent(this, SecondActivity.class)));
        add(layout, "Start foreground service", view -> startForegroundService(new Intent(this, FixtureForegroundService.class)));
        add(layout, "Stop foreground service", view -> stopService(new Intent(this, FixtureForegroundService.class)));
        add(layout, "Send receiver event", view -> sendBroadcast(new Intent("dev.screentime.fixture.lifecycle.EVENT").setPackage(getPackageName())));
        add(layout, "Enter picture-in-picture", view -> enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16, 9)).build()));
        events = new TextView(this); layout.addView(events); setContentView(layout); refresh();
    }
    @Override protected void onResume() { super.onResume(); EventLog.record(this, "main:onResume"); if (events != null) refresh(); }
    @Override protected void onPause() { EventLog.record(this, "main:onPause"); super.onPause(); }
    private void add(LinearLayout layout, String label, View.OnClickListener action) { Button button = new Button(this); button.setText(label); button.setOnClickListener(action); layout.addView(button); }
    private void refresh() { events.setText(EventLog.read(this)); }
}
