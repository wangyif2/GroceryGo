package com.groceryotg.android.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.GroceryListCursorAdapter;
import com.groceryotg.android.fragment.GroceryViewBinder;
import com.groceryotg.android.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private GroceryListCursorAdapter adapter;
    private Activity mActivity;
    
    private String mQuery = "";
	
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		this.setHasOptionsMenu(true);
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fillData();
    }
	
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.search_list, container, false);
    	return v;
    }

	public void refreshQuery(String newQuery) {
        Bundle b = new Bundle();
    	b.putBoolean("reload", true);
    	mQuery = newQuery;
    	getLoaderManager().restartLoader(0, b, this);
    }
	
	private void fillData() {
		String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                CartTable.COLUMN_CART_FLAG_SHOPLIST};
        int[] to = new int[]{R.id.grocery_row_id,
                R.id.grocery_row_label,
                R.id.grocery_row_details,
                R.id.grocery_row_price,
                R.id.grocery_row_store,
                R.id.grocery_row_in_shopcart};

        
        adapter = new GroceryListCursorAdapter(mActivity, R.layout.grocery_fragment_list_row, null, from, to);
        adapter.setViewBinder(new GroceryViewBinder());
        setListAdapter(adapter);
        
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
				                GroceryTable.COLUMN_GROCERY_ID,
				                GroceryTable.COLUMN_GROCERY_NAME,
				                GroceryTable.COLUMN_GROCERY_PRICE,
				                StoreParentTable.COLUMN_STORE_PARENT_NAME,
				                CartTable.COLUMN_CART_GROCERY_ID,
				                CartTable.COLUMN_CART_FLAG_SHOPLIST};
    	
    	List<String> selectionArgs = new ArrayList<String>();
    	String selection = "";
    	
    	// If user entered a search query, filter the results based on grocery name
        if (!mQuery.isEmpty()) {
            selection += GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + mQuery + "%");
        }
        
        // Filter by store based on shared preferences
        SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(mActivity);
        if (selectedStores != null && selectedStores.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < selectedStores.size(); storeNum++) {
                if (selectedStores.valueAt(storeNum) == true) {
                    if (storeSelection.isEmpty()) {
                        storeSelection = "(";
                        storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    } else {
                        storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    }
                    selectionArgs.add(((Integer) selectedStores.keyAt(storeNum)).toString());
                }
            }
            if (!storeSelection.isEmpty()) {
                storeSelection += ")";
                selection += (selection.isEmpty() ? "" : " AND ");
                selection += storeSelection;
            }
        }
        
        // Order by most relevant item first
        String sortOrder = GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_SCORE;
        
        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        return new CursorLoader(mActivity, GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        
        // Fill in the number of results in the top bar
        int numResults = adapter.getCount();
        TextView tv_numResults = (TextView) mActivity.findViewById(R.id.search_num_results);
        tv_numResults.setText(((Integer)numResults).toString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
