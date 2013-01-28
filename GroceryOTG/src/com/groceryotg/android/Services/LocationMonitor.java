package com.groceryotg.android.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * User: robert
 * Date: 25/01/13
 */
public class LocationMonitor extends BroadcastReceiver {
    public static final String EXTRA_ERROR = "com.groceryotg.android.service.EXTRA_ERROR";
    public static final String EXTRA_INTENT = "com.groceryotg.android.service.EXTRA_INTENT";
    public static final String EXTRA_LOCATION = "com.groceryotg.android.service.EXTRA_LOCATION";
    public static final String EXTRA_PROVIDER = "com.groceryotg.android.service.EXTRA_PROVIDER";
    public static final String EXTRA_LASTKNOWN = "com.groceryotg.android.service.EXTRA_LASTKNOWN";
    /**
     * If this is returned true (defaults to false unless
     * provider is explicitly NOT enabled), then the provider
     * could not be enabled.
     */
    public static final String EXTRA_ERROR_PROVIDER_DISABLED = "com.groceryotg.android.service.EXTRA_ERROR_PROV_DISABLED";
    /**
     * Optional Timeout. Pass milliseconds as a long. Defaults
     * to 2 minutes.
     */
    public static final String EXTRA_TIMEOUT = "com.groceryotg.android.service.EXTRA_TIMEOUT";

    /**
     * Standard entry point for a BroadcastReceiver. Delegates
     * the event to LocationMonitorService for processing.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationMonitorService.requestLocation(context, intent);
    }
}
