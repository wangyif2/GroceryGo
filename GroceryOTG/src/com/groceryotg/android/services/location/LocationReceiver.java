package com.groceryotg.android.services.location;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.groceryotg.android.GroceryMapView;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: John
 * Date: 2013-02-12
 */
public class LocationReceiver extends BroadcastReceiver {
    //currently polling time is every 1 hour
    public static final int pollingPeriod = 60*60*1000;
    // a near location is 500m
    //public static int LOCATION_NEAR = 500;
    public static int LOCATION_NEAR = 50000000;

    public static final int NOTIFICATION_LOCATION_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Location loc = (Location) intent.getExtras().get(LocationMonitor.EXTRA_LOCATION);

        if (loc == null)
            return;
        
        constructNotification(context, loc);
    }
    
    private void constructNotification(Context context, Location loc) {
    	Bundle extras = new Bundle();
    	ArrayList<Integer> storeIDs = new ArrayList<Integer>();
    	
    	ArrayList<String> events = new ArrayList<String>();
    	boolean displayNotification = false;
    	
    	Cursor stores = GroceryOTGUtils.getGroceriesFromCartFromStores(context);
    	stores.moveToFirst();
    	while (!stores.isAfterLast()) {
        	String name = stores.getString(stores.getColumnIndex(CartTable.COLUMN_CART_GROCERY_NAME));
            int id = stores.getInt(stores.getColumnIndex(StoreTable.COLUMN_STORE_ID));
            Location storeLoc = new Location("Store Location");
            storeLoc.setLatitude(stores.getDouble(stores.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE)));
            storeLoc.setLongitude(stores.getDouble(stores.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE)));
            
            // calculate the distance in meters between the current user location and the store's location
            float distance = loc.distanceTo(storeLoc);
            
            if (distance <= LOCATION_NEAR) {
            	events.add(name);
            	if (!storeIDs.contains(id))
            		storeIDs.add(id);
            	displayNotification = true;
            }
            
            stores.moveToNext();
        }
    	
    	if (!displayNotification)
    		return;
    	
    	extras.putIntegerArrayList(GroceryMapView.EXTRA_FILTER_STORE, storeIDs);
        
    	// Now make a notification if there are nearby items in the cart
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GroceryOTG")
                        .setContentText("An item in your cart is near")
                        .setAutoCancel(true);
        // Create a big notification
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        
        // Sets a title for the inbox style big view
        inboxStyle.setBigContentTitle("Items on the go:");
        // Moves events into the big view
        for (int i=0; i < events.size(); i++) {
            inboxStyle.addLine(events.get(i));
        }
        // Moves the big view style object into the notification object.
        mBuilder.setStyle(inboxStyle);
        
        // Creates an explicit intent for the top activity that will be opened (the map)
        Intent resultIntent = new Intent(context, GroceryMapView.class);
        resultIntent.putExtras(extras);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the 
        // Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(GroceryMapView.class);
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
        // the ID allows for updating the notification later on
        mNotificationManager.notify(NOTIFICATION_LOCATION_ID, mBuilder.build());
    }
}
