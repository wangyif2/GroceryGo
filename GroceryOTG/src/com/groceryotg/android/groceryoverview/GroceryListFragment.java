package com.groceryotg.android.groceryoverview;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.R;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.ServerURL;

import java.util.ArrayList;
import java.util.List;


/**
 * User: robert
 * Date: 16/03/13
 */
public class GroceryListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String CATEGORY_POSITION = "position";
    SimpleCursorAdapter adapter;
    TextView emptyTextView;
    Menu menu;

    private Integer categoryId;

    static GroceryListFragment newInstance(int pos) {
        GroceryListFragment f = new GroceryListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(CATEGORY_POSITION, pos);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        categoryId = getArguments() != null ? getArguments().getInt(CATEGORY_POSITION) : 1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grocery_pager_menu, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
//                refreshCurrentPager();
                return true;
            case R.id.map:
//                launchMapActivity();
                return true;
            case R.id.shop_cart:
//                launchShopCartActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.grocery_fragment_list, container, false);
        emptyTextView = (TextView) v.findViewById(R.id.empty_grocery_list);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayEmptyListMessage(buildNoNewContentString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillData();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v.findViewById(R.id.grocery_row_label);
        TextView idView = (TextView) v.findViewById(R.id.grocery_row_id);

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_ID, idView.getText().toString());
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, textView.getText().toString());

        getActivity().getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);

        Bundle b = new Bundle();
        b.putString("query", GroceryFragmentActivity.mQuery);
        b.putBoolean("reload", true);
        getLoaderManager().restartLoader(0, b, this);
        Toast t = Toast.makeText(getActivity(), "Item added to Shopping Cart", Toast.LENGTH_SHORT);
        t.show();
    }

    private void fillData() {
        String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                CartTable.COLUMN_CART_GROCERY_ID};
        int[] to = new int[]{R.id.grocery_row_id,
                R.id.grocery_row_label,
                R.id.grocery_row_details,
                R.id.grocery_row_price,
                R.id.grocery_row_store,
                R.id.grocery_row_inshopcart};

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.grocery_fragment_row, null, from, to, 0);
        adapter.setViewBinder(new GroceryViewBinder());

        setListAdapter(adapter);

        // Prepare the asynchronous loader.
        Bundle b = new Bundle();
        b.putString("query", "");
        b.putBoolean("reload", false);
        getLoaderManager().initLoader(0, b, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String emptyString = bundle.getBoolean("reload") ? buildNoSearchResultString() : buildNoNewContentString();
        String query = bundle.getString("query");
        displayEmptyListMessage(emptyString);

        List<String> selectionArgs = new ArrayList<String>();

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                CartTable.COLUMN_CART_GROCERY_ID};
        String selection = GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
        selectionArgs.add(categoryId.toString());

        // If user entered a search query, filter the results based on grocery name
        if (!query.isEmpty()) {
            selection += " AND " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + query + "%");
        }
        if (GroceryFragmentActivity.storeSelected != null && GroceryFragmentActivity.storeSelected.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < GroceryFragmentActivity.storeSelected.size(); storeNum++) {
                if (GroceryFragmentActivity.storeSelected.valueAt(storeNum) == GroceryFragmentActivity.SELECTED) {
                    if (storeSelection.isEmpty()) {
                        storeSelection = " AND (";
                        storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    } else {
                        storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    }
                    selectionArgs.add(((Integer) GroceryFragmentActivity.storeSelected.keyAt(storeNum)).toString());
                }
            }
            if (!storeSelection.isEmpty()) {
                storeSelection += ")";
                selection += storeSelection;
            }
        }
        if (GroceryFragmentActivity.mPriceRangeMin != null) {
            selection += " AND " + GroceryTable.COLUMN_GROCERY_PRICE + " >= ?";
            selectionArgs.add(GroceryFragmentActivity.mPriceRangeMin.toString());
        }
        if (GroceryFragmentActivity.mPriceRangeMax != null) {
            selection += " AND " + GroceryTable.COLUMN_GROCERY_PRICE + " <= ?";
            selectionArgs.add(GroceryFragmentActivity.mPriceRangeMax.toString());
        }

        final String[] selectionArgsArr = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArgsArr);
        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    private void displayEmptyListMessage(String emptyStringMsg) {
        ListView myListView = this.getListView();
        emptyTextView.setText(emptyStringMsg);
        myListView.setEmptyView(emptyTextView);
    }

    private String buildNoNewContentString() {
        String emptyStringFormat = getResources().getString(R.string.no_new_content);
        return (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
    }

    private String buildNoSearchResultString() {
        return getResources().getString(R.string.no_search_results);
    }
}
