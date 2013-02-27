package com.groceryotg.android.services.Location;

import android.location.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * User: John
 * Date: 2013-02-12
 */
public class LocationReceiver extends BroadcastReceiver {

    //currently polling time is every 5 minutes
    private static final int pollingPeriod = 300000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Location loc = (Location) intent.getExtras().get(LocationMonitor.EXTRA_LOCATION);
        String msg;

        if (loc == null) {
            msg = intent.getStringExtra(LocationMonitor.EXTRA_ERROR);
        } else {
            msg = loc.toString();
        }

        if (msg == null) {
            msg = "Invalid location received...";
        }

        Log.i("GroceryOTG", msg);
    }
}
