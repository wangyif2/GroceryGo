package com.groceryotg.android;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ShopCartOverView extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DELETE_ID = 1;
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopcart_list);
        this.getListView().setDividerHeight(2);
        fillData();
        registerForContextMenu(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shopcart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart_add:
                createCartGroceryItem();
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
    public boolean onContextItemSelected(MenuItem item) {
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
        String[] from = new String[]{CartTable.COLUMN_CART_GROCERY_NAME};
        int[] to = new int[]{R.id.cart_grocery_name};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.shopcart_row, null, from, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {CartTable.COLUMN_ID, CartTable.COLUMN_CART_GROCERY_NAME};
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
}
