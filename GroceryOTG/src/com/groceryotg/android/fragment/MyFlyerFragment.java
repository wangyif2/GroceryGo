package com.groceryotg.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.View;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: robert
 * Date: 25/03/13
 */
public class MyFlyerFragment extends GroceryListFragment {

    private static final String MY_FLYER_NO_CONTENT = "Currently no recommendation for you, keep looking!";

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        isSearch = bundle.getBoolean("reload");
        String query = bundle.getString("query");

        List<String> selectionArgs = new ArrayList<String>();

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID,
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
        SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(activity);
        if (selectedStores != null && selectedStores.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < selectedStores.size(); storeNum++) {
                if (selectedStores.valueAt(storeNum) == true) {
                    if (storeSelection.isEmpty()) {
                        storeSelection = " AND (";
                        storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    } else {
                        storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    }
                    selectionArgs.add(((Integer) selectedStores.keyAt(storeNum)).toString());
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
        if (progressView != null)
            progressView.setVisibility(View.GONE);

        if (cursor.getCount() == 0)
            if (isSearch)
                displayEmptyListMessage(buildNoSearchResultString());
            else
                displayEmptyListMessage(buildNoNewContentString());

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        super.onLoaderReset(null);
    }

    @Override
    public String buildNoNewContentString() {
        return MY_FLYER_NO_CONTENT;
    }
}
