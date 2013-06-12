package com.groceryotg.android.services.location;

import com.groceryotg.android.settings.SettingsManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.SystemClock;

public class LocationServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	if (SettingsManager.getNotificationsEnabled(context)) {
	        AlarmManager locationAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        Intent locationIntent = new Intent(context, LocationMonitor.class);
	        locationIntent.putExtra(LocationMonitor.EXTRA_INTENT, new Intent(context, LocationReceiver.class));
	        locationIntent.putExtra(LocationMonitor.EXTRA_PROVIDER, LocationManager.NETWORK_PROVIDER);
	        PendingIntent locationPendingIntent = PendingIntent.getBroadcast(context, 0, locationIntent, 0);
	        locationAlarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), LocationReceiver.pollingPeriod, locationPendingIntent);
    	}
    }
}
