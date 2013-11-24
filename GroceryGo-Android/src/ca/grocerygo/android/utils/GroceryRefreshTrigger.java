package ca.grocerygo.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import ca.grocerygo.android.services.NetworkHandler;

/**
 * User: robert
 * Date: 26/06/13
 */
public class GroceryRefreshTrigger {
    public static final String SETTINGS_IS_NEW_DATA_AVA = "isNewDataAvailable";

    public static void enableRefresh(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean(SETTINGS_IS_NEW_DATA_AVA, true);
        settingsEditor.commit();

        Bundle bundle = new Bundle();
        bundle.putBoolean(SETTINGS_IS_NEW_DATA_AVA, true);
        Intent localIntent = new Intent(NetworkHandler.REFRESH_COMPLETED_ACTION).putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    public static void refreshAll(Context context) {
        populateCategory(context);
        populateStoreParent(context);
        populateStore(context);
        populateFlyer(context);
        populateGrocery(context);
    }

    public static void stopAll(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        context.stopService(intent);
    }

    public static void populateCategory(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
        context.startService(intent);
    }

    public static void populateGrocery(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        context.startService(intent);
    }

    public static void populateStoreParent(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.STO_PAR);
        context.startService(intent);
    }

    public static void populateStore(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.STO);
        context.startService(intent);
    }

    public static void populateFlyer(Context context) {
        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.FLY);
        context.startService(intent);
    }

}
