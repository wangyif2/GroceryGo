package com.groceryotg.android.groceryoverview;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.CategoryOverView;
import com.groceryotg.android.GroceryMapView;
import com.groceryotg.android.R;
import com.groceryotg.android.ShopCartOverView;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.services.Location.GroceryOTGUtils;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.utils.RefreshAnimation;
import com.slidingmenu.lib.SlidingMenu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: robert
 * Date: 16/03/13
 */
public class GroceryFragmentActivity extends SherlockFragmentActivity implements OnQueryTextListener, OnCloseListener {
    static HashMap<Integer, String> categories;

    public static Context mContext;
    public static ViewPager mPager;
    GroceryAdapter mAdapter;
    SlidingMenu mSlidingMenu;
    RefreshStatusReceiver mRefreshStatusReceiver;
    MenuItem refreshItem;
    Menu menu;

    public static String mQuery;
    private SearchView mSearchView;

    // Filters
    public static Map<Integer, String> storeNames;
    public static SparseIntArray storeSelected;
    private Integer subcategoryId;
    public static Double mPriceRangeMin;
    public static Double mPriceRangeMax;

    public static final Integer SELECTED = 1;
    public static final Integer NOT_SELECTED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grocery_pager);

        Bundle extras = getIntent().getExtras();
        categories = getCategoryInfo();
        mContext = this;
        setmQuery("");

        initStores();

        configActionBar();

        configViewPager();

        configSlidingMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshStatusReceiver = new RefreshStatusReceiver();
        IntentFilter mStatusIntentFilter = new IntentFilter(NetworkHandler.REFRESH_COMPLETED_ACTION);
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshStatusReceiver, mStatusIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshStatusReceiver);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getSupportMenuInflater();
//        inflater.inflate(R.menu.grocery_pager_menu, menu);
//        this.menu = menu;
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.refresh:
//                refreshCurrentPager();
//                return true;
//            case R.id.map:
//                launchMapActivity();
//                return true;
//            case R.id.shop_cart:
//                launchShopCartActivity();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Gets the search query from the voice recognizer intent
            String query = intent.getStringExtra(SearchManager.QUERY);

            // Set the search box text to the received query and submit the search
            mSearchView.setQuery(query, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.grocery_pager_menu1, menu);

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
                refreshCurrentPager();
                return true;
            case R.id.filter:
                launchFilterDialog();
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

    private HashMap<Integer, String> getCategoryInfo() {
        HashMap<Integer, String> categories = new HashMap<Integer, String>();
        Cursor c = getContentResolver().query(GroceryotgProvider.CONTENT_URI_CAT, null, null, null, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            categories.put(
                    c.getInt(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_ID)),
                    c.getString(c.getColumnIndexOrThrow(CategoryTable.COLUMN_CATEGORY_NAME)));
            c.moveToNext();
        }
        return categories;
    }

    private void initStores() {
        // Initialize the list of stores from database
        storeSelected = new SparseIntArray();
        storeNames = new HashMap<Integer, String>();

        Cursor storeCursor = GroceryOTGUtils.getStoreParentNames(this);
        if (storeCursor != null) {
            storeCursor.moveToFirst();
            while (!storeCursor.isAfterLast()) {
                storeSelected.put(storeCursor.getInt(storeCursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID)), SELECTED);
                storeNames.put(storeCursor.getInt(storeCursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID)),
                                storeCursor.getString(storeCursor.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME)));
                storeCursor.moveToNext();
            }
        }
    }

    private void loadDataWithQuery(Boolean reload, String query) {
        int index = mPager.getCurrentItem();
        GroceryAdapter adapter = ((GroceryAdapter)mPager.getAdapter());
        GroceryListFragment fragment = adapter.getFragment(index);

        Bundle b = new Bundle();
        b.putString("query", query);
        if (reload) {
            setmQuery(query);
            b.putBoolean("reload", true);
            fragment.getLoaderManager().restartLoader(0, b, fragment);
        } else {
            b.putBoolean("reload", false);
            fragment.getLoaderManager().restartLoader(0, b, fragment);
        }

    }

    public void setmQuery(String mQuery) {
        this.mQuery = mQuery;
    }

    private void configActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void configViewPager() {
        mAdapter = new GroceryAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
    }

    private void configSlidingMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(R.layout.menu_frame);

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

        menuView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Switch activity based on what mSlidingMenu item the user selected
                TextView textView = (TextView) view;
                String selectedItem = textView.getText().toString();

                if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cat))) {
                    // Selected Categories
                    if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                    mPager.setCurrentItem(0);
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cart))) {
                    // Selected Shopping Cart
                    launchShopCartActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
                    refreshCurrentPager();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_settings))) {
                    // Selected Settings
                    if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_about))) {
                    // Selected About
                    //startActivity(new Intent(CategoryOverView.this, About.class));
                    if (mSlidingMenu.isMenuShowing())
                        mSlidingMenu.showContent();
                }
            }
        });
    }

    private void launchFilterDialog() {
        final CharSequence[] items = new CharSequence[storeNames.keySet().size()];
        final boolean[] states = new boolean[storeNames.keySet().size()];
        final SparseIntArray mapIndexToId = new SparseIntArray(); // maps index in the dialog to store_id

        Iterator<Map.Entry<Integer, String>> it = storeNames.entrySet().iterator();
        Integer indexer = 0;
        while (it.hasNext()) {
            Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>) it.next();
            items[indexer] = pairs.getValue();
            states[indexer] = (storeSelected.get(pairs.getKey()) == SELECTED ? true : false);
            mapIndexToId.put(indexer, pairs.getKey());
            indexer++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.groceryoverview_filter_title);
        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SparseBooleanArray selectedItems = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                for (int i = 0; i < selectedItems.size(); i++) {
                    int key_index = selectedItems.keyAt(i);
                    if (selectedItems.get(key_index)) {
                        storeSelected.put(mapIndexToId.get(key_index), SELECTED);
                    } else {
                        storeSelected.put(mapIndexToId.get(key_index), NOT_SELECTED);
                    }
                }
                Toast.makeText(getBaseContext(), getResources().getString(R.string.groceryoverview_filter_updated), Toast.LENGTH_LONG).show();
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

    private void launchShopCartActivity() {
        Intent intent = new Intent(this, ShopCartOverView.class);
        startActivity(intent);
    }

    private void launchMapActivity() {
        Intent intent = new Intent(this, GroceryMapView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void refreshCurrentPager() {
        if (mSlidingMenu.isMenuShowing())
            mSlidingMenu.showContent();

        refreshItem = menu.findItem(R.id.refresh);
        RefreshAnimation.refreshIcon(mContext, true, refreshItem);

        Intent intent = new Intent(mContext, NetworkHandler.class);
        if (mPager.getCurrentItem() == 0)
            intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
        else
            intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.GRO);
        startService(intent);
    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }

        // Refresh the list to display all items again and hide the search view
        loadDataWithQuery(true, "");
        mSearchView.setVisibility(SearchView.GONE);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
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
        return false;
    }

    private class RefreshStatusReceiver extends BroadcastReceiver {
        private RefreshStatusReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getBundleExtra("bundle").getInt(NetworkHandler.CONNECTION_STATE);

            Toast toast = null;
            RefreshAnimation.refreshIcon(context, false, refreshItem);
            if (resultCode == NetworkHandler.CONNECTION) {
                toast = Toast.makeText(mContext, "Groceries Updated", Toast.LENGTH_LONG);
            } else if (resultCode == NetworkHandler.NO_CONNECTION) {
                toast = Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG);
            }
            assert toast != null;
            toast.show();
        }
    }

    public static class GroceryAdapter extends FragmentStatePagerAdapter {

        private HashMap<Integer, GroceryListFragment> mPageReferenceMap;

        public GroceryAdapter(FragmentManager fm) {
            super(fm);
            mPageReferenceMap = new HashMap<Integer, GroceryListFragment>();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "categories overview";
            } else
                return categories.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return new CategoryGridFragment();
            } else {
                GroceryListFragment myFragment = GroceryListFragment.newInstance(i);
                mPageReferenceMap.put(i, myFragment);
                return myFragment;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mPageReferenceMap.remove(position);
        }

        @Override
        public int getCount() {
            //the plus 1 here is for the overview front page
            return categories.size() + 1;
        }

        public GroceryListFragment getFragment(int key) {
            return mPageReferenceMap.get(key);
        }
    }
}
