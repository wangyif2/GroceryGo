package com.groceryotg.android;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.fragment.GroceryViewBinder;
import com.slidingmenu.lib.SlidingMenu;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ShopCartOverView extends SherlockListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DELETE_ID = 1;
    private ShopCartCursorAdapter adapter;
    private SlidingMenu slidingMenu;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_list);
        this.getListView().setDividerHeight(2);
        
        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Configure sliding menu
        configSlidingMenu();
        
        fillData();
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.shopcart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart_add:
                createCartGroceryItem();
                return true;
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
                getContentResolver().delete(uri, null, null);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ShopCartDetailView.class);
        Uri cartGroceryItemUri = Uri.parse(GroceryotgProvider.CONTENT_URI_CART_ITEM + "/" + id);
        i.putExtra(GroceryotgProvider.CONTENT_ITEM_TYPE_CART_ITEM, cartGroceryItemUri);

        startActivity(i);
    }

    private void createCartGroceryItem() {
        Intent i = new Intent(this, ShopCartDetailView.class);
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
        adapter = new ShopCartCursorAdapter(this, R.layout.shopcart_row, null, from, to);
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
        return new CursorLoader(this, GroceryotgProvider.CONTENT_URI_CART_ITEM, projection, null, null, null);
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
        Intent intent = new Intent(this, GroceryFragmentActivity.class);
        startActivity(intent);
    }
    
    private void launchMapActivity() {
        Intent intent = new Intent(this, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    private void configSlidingMenu() {
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.menu_frame);

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
}
