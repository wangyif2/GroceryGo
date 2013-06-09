package com.groceryotg.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.GroceryMapView;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartCursorAdapter;
import com.groceryotg.android.ShopCartDetailView;
import com.groceryotg.android.ShopCartViewBinder;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.slidingmenu.lib.SlidingMenu;

public class ShopCartOverviewFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int DELETE_ID = 1;
    private ShopCartCursorAdapter adapter;
    private SlidingMenu slidingMenu;
    private Menu actionBarMenu;
    private boolean filterShoplist;
    private boolean filterWatchlist;
    
    private Activity mActivity;
    
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initFilter();
        
        configActionBar();
        
        configSlidingMenu();
        
        fillData();
        registerForContextMenu(getListView());
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	ListView v = (ListView) inflater.inflate(R.layout.shopcart_list, null);
    	v.setDividerHeight(2);
    	return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shopcart_menu, menu);
        actionBarMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart_add:
                createCartGroceryItem();
                return true;
            case R.id.cart_filter_shoplist:
            	//MenuItem itemShoplist = ;
            	int newIconShoplist = (filterShoplist ? R.drawable.ic_menu_cart : R.drawable.ic_menu_cart_highlight);
            	actionBarMenu.findItem(R.id.cart_filter_shoplist).setIcon(newIconShoplist);
            	filterShoplist = (filterShoplist ? false : true);
            	refreshData();
            	return true;
            case R.id.cart_filter_watchlist:
            	int newIconWatchlist = (filterWatchlist ? R.drawable.ic_menu_watch : R.drawable.ic_menu_watch_highlight);
            	actionBarMenu.findItem(R.id.cart_filter_watchlist).setIcon(newIconWatchlist);
            	filterWatchlist = (filterWatchlist ? false : true);
            	refreshData();
            	return true;
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.cart_item_delete);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Uri uri = Uri.parse(GroceryotgProvider.CONTENT_URI_CART_ITEM + "/" + info.id);
                mActivity.getContentResolver().delete(uri, null, null);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(mActivity, ShopCartDetailView.class);
        Uri cartGroceryItemUri = Uri.parse(GroceryotgProvider.CONTENT_URI_CART_ITEM + "/" + id);
        i.putExtra(GroceryotgProvider.CONTENT_ITEM_TYPE_CART_ITEM, cartGroceryItemUri);

        startActivity(i);
    }

    private void createCartGroceryItem() {
        Intent i = new Intent(mActivity, ShopCartDetailView.class);
        startActivity(i);
    }

    private void fillData() {
        String[] from = new String[]{CartTable.COLUMN_ID,
        							 CartTable.COLUMN_CART_GROCERY_NAME,
        							 CartTable.COLUMN_CART_GROCERY_ID,
        							 CartTable.COLUMN_CART_FLAG_SHOPLIST,
        							 CartTable.COLUMN_CART_FLAG_WATCHLIST,
        							 CartTable.COLUMN_CART_FLAG_SHOPLIST,
        							 CartTable.COLUMN_CART_FLAG_WATCHLIST};
        int[] to = new int[]{R.id.cart_item_id,
        					 R.id.cart_grocery_name,
        					 R.id.cart_grocery_id,
        					 R.id.cart_flag_shoplist,
        					 R.id.cart_flag_watchlist,
        					 R.id.cart_row_inshoplist,
        					 R.id.cart_row_inwatchlist};

        getLoaderManager().initLoader(0, null, this);
        adapter = new ShopCartCursorAdapter(mActivity, R.layout.shopcart_row, null, from, to);
        adapter.setViewBinder(new ShopCartViewBinder());
        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {CartTable.COLUMN_ID, 
        					   CartTable.COLUMN_CART_GROCERY_NAME, 
        					   CartTable.COLUMN_CART_GROCERY_ID, 
        					   CartTable.COLUMN_CART_FLAG_SHOPLIST, 
        					   CartTable.COLUMN_CART_FLAG_WATCHLIST};
        
        List<String> selectionArgs = new ArrayList<String>();
        String selection = "";
        
        // If user clicked on a filter, filter the results based on flags
        if (filterShoplist) {
        	selection += (selection.isEmpty() ? "" : " AND "); 
            selection += CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_FLAG_SHOPLIST + "=?";
            selectionArgs.add("1");
        }
        if (filterWatchlist) {
        	selection += (selection.isEmpty() ? "" : " AND ");
        	selection += CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_FLAG_WATCHLIST + "=?";
            selectionArgs.add("1");
        }
        
        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        
        String sortOrder = CartTable.TABLE_CART + "." + CartTable.COLUMN_CART_GROCERY_NAME;
        
        return new CursorLoader(mActivity, GroceryotgProvider.CONTENT_URI_CART_ITEM, projection, selection, selectionArgsArr, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
    
    private void configSlidingMenu() {
        slidingMenu = new SlidingMenu(mActivity);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.xml.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.attachToActivity(mActivity, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.menu_frame);

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

        menuView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Switch activity based on what slidingMenu item the user selected
                TextView textView = (TextView) view;
                String selectedItem = textView.getText().toString();

                if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cat))) {
                    // Selected Categories
                	launchHomeActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cart))) {
                    // Selected Shopping Cart
                	if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
                	if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_settings))) {
                    // Selected Settings
                	if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_about))) {
                    // Selected About
                    //startActivity(new Intent(CategoryOverView.this, About.class));
                	if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                }
            }
        });
    }
    
    private void configActionBar() {
    	((SherlockFragmentActivity) mActivity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    private void initFilter() {
    	filterShoplist = false;
        filterWatchlist = false;
    }
    
    private void refreshData() {
    	getLoaderManager().restartLoader(0, null, this);
    }
}
