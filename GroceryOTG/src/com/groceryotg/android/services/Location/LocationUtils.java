package com.groceryotg.android.services.Location;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import android.util.Log;

/**
 * User: robert
 * Date: 14/03/13
 */
public class LocationUtils {

    public static Cursor getStoreLocations(Context context) {
        String[] projection = {StoreParentTable.COLUMN_STORE_PARENT_NAME, StoreTable.COLUMN_STORE_LATITUDE, StoreTable.COLUMN_STORE_LONGITUDE};
        Cursor c = context.getContentResolver().query(GroceryotgProvider.CONTENT_URI_STO_JOIN_STOREPARENT, projection, null, null, null);
//        c.moveToFirst();
//        int columnNum = c.getColumnCount();
//        while (c.isAfterLast()) {
//            for (int i = 0; i < columnNum; i++) {
//                Log.i("GroceryOTG", c.getDouble(c.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE)));
//            }
//        }
//
        Log.i("GroceryOTG", DatabaseUtils.dumpCursorToString(c));

        return c;
    }

}
