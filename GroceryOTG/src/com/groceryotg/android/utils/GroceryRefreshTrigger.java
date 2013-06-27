package com.groceryotg.android.utils;

import android.content.Context;
import android.content.Intent;
import com.groceryotg.android.services.NetworkHandler;

/**
 * User: robert
 * Date: 26/06/13
 */
public class GroceryRefreshTrigger {

    public static void refreshAll(Context context) {
        populateCategory(context);
        populateStoreParent(context);
        populateStore(context);
        populateFlyer(context);
        populateGrocery(context);
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
