package dev.screentime.fixture.lifecycle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public final class FixtureForegroundService extends Service {
    private static final String CHANNEL = "fixture";
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        EventLog.record(this, "service:onStartCommand");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL, "Fixture", NotificationManager.IMPORTANCE_DEFAULT));
        Notification notification = new Notification.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Fixture foreground service").build();
        startForeground(100, notification);
        return START_STICKY;
    }
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() { EventLog.record(this, "service:onDestroy"); super.onDestroy(); }
}
