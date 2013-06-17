package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.*;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.settings.SettingsManager;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class GroceryListFragment extends SherlockListFragment {
    private static final String CATEGORY_POSITION = "position";
    Activity mActivity;
    
    GroceryListCursorAdapter adapter;
    String mQuery = "";
    
    ProgressBar progressView;
    Menu menu;
    MenuItem refreshItem;
    private Integer categoryId = GroceryListCursorAdapter.GLOBAL_SEARCH_CATEGORY;

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

        adapter = new GroceryListCursorAdapter(getActivity(), R.layout.grocery_fragment_list_row, null, from, to, this.categoryId, this.getListView(), mQuery, getLoaderManager());
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
    
    public void loadDataWithQuery(Boolean reload, String query) {
        Bundle b = new Bundle();
        b.putString("query", query);
        if (reload) {
            b.putBoolean("reload", true);
            getLoaderManager().restartLoader(0, b, adapter);
        } else {
            b.putBoolean("reload", false);
            getLoaderManager().restartLoader(0, b, adapter);
        }
    }

    public void fillData() {
        // Prepare the asynchronous loader.
        Bundle b = new Bundle();
        b.putString("query", "");
        b.putBoolean("reload", false);
        getLoaderManager().initLoader(0, b, adapter);
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
