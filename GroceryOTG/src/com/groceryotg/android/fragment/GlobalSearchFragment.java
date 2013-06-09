package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import com.groceryotg.android.GlobalSearchFragmentActivity;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.GroceryMapView;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartOverviewFragmentActivity;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.GroceryListCursorAdapter;
import com.groceryotg.android.fragment.GroceryViewBinder;
import com.groceryotg.android.settings.SettingsManager;

import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class GlobalSearchFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private GroceryListCursorAdapter adapter;
	private String mQuery;
	private SlidingMenu mSlidingMenu;
	private SearchView mSearchView;
    private Activity mActivity;
	
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
	}
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        configActionBar();
        configSlidingMenu();
        
        Bundle args = getArguments();
        handleIntent(args);
        fillData();
    }
	
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	ListView v = (ListView) inflater.inflate(R.layout.search_list, null);
    	v.setDividerHeight(2);
    	return v;
    }
	
	private void handleIntent(Bundle args) {
		if (args.containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH)) { // || Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
			// Update the query - this is used by the loader when fetching results from database
			mQuery = args.getString(SearchManager.QUERY).trim();
			
			// When we receive an intent from within this activity (i.e. after it
			// has been created), the searchView already exists. In this case,
			// update the searchView text to display the new query, and clear focus
			// in order to collapse the keyboard.
			if (mSearchView != null) {
				mSearchView.setQuery(mQuery, false);
				mSearchView.clearFocus();
			}
        }
	}
	
	private void refreshQuery() {
        Bundle b = new Bundle();
    	b.putBoolean("reload", true);
    	getLoaderManager().restartLoader(0, b, this);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.
            	
            	// Specify the parent activity
            	Intent parentActivityIntent = new Intent(mActivity, GroceryFragmentActivity.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	mActivity.finish();
            	return true;
            case R.id.map:
                launchMapActivity();
                return true;
            case R.id.shop_cart:
                launchShopCartActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.search_menu, menu);
	    
	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
	    mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.getComponentName()));
	    mSearchView.setIconifiedByDefault(true);
	    //mSearchView.setOnQueryTextListener(this);
	    
	    // Add callbacks to the menu item that contains the SearchView in order to capture
        // the event of pressing the 'back' button
        MenuItem searchItem = menu.findItem(R.id.search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // This is called when the user clicks on the magnifying glass icon to
                // expand the search view widget.
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // This is called the user presses the 'back' button to exit the collapsed
                // search widget view (i.e., to close the search). Here, refresh the query
                // to display the whole list of items:
                mQuery = "";
                refreshQuery();
                return true;
            }
        });
	    
	    // When we have received a search intent from another activity (i.e. not by 
        // capturing user input in this activity), we need to programmatically expand 
        // the search menu item as if the user clicked the magnifying glass, set
        // the searchView text to the received query, and call clearFocus to collapse
        // the keyboard.
	    if (!mQuery.isEmpty()) {
	    	menu.findItem(R.id.search).expandActionView();
	    	mSearchView.setQuery(mQuery, false);
	    	mSearchView.clearFocus();
	    }
	}
	
	private void fillData() {
		String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST};
        int[] to = new int[]{R.id.grocery_row_id,
                R.id.grocery_row_label,
                R.id.grocery_row_details,
                R.id.grocery_row_price,
                R.id.grocery_row_store,
                R.id.grocery_row_inshopcart,
                R.id.grocery_row_inwatchlist,
                R.id.grocery_row_inshopcart_flag,
                R.id.grocery_row_inwatchlist_flag};

        
        adapter = new GroceryListCursorAdapter(mActivity, R.layout.grocery_fragment_row, null, from, to);
        adapter.setViewBinder(new GroceryViewBinder());
        ((ListActivity) mActivity).setListAdapter(adapter);
        
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
				                CartTable.COLUMN_CART_FLAG_SHOPLIST,
				                CartTable.COLUMN_CART_FLAG_WATCHLIST};
    	
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
    
    private void configSlidingMenu() {
        mSlidingMenu = new SlidingMenu(mActivity);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.xml.shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.attachToActivity(mActivity, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(R.layout.menu_frame);

        // Populate the SlidingMenu
        String[] slidingMenuItems = new String[]{getString(R.string.slidingmenu_item_cat),
                getString(R.string.slidingmenu_item_cart),
                getString(R.string.slidingmenu_item_map),
                getString(R.string.slidingmenu_item_sync),
                getString(R.string.slidingmenu_item_settings),
                getString(R.string.slidingmenu_item_about)};

        ListView menuView = (ListView) mActivity.findViewById(R.id.menu_items);
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.menu_item, android.R.id.text1, slidingMenuItems);
        menuView.setAdapter(menuAdapter);

        menuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Switch activity based on what mSlidingMenu item the user selected
                TextView textView = (TextView) view;
                String selectedItem = textView.getText().toString();

                if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cat))) {
                    // Selected Categories
                    launchHomeActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cart))) {
                    // Selected Shopping Cart
                    launchShopCartActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
                	if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_settings))) {
                    // Selected Settings
                    if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_about))) {
                    // Selected About
                    //startActivity(new Intent(CategoryOverView.this, About.class));
                    if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                }
            }
        });
    }
    
    private void launchHomeActivity() {
        Intent intent = new Intent(mActivity, GroceryFragmentActivity.class);
        startActivity(intent);
    }
    
    private void launchMapActivity() {
        Intent intent = new Intent(mActivity, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private void launchShopCartActivity() {
        Intent intent = new Intent(mActivity, ShopCartOverviewFragmentActivity.class);
        startActivity(intent);
    }
    
    private void configActionBar() {
    	((SherlockFragmentActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
