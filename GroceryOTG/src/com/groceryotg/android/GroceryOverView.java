package com.groceryotg.android;

import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.utils.RefreshAnimation;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryOverView extends SherlockListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private MenuItem refreshItem;
    private Uri groceryUri;
    private String categoryName;
    private Integer categoryId;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_list);

        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Bundle extras = getIntent().getExtras();
        Intent intent = getIntent();
        groceryUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT);
        
        // Handle search events
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	mQuery = intent.getStringExtra(SearchManager.QUERY);
		}
        else {
        	mQuery = "";
        }
        
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

        this.getListView().setDividerHeight(2);
        fillData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.groceryoverview_menu, menu);
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
            case R.id.map:
                launchMapActivity();
                return true;
            case R.id.shop_cart:
                launchShopCartActivity();
                return true;
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v.findViewById(R.id.grocery_row_label);

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, textView.getText().toString());

        getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);

        Toast t = Toast.makeText(this, "Item added to Shopping Cart", Toast.LENGTH_SHORT);
        t.show();
    }

    private void launchShopCartActivity() {
        Intent intent = new Intent(this, ShopCartOverView.class);
        startActivity(intent);
    }

    private void launchMapActivity() {
        Intent intent = new Intent(this, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    	
    	Cursor customCursor = null;
    	String[] from = new String[]{GroceryTable.COLUMN_GROCERY_NAME, GroceryTable.COLUMN_GROCERY_PRICE};
        int[] to = new int[]{R.id.grocery_row_label, R.id.grocery_row_price};
    	
    	// If the user entered a search query, set up a cursor with selection criteria
    	if (!mQuery.isEmpty()) {
    		String selectionClause = GroceryTable.COLUMN_GROCERY_NAME + " = ?";
    		String[] selectionArgs = {mQuery};
    		
    		customCursor = getContentResolver().query(groceryUri, from, selectionClause, selectionArgs, null);
    	}
    	
        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.grocery_row, customCursor, from, to, 0);
        
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 2) {
                    TextView textView = (TextView) view;
                    if (cursor.getDouble(columnIndex) != 0) {
                        textView.setText("$" + ServerURL.getGetDecimalFormat().format(cursor.getDouble(columnIndex)));
                    } else {
                        textView.setText("N/A");
                    }
                    return true;
                }
                return false;
            }
        });

        setListAdapter(adapter);
        displayEmptyListMessage();
    }

    
    private void displayEmptyListMessage() {
        String emptyStringFormat = getResources().getString(R.string.no_new_content);
        String emptyStringMsg = (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
        ListView myListView = this.getListView();
        TextView myTextView = (TextView) findViewById(R.id.empty_grocery_list);
        myTextView.setText(emptyStringMsg);
        myListView.setEmptyView(myTextView);
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
        displayEmptyListMessage();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {GroceryTable.COLUMN_ID, GroceryTable.COLUMN_GROCERY_NAME, GroceryTable.COLUMN_GROCERY_PRICE};
        String selection = GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
        String[] selectionArgs = {categoryId.toString()};
        return new CursorLoader(this, GroceryotgProvider.CONTENT_URI_GRO, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
