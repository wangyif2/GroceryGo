package com.groceryotg.android;

import android.app.LoaderManager;
import android.content.*;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.groceryoverview.GroceryFragmentActivity;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.utils.RefreshAnimation;
import com.slidingmenu.lib.SlidingMenu;

import java.util.Locale;


public class CategoryOverView extends SherlockActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private GridView gridview;
    private SlidingMenu slidingMenu;
    private Menu menu;
    private MenuItem refreshItem;
    public static Context context;
    RefreshStatusReceiver mRefreshStatusReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getBaseContext();

        setContentView(R.layout.category_list);

        // By default, the Home button in the ActionBar is interactive. Since this
        // is the home screen (and it doesn't make sense to navigate up from the home screen)
        // the line below disables the button.
        getSupportActionBar().setHomeButtonEnabled(false);


        // Set adapter for the grid view
        configGridView();

        // Populate the grid with data
        fillData();

        // Configure the SlidingMenu
        configSlidingMenu();

        mRefreshStatusReceiver = new RefreshStatusReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter mStatusIntentFilter = new IntentFilter(NetworkHandler.REFRESH_COMPLETED_ACTION);
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshStatusReceiver, mStatusIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshStatusReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.categoryoverview_menu, menu);
        this.menu = menu;
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
//            case R.id.homeAsUp:
            // Toggle the sliding slidingMenu
            //toggle();
        }
        return super.onOptionsItemSelected(item);
    }

    private void configGridView() {
        // Set adapter for the grid view
        gridview = (GridView) findViewById(R.id.gridview);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(CategoryOverView.this, GroceryFragmentActivity.class);
                Uri uri = Uri.parse(GroceryotgProvider.CONTENT_URI_CAT + "/" + id);
                intent.putExtra(GroceryotgProvider.CONTENT_ITEM_TYPE_CAT, uri);
                startActivity(intent);
            }
        });
        gridview.setEmptyView(findViewById(R.id.empty_category_list));
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
                    if (slidingMenu.isMenuShowing())
                        slidingMenu.showContent();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_cart))) {
                    // Selected Shopping Cart
                    launchShopCartActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_map))) {
                    // Selected Map
                    launchMapActivity();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_sync))) {
                    // Selected Sync
                    refreshCurrentCategory();
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

    private void fillData() {
        String[] from = new String[]{CategoryTable.COLUMN_CATEGORY_NAME};
        int[] to = new int[]{R.id.category_row_label};

        getLoaderManager().initLoader(0, null, this);
        adapter = new CategoryGridCursorAdapter(this, R.layout.category_row, null, from, to);

        gridview.setAdapter(adapter);
    }

    private void refreshCurrentCategory() {
        if (slidingMenu.isMenuShowing())
            slidingMenu.showContent();

        refreshItem = menu.findItem(R.id.refresh);
        RefreshAnimation.refreshIcon(context, true, refreshItem);

        Intent intent = new Intent(context, NetworkHandler.class);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
        startService(intent);
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

    public class CategoryGridCursorAdapter extends SimpleCursorAdapter {
        private Context mContext;
        private int mLayout;
        private int[] gridColours;
        private TypedArray gridIcons;

        @SuppressWarnings("deprecation")
        public CategoryGridCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            this.mContext = context;
            this.mLayout = layout;

            // An array of colours for solid background fill
            String[] allColours = mContext.getResources().getStringArray(R.array.colours);
            gridColours = new int[allColours.length];
            for (int i = 0; i < allColours.length; i++) {
                gridColours[i] = Color.parseColor(allColours[i]);
            }

            // An array of icons (to use instead of colours, when available)
            gridIcons = mContext.getResources().obtainTypedArray(R.array.icons_arr);

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            Cursor c = getCursor();
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(mLayout, parent, false);

            // Get the next row from the cursor
            String colName = CategoryTable.COLUMN_CATEGORY_NAME;
            String categoryName = c.getString(c.getColumnIndex(colName));

            // Set the name of the next category in the grid view
            TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
            if (name_text != null) {
                name_text.setText(categoryName);
            }

            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor c) {

            // Get the next row from the cursor
            String colName = CategoryTable.COLUMN_CATEGORY_NAME;
            String categoryName = c.getString(c.getColumnIndex(colName)).toLowerCase(Locale.CANADA);

            // TODO: Update category names in database
            if (categoryName.equalsIgnoreCase("Miscellaneous")) {
                categoryName = "misc";
            } else if (categoryName.equalsIgnoreCase("Fruits and Vegetables")) {
                categoryName = "fruit & veg";
            } else if (categoryName.equalsIgnoreCase("Bread and Bakery")) {
                categoryName = "bread";
            } else if (categoryName.equalsIgnoreCase("Beverages")) {
                categoryName = "drinks";
            }


            // Set the name of the next category in the grid view
            TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
            if (name_text != null) {
                name_text.setText(categoryName);
            }

            int position = c.getPosition();

            if (position < gridIcons.length()) {
                v.setBackgroundResource(gridIcons.getResourceId(position % gridIcons.length(), 0));
            } else {
                v.setBackgroundColor(gridColours[position % gridColours.length]);
            }
        }

        // TODO: When should this be called?
        public void cleanUp() {
            // Recycle the obtained type array when done using the adapter
            gridIcons.recycle();
        }
    }


    public static Context getContext() {
        return context;
    }

    private class RefreshStatusReceiver extends BroadcastReceiver {
        private RefreshStatusReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int resultCode = bundle.getInt(NetworkHandler.CONNECTION_STATE);

            Toast toast = null;
            RefreshAnimation.refreshIcon(context, false, refreshItem);
            if (resultCode == NetworkHandler.CONNECTION) {
                toast = Toast.makeText(context, "Groceries Updated", Toast.LENGTH_LONG);
            } else if (resultCode == NetworkHandler.NO_CONNECTION) {
                toast = Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG);
            }
            assert toast != null;
            toast.show();
        }
    }
}