package com.groceryotg.android.services.Location;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.groceryotg.android.CategoryOverView;
import com.groceryotg.android.R;

import java.util.Date;

/**
 * User: John
 * Date: 2013-02-12
 */
public class LocationReceiver extends BroadcastReceiver {
    //currently polling time is every 5 minutes
    public static final int pollingPeriod = 3600000;

    public static final int NOTIFICATION_LOCATION_ID = 0;

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

        Log.i("GroceryOTG", msg + " Time now is: " + new Date().getTime());

        // Now make a notification if there are nearby items in the cart
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon_drinks)
                        .setContentTitle("GroceryOTG")
                        .setContentText("An item in your cart is near")
                        .setAutoCancel(true);
        // Creates an explicit intent for the top activity that will be opened
        Intent resultIntent = new Intent(context, CategoryOverView.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity. This ensures that navigating backward from the 
        // Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CategoryOverView.class);
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
