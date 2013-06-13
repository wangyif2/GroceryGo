package com.groceryotg.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.GroceryMapActivity;
import com.groceryotg.android.ShopCartOverviewFragmentActivity;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.AboutDialogFragment;
import com.groceryotg.android.settings.SettingsActivity;

import java.util.Set;

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
    
    /*public static void registerSlidingMenu(final SlidingMenu slidingMenu, final Activity activity) {
        TableRow row;
        
        row = (TableRow) activity.findViewById(R.id.menu_row_1);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected Categories
				launchHomeActivity(activity);
			}
        });
        row = (TableRow) activity.findViewById(R.id.menu_row_2);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected Shopping Cart
				launchShopCartActivity(activity);
			}
        });
        row = (TableRow) activity.findViewById(R.id.menu_row_3);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected Map
                launchMapActivity(activity);
			}
        });
        row = (TableRow) activity.findViewById(R.id.menu_row_4);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected Sync
			}
        });
        row = (TableRow) activity.findViewById(R.id.menu_row_5);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected Settings
            	launchSettingsActivity(activity);
			}
        });
        row = (TableRow) activity.findViewById(R.id.menu_row_6);
        row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (slidingMenu.isMenuShowing())
        			slidingMenu.showContent();
				// Selected About
				launchAboutDialog(activity);
			}
        });
        
    }

    public static void launchHomeActivity(Activity activity) {
        Intent intent = new Intent(activity, GroceryFragmentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add an extra to tell the pager to return to the first page
        Bundle extras = new Bundle();
        extras.putInt(GroceryFragmentActivity.EXTRA_LAUNCH_PAGE, 0);
        intent.putExtras(extras);
        
        activity.startActivity(intent);
    }
    
    public static void launchMapActivity(Activity activity) {
        Intent intent = new Intent(activity, GroceryMapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
    
    public static void launchShopCartActivity(Activity activity) {
        Intent intent = new Intent(activity, ShopCartOverviewFragmentActivity.class);
        activity.startActivity(intent);
    }
    
    public static void launchSettingsActivity(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }
    
    public static void launchAboutDialog(Activity activity) {
    	AboutDialogFragment dialog = new AboutDialogFragment();
    	dialog.show(((SherlockFragmentActivity) activity).getSupportFragmentManager(), "about_dialog");
    }*/
}
