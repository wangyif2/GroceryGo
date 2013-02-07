package com.groceryotg.android;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.NetworkHandler;

public class CategoryOverView extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list);
        this.getListView().setDividerHeight(2);
        fillData();
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
                refreshCurrentCategory();
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(this, GroceryOverView.class);
        Uri uri = Uri.parse(GroceryotgProvider.CONTENT_URI_CAT + "/" + id);
        intent.putExtra(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT, uri);

        startActivity(intent);
    }

    //TODO: implement IntentService Callback
    private void refreshCurrentCategory() {
        Intent intent = new Intent(this, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
        startService(intent);
    }

    private void launchShopCartActivity() {

    }

    private void launchMapActivity() {
        Intent intent = new Intent(this, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void fillData() {
        String[] from = new String[]{CategoryTable.COLUMN_CATEGORY_NAME};
        int[] to = new int[]{R.id.category_row_label};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.category_row, null, from, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {CategoryTable.COLUMN_ID, CategoryTable.COLUMN_CATEGORY_NAME};
        return new CursorLoader(this, GroceryotgProvider.CONTENT_URI_CAT, projection, null, null, null);
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
