package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * User: robert
 * Date: 25/03/13
 */
public class MyFlyerFragment extends GroceryListFragment {

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String emptyString = bundle.getBoolean("reload") ? buildNoSearchResultString() : buildNoNewContentString();
        String query = bundle.getString("query");
        displayEmptyListMessage(emptyString);

        List<String> selectionArgs = new ArrayList<String>();

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                CartTable.COLUMN_CART_GROCERY_ID,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST};

        //Select items that has watch ON and groceryId exist in GroceryId
        String selection = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_FLAG_WATCHLIST + "=?";
        selectionArgs.add(String.valueOf(1));

        // If user entered a search query, filter the results based on grocery name
        if (!query.isEmpty()) {
            selection += " AND " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + query + "%");
        }
        //Store filter
        if (GroceryFragmentActivity.storeSelected != null && GroceryFragmentActivity.storeSelected.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < GroceryFragmentActivity.storeSelected.size(); storeNum++) {
                if (GroceryFragmentActivity.storeSelected.valueAt(storeNum) == GroceryFragmentActivity.SELECTED) {
                    if (storeSelection.isEmpty()) {
                        storeSelection = " AND (";
                        storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    } else {
                        storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    }
                    selectionArgs.add(((Integer) GroceryFragmentActivity.storeSelected.keyAt(storeNum)).toString());
                }
            }
            if (!storeSelection.isEmpty()) {
                storeSelection += ")";
                selection += storeSelection;
            }
        }

        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);

        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        super.onLoaderReset(null);
    }
}
