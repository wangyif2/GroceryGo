package com.groceryotg.android.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

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

}
