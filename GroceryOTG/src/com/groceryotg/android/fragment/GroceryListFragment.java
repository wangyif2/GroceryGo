package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.*;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.settings.SettingsManager;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.List;

public class GroceryListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String CATEGORY_POSITION = "position";
    private static final int GLOBAL_SEARCH_CATEGORY = -1;
    Activity mActivity;
    
    GroceryListCursorAdapter adapter;
    String mQuery = "";
    
    TextView emptyTextView;
    ProgressBar progressView;
    Menu menu;
    MenuItem refreshItem;
    private Integer categoryId = GroceryListFragment.GLOBAL_SEARCH_CATEGORY;

    ViewGroup myViewGroup;

    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    public static GroceryListFragment newInstance(int pos) {
        GroceryListFragment f = new GroceryListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(CATEGORY_POSITION, pos);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        	categoryId = getArguments().getInt(CATEGORY_POSITION);
        }
        
        watchSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myViewGroup = container;
        View v = inflater.inflate(R.layout.grocery_fragment_list, container, false);
        emptyTextView = (TextView) v.findViewById(R.id.empty_grocery_list);
        progressView = (ProgressBar) v.findViewById(R.id.refresh_progress);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (progressView != null)
            progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                FlyerTable.COLUMN_FLYER_ID,
                FlyerTable.COLUMN_FLYER_URL,
                CartTable.COLUMN_CART_FLAG_SHOPLIST};
        int[] to = new int[]{R.id.grocery_row_id,
                R.id.grocery_row_label,
                R.id.grocery_row_details,
                R.id.grocery_row_price,
                R.id.grocery_row_store,
                R.id.grocery_row_store_id,
                R.id.grocery_row_flyer_url,
                R.id.grocery_row_in_shopcart};

        adapter = new GroceryListCursorAdapter(getActivity(), R.layout.grocery_fragment_list_row, null, from, to);
        adapter.setViewBinder(new GroceryViewBinder());

        setListAdapter(new SlideExpandableListAdapter(adapter, R.id.expandable_toggle_button, R.id.expandable));
        
        // Handles the search filter
        Bundle args = mActivity.getIntent().getExtras();
        if (args != null) {
	        if (args.containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH)) {
				// Update the query - this is used by the loader when fetching results from database
				mQuery = args.getString(SearchManager.QUERY).trim();
	        }
        }
        
        this.loadDataWithQuery(false, mQuery);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v.findViewById(R.id.grocery_row_label);
        TextView idView = (TextView) v.findViewById(R.id.grocery_row_id);

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_ID, idView.getText().toString());
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, textView.getText().toString());
        values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
        values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

        getActivity().getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);

        Bundle b = new Bundle();
        b.putString("query", "");
        b.putBoolean("reload", true);
        getLoaderManager().restartLoader(0, b, this);
        Toast t = Toast.makeText(getActivity(), "Item added to Shopping Cart", Toast.LENGTH_SHORT);
        t.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String query = bundle.getString("query").trim();

        List<String> selectionArgs = new ArrayList<String>();
        boolean isAtLeastOneWhere = false;

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID,
                FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_URL,
                CartTable.COLUMN_CART_GROCERY_ID,
                CartTable.COLUMN_CART_FLAG_SHOPLIST};
        
        String selection;
        if (categoryId == GroceryListFragment.GLOBAL_SEARCH_CATEGORY) {
        	selection = "";
        } else {
        	if (!isAtLeastOneWhere) {
        		isAtLeastOneWhere = true;
        	}
	        selection = GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
	        selectionArgs.add(categoryId.toString());
        }

        // If user entered a search query, filter the results based on grocery name
        if (!query.isEmpty()) {
        	if (!isAtLeastOneWhere) {
        		isAtLeastOneWhere = true;
        	} else {
        		selection += " AND ";
        	}
            selection += GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + query + "%");
        }
        SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(mActivity);
        if (selectedStores != null && selectedStores.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < selectedStores.size(); storeNum++) {
                if (selectedStores.valueAt(storeNum) == true) {
                    if (storeSelection.isEmpty()) {
                    	if (!isAtLeastOneWhere) {
                    		isAtLeastOneWhere = true;
                    	} else {
                    		selection += " AND ";
                    	}
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
                selection += storeSelection;
            }
        }
        if (CategoryTopFragmentActivity.mPriceRangeMin != null) {
        	if (!isAtLeastOneWhere) {
        		isAtLeastOneWhere = true;
        	} else {
        		selection += " AND ";
        	}
            selection += GroceryTable.COLUMN_GROCERY_PRICE + " >= ?";
            selectionArgs.add(CategoryTopFragmentActivity.mPriceRangeMin.toString());
        }
        if (CategoryTopFragmentActivity.mPriceRangeMax != null) {
        	if (!isAtLeastOneWhere) {
        		isAtLeastOneWhere = true;
        	} else {
        		selection += " AND ";
        	}
            selection += GroceryTable.COLUMN_GROCERY_PRICE + " <= ?";
            selectionArgs.add(CategoryTopFragmentActivity.mPriceRangeMax.toString());
        }

        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, GroceryTable.COLUMN_GROCERY_SCORE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
        if (progressView != null)
            progressView.setVisibility(View.GONE);

        if (cursor.getCount() == 0) {
        	if (!mQuery.isEmpty()) {
        		displayEmptyListMessage(buildNoSearchResultString());
        	} else {
        		displayEmptyListMessage(buildNoNewContentString());
        	}
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    /*private void refreshGrocery() {
        refreshItem = menu.findItem(R.id.refresh);
        RefreshAnimation.refreshIcon(getActivity(), true, refreshItem);

        Intent intent = new Intent(getActivity(), NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        getActivity().startService(intent);
    }*/

    public void loadDataWithQuery(Boolean reload, String query) {
        Bundle b = new Bundle();
        b.putString("query", query);
        if (reload) {
            b.putBoolean("reload", true);
            getLoaderManager().restartLoader(0, b, this);
        } else {
            b.putBoolean("reload", false);
            getLoaderManager().restartLoader(0, b, this);
        }
    }

    public void fillData() {
        // Prepare the asynchronous loader.
        Bundle b = new Bundle();
        b.putString("query", "");
        b.putBoolean("reload", false);
        getLoaderManager().initLoader(0, b, this);
    }

    public void displayEmptyListMessage(String emptyStringMsg) {
        ListView myListView = this.getListView();
        emptyTextView.setText(emptyStringMsg);
        emptyTextView.setVisibility(View.VISIBLE);
        myListView.setEmptyView(emptyTextView);
    }

    public String buildNoNewContentString() {
        String emptyStringFormat = getResources().getString(R.string.no_new_content);
        return (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
    }

    public String buildNoSearchResultString() {
        return getResources().getString(R.string.no_search_results);
    }

    private void watchSettings() {
        mSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                loadDataWithQuery(true, "");
            }
        };
        SettingsManager.getPrefs(mActivity).registerOnSharedPreferenceChangeListener(mSettingsListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingsManager.getPrefs(mActivity).unregisterOnSharedPreferenceChangeListener(mSettingsListener);
    }
}
