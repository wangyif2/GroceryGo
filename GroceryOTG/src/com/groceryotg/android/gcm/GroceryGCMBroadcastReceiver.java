package com.groceryotg.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.groceryotg.android.GroceryApplication;

/**
 * User: robert
 * Date: 21/06/13
 */
public class GroceryGCMBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(GroceryApplication.TAG, intent.getExtras().toString());
    }
}
