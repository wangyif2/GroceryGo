package ca.grocerygo.android.services.location;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.SparseArray;
import ca.grocerygo.android.MapFragmentActivity;
import ca.grocerygo.android.R;
import ca.grocerygo.android.database.CartTable;
import ca.grocerygo.android.database.StoreTable;
import ca.grocerygo.android.settings.SettingsManager;
import ca.grocerygo.android.utils.GroceryGoUtils;
import ca.grocerygo.android.utils.GroceryStoreDistanceMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LocationReceiver extends BroadcastReceiver {
	// a near location is 1500m
	public static final int LOCATION_NEAR = 1500;
	public static final int NOTIFICATION_LOCATION_ID = 0;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Location loc = (Location) intent.getExtras().get(LocationMonitor.EXTRA_LOCATION);

		if (loc == null)
			return;
		
		constructNotification(context, loc);
		rebuildStoreDistances(context, loc);
	}
	
	private void rebuildStoreDistances(Context context, Location loc) {
		// Build a new store location map using the new location and reload the view
		SparseArray<Float> newDistanceMap = GroceryGoUtils.buildDistanceMap(context);
		GroceryStoreDistanceMap.setmStoreDistanceMap(newDistanceMap);
		GroceryGoUtils.reloadGroceryLocation(context);
	}
	
	private void constructNotification(Context context, Location loc) {
		Bundle extras = new Bundle();
		ArrayList<Integer> storeIDs = new ArrayList<Integer>(); // list of unique stores that are closeby
		Set<String> newEvents = new HashSet<String>();
		
		ArrayList<String> events = new ArrayList<String>(); // list of unique grocery names
		boolean displayNotification = false;
		
		Cursor stores = GroceryGoUtils.getGroceriesFromCartFromStores(context);
		stores.moveToFirst();
		while (!stores.isAfterLast()) {
			String name = stores.getString(stores.getColumnIndex(CartTable.COLUMN_CART_GROCERY_NAME));
			int storeID = stores.getInt(stores.getColumnIndex(StoreTable.COLUMN_STORE_ID));
			Location storeLoc = new Location("Store Location");
			storeLoc.setLatitude(stores.getDouble(stores.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE)));
			storeLoc.setLongitude(stores.getDouble(stores.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE)));
			
			// calculate the distance in meters between the current user location and the store's location
			float distance = loc.distanceTo(storeLoc);
			
			if (distance <= LOCATION_NEAR) {
				if (!events.contains(name))
					events.add(name);
				if (!storeIDs.contains(storeID))
					storeIDs.add(storeID);
				newEvents.add(name);
				displayNotification = true;
			}
			
			stores.moveToNext();
		}
		
		if (!displayNotification)
			return;
		
		// Check if the exact same set of items has been previously notified
		SharedPreferences prefs = SettingsManager.getPrefs(context);
		Set<String> oldEvents = prefs.getStringSet(SettingsManager.SETTINGS_PREVIOUS_NOTIFICATION, new HashSet<String>());
		if (oldEvents.equals(newEvents))
			return;
		
		Editor prefsEditor = prefs.edit();
		prefsEditor.putStringSet(SettingsManager.SETTINGS_PREVIOUS_NOTIFICATION, newEvents);
		prefsEditor.commit();
		
		extras.putIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE, storeIDs);
		
		// Now make a notification if there are nearby items in the cart
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_stat_notify_item)
						.setContentTitle(context.getString(R.string.app_name))
						.setContentText(context.getString(R.string.notification_content_text))
						.setAutoCancel(true);
		// Create a big notification
		NotificationCompat.InboxStyle inboxStyle =
				new NotificationCompat.InboxStyle();
		
		// Sets a title for the inbox style big view
		inboxStyle.setBigContentTitle(context.getString(R.string.notification_contect_big_title));
		// Moves events into the big view
		for (int i=0; i < events.size(); i++) {
			inboxStyle.addLine(events.get(i));
		}
		// Moves the big view style object into the notification object.
		mBuilder.setStyle(inboxStyle);
		
		// Creates an explicit intent for the top activity that will be opened (the map)
		Intent resultIntent = new Intent(context, MapFragmentActivity.class);
		resultIntent.putExtras(extras);

		// The stack builder object will contain an artificial back stack for the
		// started Activity. This ensures that navigating backward from the 
		// Activity leads out of your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MapFragmentActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Now set sounds and vibrations
		mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
			mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
		
		// the ID allows for updating the notification later on
		mNotificationManager.notify(NOTIFICATION_LOCATION_ID, mBuilder.build());
	}
}
