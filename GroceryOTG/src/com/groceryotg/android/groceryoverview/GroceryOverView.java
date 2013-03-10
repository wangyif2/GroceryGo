package com.groceryotg.android.groceryoverview;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.CategoryOverView;
import com.groceryotg.android.GroceryMapView;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartOverView;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.utils.RefreshAnimation;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryOverView extends SherlockListActivity implements OnQueryTextListener, OnCloseListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private MenuItem refreshItem;
    private SlidingMenu slidingMenu;
    private Uri groceryUri;
    private String categoryName;
    private Integer categoryId;
    
    // User search
    private String mQuery;
    private SearchView mSearchView;

    // Filters
    private Map<Integer,String> storeNames;
    private SparseIntArray storeSelected;
    private Integer subcategoryId;
    private Double mPriceRangeMin;
    private Double mPriceRangeMax;

    private final Integer SELECTED = 1;
    private final Integer NOT_SELECTED = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_list);

        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure sliding menu
        configSlidingMenu();
        
        Bundle extras = getIntent().getExtras();
        groceryUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT);

        if (extras != null) {
            groceryUri = extras.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT);
            String[] projection = {CategoryTable.COLUMN_CATEGORY_NAME, CategoryTable.COLUMN_CATEGORY_ID};
            Cursor cursor = getContentResolver().query(groceryUri, projection, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_NAME));
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_ID));

                this.getActionBar().setTitle(categoryName);
            }
        }

        // Initialize the list of stores from database
        storeSelected = new SparseIntArray();
        storeNames = new HashMap<Integer,String>();
        Uri storeUri = GroceryotgProvider.CONTENT_URI_STO;
        String[] projection = {StoreTable.COLUMN_STORE_ID, StoreTable.COLUMN_STORE_NAME};
        Cursor storeCursor = getContentResolver().query(storeUri, projection, null, null, null);
        if (storeCursor != null) {
        	storeCursor.moveToFirst();
	        while (!storeCursor.isAfterLast()) {
	        	storeSelected.put(storeCursor.getInt(storeCursor.getColumnIndex(StoreTable.COLUMN_STORE_ID)), SELECTED);
	        	storeNames.put(storeCursor.getInt(storeCursor.getColumnIndex(StoreTable.COLUMN_STORE_ID)),
	        				   storeCursor.getString(storeCursor.getColumnIndex(StoreTable.COLUMN_STORE_NAME)));
	        	storeCursor.moveToNext();
	        }
        }
        // Initialize the user query to blank
        setmQuery("");

        this.getListView().setDividerHeight(2);
        fillData();
    }

    @Override
    public boolean onClose() {
        /*
         * This method NEVER gets called IF the SearchView is set to be collapsible
    	 * (showAsAction=collapsibleActionView in the XML file). Making it collapsible
    	 * means that the search widget shows up in the top action bar instead of at the
    	 * bottom. With a collapsible SearchView, the 'x' button only clears the text in
    	 * the EditText field. After pressing it once, the 'x' disappears.
    	 *
    	 * With a non-collapsible SearchView, the 'x' button pressed once clears the text
    	 * in the EditText field, and then (it is still visible) pressing it again invokes this method.
    	 */
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }

        // Refresh the list to display all items again and hide the search view
        loadDataWithQuery(true, "");
        mSearchView.setVisibility(SearchView.GONE);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.groceryoverview_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // If set to "true" the icon is displayed within the EditText, if set to "false" it is displayed outside
        mSearchView.setIconifiedByDefault(true);

        // Instead of invoking activity again, use onQueryTextListener when a search is performed
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);

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
                loadDataWithQuery(true, "");
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshItem = item;
                RefreshAnimation.refreshIcon(this, true, refreshItem);
                refreshCurrentGrocery();
                return true;
            case R.id.filter:
            	launchFilterDialog();
            	return true;
            /*
            case R.id.map:
                launchMapActivity();
                return true;
            case R.id.shop_cart:
                launchShopCartActivity();
                return true;
            */
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.

                // Specify the parent activity
                Intent parentActivityIntent = new Intent(this, CategoryOverView.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        /*
         * You don't need to deal with "appData" and passing bundles back to the search
         * activity, because you already have the search query here.
         */
        String newQuery = !TextUtils.isEmpty(query) ? query : null;

        // Don't do anything if the query hasn't changed
        if (newQuery == null && mQuery == null ||
                newQuery != null && mQuery.equals(newQuery))
            return true;

        loadDataWithQuery(true, newQuery);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // This is called when you click the search icon or type characters in the search widget
        // (called on every keystroke)
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v.findViewById(R.id.grocery_row_label);

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, textView.getText().toString());

        getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);

        Toast t = Toast.makeText(this, "Item added to Shopping Cart", Toast.LENGTH_SHORT);
        t.show();
    }

    
    private void launchFilterDialog() {
    	final CharSequence[] items = new CharSequence[storeNames.keySet().size()];
    	final boolean[] states = new boolean[storeNames.keySet().size()];
    	final SparseIntArray mapIndexToId = new SparseIntArray(); // maps index in the dialog to store_id
    	
    	Iterator<Entry<Integer,String>> it = storeNames.entrySet().iterator();
    	Integer indexer = 0;
    	while (it.hasNext()) {
    		Map.Entry<Integer,String> pairs = (Map.Entry<Integer,String>) it.next();
    		items[indexer] = pairs.getValue();
    		states[indexer] = (storeSelected.get(pairs.getKey()) == SELECTED ? true : false);
    		mapIndexToId.put(indexer, pairs.getKey());
    		indexer++;
    	}
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.groceryoverview_filter_title);
        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SparseBooleanArray selectedItems = ((AlertDialog)dialog).getListView().getCheckedItemPositions();
                for (int i=0; i < selectedItems.size(); i++) {
                	int key_index = selectedItems.keyAt(i);
                	//Log.d("FilterDialog", "SelectedItems, i=" + ((Integer)i).toString() 
            		//		+ ", key=" + ((Integer)selectedItems.keyAt(i)).toString() 
            		//		+ ", store=" + items[key_index] + ", selected? " 
                	//		+ (selectedItems.get(i)==true ? "yes" : "no") );
                	if (selectedItems.get(key_index)) {
                		storeSelected.put(mapIndexToId.get(key_index), SELECTED);
                	}
                	else {
                		storeSelected.put(mapIndexToId.get(key_index), NOT_SELECTED);
                	}
                }
                Toast.makeText(getContext(), getResources().getString(R.string.groceryoverview_filter_updated), Toast.LENGTH_LONG).show();
                loadDataWithQuery(true, mQuery);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        });
        builder.create().show();
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

    private void launchHomeActivity() {
        Intent intent = new Intent(this, CategoryOverView.class);
        startActivity(intent);
    }
    
    private void refreshCurrentGrocery() {
        Intent intent = new Intent(this, NetworkHandler.class);

        PendingIntent pendingIntent = createPendingResult(1, intent, PendingIntent.FLAG_ONE_SHOT);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        intent.putExtra("pendingIntent", pendingIntent);

        startService(intent);
    }

    private void fillData() {
        String[] from = new String[]{GroceryTable.COLUMN_GROCERY_NAME, GroceryTable.COLUMN_GROCERY_NAME, GroceryTable.COLUMN_GROCERY_PRICE, StoreTable.COLUMN_STORE_NAME};
        int[] to = new int[]{R.id.grocery_row_label, R.id.grocery_row_details, R.id.grocery_row_price, R.id.grocery_row_store};

        adapter = new SimpleCursorAdapter(this, R.layout.grocery_row, null, from, to, 0);
        adapter.setViewBinder(new GroceryViewBinder());

        setListAdapter(adapter);
        displayEmptyListMessage(buildNoNewContentString());

        // Prepare the asynchronous loader.
        loadDataWithQuery(false, "");
    }
    
    private void loadDataWithQuery(Boolean reload, String query) {
        Bundle b = new Bundle();
        b.putString("query", query);
        if (reload) {
            setmQuery(query);
            b.putBoolean("reload", true);
            getLoaderManager().restartLoader(0, b, this);
        } else {
            b.putBoolean("reload", false);
            getLoaderManager().initLoader(0, b, this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast toast = null;
        RefreshAnimation.refreshIcon(this, false, refreshItem);
        if (resultCode == NetworkHandler.CONNECTION) {
            toast = Toast.makeText(this, "Groceries Updated", Toast.LENGTH_LONG);
        } else if (resultCode == NetworkHandler.NO_CONNECTION) {
            toast = Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG);
        }
        assert toast != null;
        toast.show();
        displayEmptyListMessage(buildNoNewContentString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String emptyString = args.getBoolean("reload") ? buildNoSearchResultString() : buildNoNewContentString();
        String query = args.getString("query");
        displayEmptyListMessage(emptyString);

        List<String> selectionArgs = new ArrayList<String>();

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreTable.COLUMN_STORE_NAME};
        String selection = GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
        selectionArgs.add(categoryId.toString());

        // If user entered a search query, filter the results based on grocery name
        if (!query.isEmpty()) {
            selection += " AND " + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + query + "%");
        }
        if (storeSelected != null && storeSelected.size() > 0) {
        	// Go through selected stores and add them to query
        	String storeSelection = "";
        	for (int i=0; i<storeSelected.size(); i++) {
        		if (storeSelected.valueAt(i)==SELECTED) {
        			if (storeSelection.isEmpty()) {
        				storeSelection = " AND (";
        				storeSelection += GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_STORE + " = ?";
        			}
        			else {
        				storeSelection += " OR " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_STORE + " = ?";
        			}
        			selectionArgs.add(((Integer)storeSelected.keyAt(i)).toString());
        		}
        	}
        	if (!storeSelection.isEmpty()) {
        		storeSelection += ")";
        		selection += storeSelection;
        	}
        }
        if (mPriceRangeMin != null) {
            selection += " AND " + GroceryTable.COLUMN_GROCERY_PRICE + " >= ?";
            selectionArgs.add(mPriceRangeMin.toString());
        }
        if (mPriceRangeMax != null) {
            selection += " AND " + GroceryTable.COLUMN_GROCERY_PRICE + " <= ?";
            selectionArgs.add(mPriceRangeMax.toString());
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
        // Called when a previously created loader is being reset, thus making its
        // data unavailable
        adapter.swapCursor(null);
    }

    private void displayEmptyListMessage(String emptyStringMsg) {
        ListView myListView = this.getListView();
        TextView myTextView = (TextView) findViewById(R.id.empty_grocery_list);
        myTextView.setText(emptyStringMsg);
        myListView.setEmptyView(myTextView);
    }

    private String buildNoNewContentString() {
        String emptyStringFormat = getResources().getString(R.string.no_new_content);
        return (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
    }

    private String buildNoSearchResultString() {
        return getResources().getString(R.string.no_search_results);
    }

    public void setmQuery(String mQuery) {
        this.mQuery = mQuery;
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
                    launchShopCartActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
                	refreshCurrentGrocery();
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
    
    private Context getContext() {
        return this.getBaseContext();
    }
}
