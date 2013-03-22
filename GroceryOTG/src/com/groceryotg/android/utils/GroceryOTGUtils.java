package com.groceryotg.android.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * User: robert
 * Date: 14/03/13
 */
public class GroceryOTGUtils {

    public static Cursor getStoreLocations(Context context) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_NAME, StoreTable.COLUMN_STORE_LATITUDE, StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO_JOIN_STOREPARENT, projection, null, null, null);
        return c;
    }

    public static Cursor getStoreParentNames(Context context) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_ID, StoreParentTable.COLUMN_STORE_PARENT_NAME};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STOPARENT, projection, null, null, null);
        return c;
    }

    public static Cursor getGroceriesFromCartFromStores(Context context) {
        String[] projection = {CartTable.COLUMN_CART_GROCERY_ID, CartTable.COLUMN_CART_GROCERY_NAME, StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_STORE_LATITUDE, StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_CART_JOIN_STORE, projection, null, null, null);
        return c;
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

    public static int getMaxUpdateDate(Context context) {
        String[] projection = {"Max(" + GroceryTable.COLUMN_GROCERY_UPDATE + ")"};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_GRO, projection, null, null, null);

        Log.i("GroceryOTG", DatabaseUtils.dumpCursorToString(c));

//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Log.i("GroceryOTG", "max server time is: " + dateFormat.parse("2013-03-21 08:00:02").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
