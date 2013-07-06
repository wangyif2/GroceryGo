package com.grocerygo.android.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.grocerygo.android.GroceryApplication;
import com.grocerygo.android.R;
import com.grocerygo.android.services.NetworkHandler;

/**
 * User: robert
 * Date: 21/06/13
 */
public class GroceryGCMBroadcastReceiver extends BroadcastReceiver {

    public static final String SETTINGS_IS_NEW_DATA_AVA = "isNewDataAvailable";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(GroceryApplication.TAG, context.getString(R.string.gcm_received));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean(SETTINGS_IS_NEW_DATA_AVA, true);
        settingsEditor.commit();

        Log.i(GroceryApplication.TAG, String.valueOf(settings.getBoolean(SETTINGS_IS_NEW_DATA_AVA, false)));


        Bundle bundle = new Bundle();
        bundle.putBoolean(SETTINGS_IS_NEW_DATA_AVA, true);
        Intent localIntent = new Intent(NetworkHandler.REFRESH_COMPLETED_ACTION).putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

}
