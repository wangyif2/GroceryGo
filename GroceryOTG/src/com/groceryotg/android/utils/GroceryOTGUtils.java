package com.groceryotg.android.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

import java.util.Set;

/**
 * User: robert
 * Date: 14/03/13
 */
public class GroceryOTGUtils {

    public static Cursor getStoreLocations(Context context) {
        String[] projection = {StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_ID,
        		StoreParentTable.TABLE_STORE_PARENT+"."+StoreParentTable.COLUMN_STORE_PARENT_ID,
        		StoreParentTable.TABLE_STORE_PARENT+"."+StoreParentTable.COLUMN_STORE_PARENT_NAME,
        		StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_LATITUDE,
        		StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO_JOIN_STOREPARENT, projection, null, null, null);
        return c;
    }
    
    public static Cursor getStoreFlyerIDs(Context context) {
    	String[] projection = {StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_ID,
    			StoreTable.TABLE_STORE+"."+StoreTable.COLUMN_STORE_FLYER};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO, projection, null, null, null);
        return c;
    }

    public static Cursor getStoreParentNamesCursor(Context context) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_ID, StoreParentTable.COLUMN_STORE_PARENT_NAME};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STOPARENT, projection, null, null, null);
        return c;
    }

    public static Cursor getGroceriesFromCartFromStores(Context context) {
        String[] projection = {CartTable.COLUMN_CART_GROCERY_ID, CartTable.COLUMN_CART_GROCERY_NAME, StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_STORE_LATITUDE, StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_CART_JOIN_STORE, projection, null, null, null);
        return c;
    }

    public static int getMaxGroceryId(Context context) {
        String[] projection = {"Max(" + GroceryTable.COLUMN_GROCERY_ID + ")"};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_GRO, projection, null, null, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    /**
     * Method copies the intent extras from the received intent to the intent
     * that will be dispatched.
     *
     * @param aReceived
     * @param aDispatch
     */
    public static void copyIntentData(Intent aReceived, Intent aDispatch) {
        Set<String> lKeys = aReceived.getExtras().keySet();

        for (String lKey : lKeys) {
            aDispatch.putExtra(lKey, aReceived.getStringExtra(lKey));
        }
    }
}
