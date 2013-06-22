package com.groceryotg.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.groceryotg.android.GroceryApplication;
import com.groceryotg.android.services.NetworkHandler;

/**
 * User: robert
 * Date: 21/06/13
 */
public class GroceryGCMBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(GroceryApplication.TAG, intent.getExtras().toString());

        populateGrocery(context);
    }

    private void populateGrocery(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        context.startService(intent);
    }
}
