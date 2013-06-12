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
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartCursorAdapter;
import com.groceryotg.android.ShopCartDetailView;
import com.groceryotg.android.ShopCartViewBinder;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

public class ShopCartOverviewFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final int DELETE_ID = 1;
    private ShopCartCursorAdapter adapter;

    private Activity mActivity;
    
    @Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
		this.setHasOptionsMenu(true);
	}
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	registerForContextMenu(getListView());
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initFilter();
        
        fillData();
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.shopcart_list, container, false);
    	return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shopcart_menu, menu);
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
        							 CartTable.COLUMN_CART_FLAG_SHOPLIST};
        int[] to = new int[]{R.id.cart_item_id,
        					 R.id.cart_grocery_name,
        					 R.id.cart_grocery_id,
        					 R.id.cart_row_in_shopcart};

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
        					   CartTable.COLUMN_CART_FLAG_SHOPLIST};
        
        List<String> selectionArgs = new ArrayList<String>();
        String selection = "";
        
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

    private void initFilter() {
    }
    
    private void refreshData() {
    	getLoaderManager().restartLoader(0, null, this);
    }
}
