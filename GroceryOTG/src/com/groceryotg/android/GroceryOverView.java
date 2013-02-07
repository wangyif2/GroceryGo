package com.groceryotg.android;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryOverView extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private Uri contactUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_list);

        Bundle extras = getIntent().getExtras();

        contactUri = (savedInstanceState == null) ? null : (Uri) savedInstanceState.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT);

        if (extras != null) {
            contactUri = extras.getParcelable(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT);
            String[] projection = {CategoryTable.COLUMN_CATEGORY_NAME};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                this.getActionBar().setTitle(cursor.getString(cursor.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_NAME)));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshCurrentGrocery();
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

    private void launchShopCartActivity() {

    }

    private void launchMapActivity() {
    }

    private void refreshCurrentGrocery() {

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
