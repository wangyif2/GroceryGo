package ca.grocerygo.android.services.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationMonitor extends BroadcastReceiver {
	public static final String EXTRA_ERROR = "com.grocerygo.android.service.EXTRA_ERROR";
	public static final String EXTRA_INTENT = "com.grocerygo.android.service.EXTRA_INTENT";
	public static final String EXTRA_LOCATION = "com.grocerygo.android.service.EXTRA_LOCATION";
	public static final String EXTRA_PROVIDER = "com.grocerygo.android.service.EXTRA_PROVIDER";
	public static final String EXTRA_LASTKNOWN = "com.grocerygo.android.service.EXTRA_LASTKNOWN";
	/**
	 * If this is returned true (defaults to false unless
	 * provider is explicitly NOT enabled), then the provider
	 * could not be enabled.
	 */
	public static final String EXTRA_ERROR_PROVIDER_DISABLED = "com.grocerygo.android.service.EXTRA_ERROR_PROV_DISABLED";
	/**
	 * Optional Timeout. Pass milliseconds as a long. Defaults
	 * to 2 minutes.
	 */
	public static final String EXTRA_TIMEOUT = "com.grocerygo.android.service.EXTRA_TIMEOUT";

	/**
	 * Standard entry point for a BroadcastReceiver. Delegates
	 * the event to LocationMonitorService for processing.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		LocationMonitorService.requestLocation(context, intent);
	}
}
