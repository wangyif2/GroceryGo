package com.groceryotg.android;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.GroceryListCursorAdapter;
import com.groceryotg.android.fragment.GroceryViewBinder;
import com.slidingmenu.lib.SlidingMenu;

public class GlobalSearchActivity extends SherlockListActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final String GLOBAL_SEARCH = "global_search";
	
	private GroceryListCursorAdapter adapter;
	private String mQuery;
	private SlidingMenu mSlidingMenu;
	private SearchView mSearchView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        this.getListView().setDividerHeight(2);
        
        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Configure sliding menu
        configSlidingMenu();
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        handleIntent(intent);
        
        fillData();
    }
	
	@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        refreshQuery();
    }
	
	private void handleIntent(Intent intent) {
		if (intent.getExtras().containsKey(GLOBAL_SEARCH) || Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mQuery = intent.getStringExtra(SearchManager.QUERY);
        }
	}
	
	@Override
    public boolean onQueryTextSubmit(String query) {
        String newQuery = !TextUtils.isEmpty(query) ? query : null;

        // Don't do anything if the query hasn't changed
        if (newQuery == null && mQuery == null || newQuery != null && mQuery.equals(newQuery))
            return true;
        
        refreshQuery();
        return true;
    }
	
	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
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
            	Intent parentActivityIntent = new Intent(this, GroceryFragmentActivity.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	finish();
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the options menu from XML
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.grocery_pager_menu, menu);
	    
	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    mSearchView.setIconifiedByDefault(true);
	    
	    return true;
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

        
        adapter = new GroceryListCursorAdapter(this, R.layout.grocery_fragment_row, null, from, to);
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
				                CartTable.COLUMN_CART_FLAG_SHOPLIST,
				                CartTable.COLUMN_CART_FLAG_WATCHLIST};
    	
    	List<String> selectionArgs = new ArrayList<String>();
    	String selection = "";
    	
    	// If user entered a search query, filter the results based on grocery name
        if (!mQuery.isEmpty()) {
            selection += GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + mQuery + "%");
        }
        
        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        return new CursorLoader(this, GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
    
    private void configSlidingMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(R.layout.menu_frame);

        // Populate the SlidingMenu
        String[] slidingMenuItems = new String[]{getString(R.string.slidingmenu_item_cat),
                getString(R.string.slidingmenu_item_cart),
                getString(R.string.slidingmenu_item_map),
                getString(R.string.slidingmenu_item_sync),
                getString(R.string.slidingmenu_item_settings),
                getString(R.string.slidingmenu_item_about)};

        ListView menuView = (ListView) findViewById(R.id.menu_items);
        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this,
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
        Intent intent = new Intent(this, GroceryFragmentActivity.class);
        startActivity(intent);
    }
    
    private void launchMapActivity() {
        Intent intent = new Intent(this, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private void launchShopCartActivity() {
        Intent intent = new Intent(this, ShopCartOverView.class);
        startActivity(intent);
    }
}
