package com.groceryotg.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.*;
import com.groceryotg.android.database.CartTable;
import com.groceryotg.android.database.FlyerTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.settings.SettingsManager;
import com.groceryotg.android.utils.RefreshAnimation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * User: robert
 * Date: 16/03/13
 */
public class GroceryListFragment extends SherlockListFragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String CATEGORY_POSITION = "position";
    Activity activity;
    GroceryListCursorAdapter adapter;
    TextView emptyTextView;
    ProgressBar progressView;
    Menu menu;
    MenuItem refreshItem;
    private Integer categoryId;
    boolean isSearch;

    private CharSequence[] items;
    private boolean[] states;
    private SparseIntArray mapIndexToId; // maps index in the dialog to store_id

    private SearchView mSearchView;
    ViewGroup myViewGroup;

    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;

    public static GroceryListFragment newInstance(int pos) {
        GroceryListFragment f = new GroceryListFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(CATEGORY_POSITION, pos);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        categoryId = getArguments() != null ? getArguments().getInt(CATEGORY_POSITION) - 1 : 1;
        watchSettings();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (RefreshAnimation.isInProgress()) {
            RefreshAnimation.setInProgress(false);
            RefreshAnimation.getRefresh().getActionView().clearAnimation();
            RefreshAnimation.getRefresh().setActionView(null);
        }
        menu.clear();
        inflater.inflate(R.menu.grocery_pager_menu1, menu);
        this.menu = menu;
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshGrocery();
                return true;
            case R.id.filter:
                launchFilterDialog();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myViewGroup = container;
        View v = inflater.inflate(R.layout.grocery_fragment_list, container, false);
        emptyTextView = (TextView) v.findViewById(R.id.empty_grocery_list);
        progressView = (ProgressBar) v.findViewById(R.id.refresh_progress);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (progressView != null)
            progressView.setVisibility(View.VISIBLE);

//        displayEmptyListMessage(buildNoNewContentString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        fillData();
        String[] from = new String[]{GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                FlyerTable.COLUMN_FLYER_ID,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST};
        int[] to = new int[]{R.id.grocery_row_id,
                R.id.grocery_row_label,
                R.id.grocery_row_details,
                R.id.grocery_row_price,
                R.id.grocery_row_store,
                R.id.grocery_row_store_id,
                R.id.grocery_row_inshopcart,
                R.id.grocery_row_inwatchlist,
                R.id.grocery_row_inshopcart_flag,
                R.id.grocery_row_inwatchlist_flag};

        adapter = new GroceryListCursorAdapter(getActivity(), R.layout.grocery_fragment_row, null, from, to);
        adapter.setViewBinder(new GroceryViewBinder());

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView textView = (TextView) v.findViewById(R.id.grocery_row_label);
        TextView idView = (TextView) v.findViewById(R.id.grocery_row_id);

        ContentValues values = new ContentValues();
        values.put(CartTable.COLUMN_CART_GROCERY_ID, idView.getText().toString());
        values.put(CartTable.COLUMN_CART_GROCERY_NAME, textView.getText().toString());
        values.put(CartTable.COLUMN_CART_FLAG_SHOPLIST, CartTable.FLAG_TRUE);
        values.put(CartTable.COLUMN_CART_FLAG_WATCHLIST, CartTable.FLAG_FALSE);

        getActivity().getContentResolver().insert(GroceryotgProvider.CONTENT_URI_CART_ITEM, values);

        Bundle b = new Bundle();
        b.putString("query", GroceryFragmentActivity.myQuery);
        b.putBoolean("reload", true);
        getLoaderManager().restartLoader(0, b, this);
        Toast t = Toast.makeText(getActivity(), "Item added to Shopping Cart", Toast.LENGTH_SHORT);
        t.show();
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String newQuery = !TextUtils.isEmpty(query) ? query : null;

        // Don't do anything if the query hasn't changed
        if (newQuery == null && GroceryFragmentActivity.myQuery == null ||
                newQuery != null && GroceryFragmentActivity.myQuery.equals(newQuery))
            return true;

        Intent globalSearchIntent = new Intent(getActivity(), GlobalSearchActivity.class);
        globalSearchIntent.putExtra(GlobalSearchActivity.GLOBAL_SEARCH, true);
        globalSearchIntent.putExtra(SearchManager.QUERY, newQuery);
        globalSearchIntent.setAction(Intent.ACTION_SEARCH);
        startActivity(globalSearchIntent);

        return true;
    }

    public boolean handleVoiceSearch(String query) {
        mSearchView.setQuery(query, true);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void launchShopCartActivity() {
        Intent intent = new Intent(getActivity(), ShopCartOverView.class);
        startActivity(intent);
    }

    private void launchMapActivity() {
        Intent intent = new Intent(getActivity(), GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        isSearch = bundle.getBoolean("reload");
        String query = bundle.getString("query").trim();

        List<String> selectionArgs = new ArrayList<String>();

        String[] projection = {GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_ID,
                GroceryTable.COLUMN_GROCERY_ID,
                GroceryTable.COLUMN_GROCERY_NAME,
                GroceryTable.COLUMN_GROCERY_PRICE,
                StoreParentTable.COLUMN_STORE_PARENT_NAME,
                FlyerTable.TABLE_FLYER + "." + FlyerTable.COLUMN_FLYER_ID,
                CartTable.COLUMN_CART_GROCERY_ID,
                CartTable.COLUMN_CART_FLAG_SHOPLIST,
                CartTable.COLUMN_CART_FLAG_WATCHLIST};
        String selection = GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_CATEGORY + "=?";
        selectionArgs.add(categoryId.toString());

        // If user entered a search query, filter the results based on grocery name
        if (!query.isEmpty()) {
            selection += " AND " + GroceryTable.TABLE_GROCERY + "." + GroceryTable.COLUMN_GROCERY_NAME + " LIKE ?";
            selectionArgs.add("%" + query + "%");
        }
        SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(activity);
        if (selectedStores != null && selectedStores.size() > 0) {
            // Go through selected stores and add them to query
            String storeSelection = "";
            for (int storeNum = 0; storeNum < selectedStores.size(); storeNum++) {
                if (selectedStores.valueAt(storeNum) == true) {
                    if (storeSelection.isEmpty()) {
                        storeSelection = " AND (";
                        storeSelection += StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    } else {
                        storeSelection += " OR " + StoreParentTable.TABLE_STORE_PARENT + "." + StoreParentTable.COLUMN_STORE_PARENT_ID + " = ?";
                    }
                    selectionArgs.add(((Integer) selectedStores.keyAt(storeNum)).toString());
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
        return new CursorLoader(getActivity(), GroceryotgProvider.CONTENT_URI_GRO_JOINSTORE, projection, selection, selectionArgsArr, GroceryTable.COLUMN_GROCERY_SCORE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
        if (progressView != null)
            progressView.setVisibility(View.GONE);

        if (cursor.getCount() == 0)
            if (isSearch)
                displayEmptyListMessage(buildNoSearchResultString());
            else
                displayEmptyListMessage(buildNoNewContentString());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    private void refreshGrocery() {
        refreshItem = menu.findItem(R.id.refresh);
        RefreshAnimation.refreshIcon(getActivity(), true, refreshItem);

        Intent intent = new Intent(getActivity(), NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        getActivity().startService(intent);
    }

    private void launchFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.groceryoverview_filter_title);
        initFilter();
        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SparseBooleanArray selectedItems = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                SparseBooleanArray selectedStores = new SparseBooleanArray();
                for (int i = 0; i < selectedItems.size(); i++) {
                    int key_index = selectedItems.keyAt(i);
                    selectedStores.append(mapIndexToId.get(key_index), selectedItems.valueAt(i));
                }
                SettingsManager.setStoreFilter(activity, selectedStores);
                loadDataWithQuery(true, GroceryFragmentActivity.myQuery);
                Toast.makeText(getActivity(), getResources().getString(R.string.groceryoverview_filter_updated), Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public void loadDataWithQuery(Boolean reload, String query) {
        Bundle b = new Bundle();
        b.putString("query", query);
        if (reload) {
            GroceryFragmentActivity.setMyQuery(query);
            b.putBoolean("reload", true);
            getLoaderManager().restartLoader(0, b, this);
        } else {
            b.putBoolean("reload", false);
            getLoaderManager().restartLoader(0, b, this);
        }
    }

    public void fillData() {
        // Prepare the asynchronous loader.
        Bundle b = new Bundle();
        b.putString("query", "");
        b.putBoolean("reload", false);
        getLoaderManager().initLoader(0, b, this);
    }

    public void displayEmptyListMessage(String emptyStringMsg) {
        ListView myListView = this.getListView();
        emptyTextView.setText(emptyStringMsg);
        emptyTextView.setVisibility(View.VISIBLE);
        myListView.setEmptyView(emptyTextView);
    }

    public String buildNoNewContentString() {
        String emptyStringFormat = getResources().getString(R.string.no_new_content);
        return (ServerURL.getLastRefreshed() == null) ? String.format(emptyStringFormat, " Never") : String.format(emptyStringFormat, ServerURL.getLastRefreshed());
    }

    public String buildNoSearchResultString() {
        return getResources().getString(R.string.no_search_results);
    }

    private void initFilter() {
        this.items = new CharSequence[GroceryFragmentActivity.storeNames.keySet().size()];
        this.states = new boolean[GroceryFragmentActivity.storeNames.keySet().size()];
        this.mapIndexToId = new SparseIntArray(); // maps index in the dialog to store_id

        Iterator<Map.Entry<Integer, String>> it = GroceryFragmentActivity.storeNames.entrySet().iterator();
        Integer indexer = 0;
        SparseBooleanArray selectedStores = SettingsManager.getStoreFilter(activity);
        while (it.hasNext()) {
            Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>) it.next();
            this.items[indexer] = pairs.getValue();
            this.states[indexer] = selectedStores.get(pairs.getKey(), false);
            this.mapIndexToId.put(indexer, pairs.getKey());
            indexer++;
        }
    }

    private void watchSettings() {
        mSettingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                loadDataWithQuery(true, GroceryFragmentActivity.myQuery);
            }
        };
        SettingsManager.getPrefs(activity).registerOnSharedPreferenceChangeListener(mSettingsListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingsManager.getPrefs(activity).unregisterOnSharedPreferenceChangeListener(mSettingsListener);
    }
}
