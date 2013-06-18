package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.util.SparseArray;
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
import com.groceryotg.android.utils.GroceryOTGUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class GroceryListFragment extends SherlockListFragment {
    private static final String CATEGORY_POSITION = "position";
    private static final String ARGS_DISTANCE_MAP_KEYS = "distance_map_keys";
    private static final String ARGS_DISTANCE_MAP_VALUES = "distance_map_values";
    
    private Activity mActivity;
    
    private GroceryListCursorAdapter adapter;
    private String mQuery = "";
    
    private ProgressBar progressView;
    private Menu menu;
    private MenuItem refreshItem;
    private Integer categoryId = GroceryListCursorAdapter.GLOBAL_SEARCH_CATEGORY;
    
    private SparseArray<Float> mDistanceMap;

    ViewGroup myViewGroup;

    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    public static GroceryListFragment newInstance(int pos, SparseArray<Float> distanceMap) {
        GroceryListFragment f = new GroceryListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(CATEGORY_POSITION, pos);
        f.setArguments(args);
        
        int[] keyArray = new int[distanceMap.size()];
        float[] valueArray = new float[distanceMap.size()];
        for (int index = 0; index < distanceMap.size(); index++) {
        	keyArray[index] = distanceMap.keyAt(index);
        	valueArray[index] = distanceMap.valueAt(index);
        }
        args.putIntArray(ARGS_DISTANCE_MAP_KEYS, keyArray);
        args.putFloatArray(ARGS_DISTANCE_MAP_VALUES, valueArray);

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
        Bundle args = getArguments();
        if (args != null) {
        	categoryId = args.getInt(CATEGORY_POSITION);
        	
        	mDistanceMap = new SparseArray<Float>();
			int[] keyArray = args.getIntArray(ARGS_DISTANCE_MAP_KEYS);
			float[] valueArray = args.getFloatArray(ARGS_DISTANCE_MAP_VALUES);
			for (int index = 0; index < keyArray.length; index++) {
				mDistanceMap.put(keyArray[index], valueArray[index]);
			}
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
        
        if (mDistanceMap == null) {
	        mDistanceMap = GroceryOTGUtils.buildDistanceMap(mActivity);
        }
        
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
                R.id.grocery_row_store_parent_name,
                R.id.grocery_row_store_id,
                R.id.grocery_row_flyer_url,
                R.id.grocery_row_in_shopcart};
        
        // Handles the search filter
        Bundle args = mActivity.getIntent().getExtras();
        if (args != null) {
	        if (args.containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH)) {
				// Update the query - this is used by the loader when fetching results from database
				mQuery = args.getString(SearchManager.QUERY).trim();
	        }
        }

        int layoutId = R.layout.grocery_fragment_list_row;
        // Uncomment this to use alternate layout
        //layoutId = R.layout.grocery_fragment_list_row_alt;
        adapter = new GroceryListCursorAdapter(getActivity(), layoutId, null, from, to, this.categoryId, this.getView(), this.getListView(), mQuery, getLoaderManager(), this.mDistanceMap);
        adapter.setViewBinder(new GroceryViewBinder());
        
        SlideExpandableListAdapter wrappedAdapter = new SlideExpandableListAdapter(adapter, R.id.expandable_toggle_button, R.id.expandable);
        // Make a VERY short animation duration
        wrappedAdapter.setAnimationDuration(0);
        
        setListAdapter(wrappedAdapter);
        
        this.loadDataWithQuery(false, mQuery);
    }
    
    public void loadDataWithQuery(Boolean reload, String query) {
    	if (adapter == null)
    		return;
    	
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
        b.putString("query", mQuery);
        b.putBoolean("reload", false);
        getLoaderManager().initLoader(0, b, adapter);
    }

    private void watchSettings() {
        mSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                loadDataWithQuery(true, mQuery);
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
