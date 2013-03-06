package com.groceryotg.android;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.groceryotg.android.services.Location.LocationMonitor;
import com.groceryotg.android.services.Location.LocationReceiver;
import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.utils.RefreshAnimation;
import com.slidingmenu.lib.SlidingMenu;


public class CategoryOverView extends SherlockActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private GridView gridview;
    private SlidingMenu slidingMenu;
    private Menu menu;
    private MenuItem refreshItem;
    public static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getBaseContext();

        setContentView(R.layout.category_list);

        // By default, the Home button in the ActionBar is interactive. Since this
        // is the home screen (and it doesn't make sense to navigate up from the home screen)
        // the line below disables the button.
        getSupportActionBar().setHomeButtonEnabled(false);

        // Configure the SlidingMenu
        configSlidingMenu();

        // Set adapter for the grid view
        configGridView();

        // Populate the grid with data
        fillData();

        // Setup alarm for polling of location data
        configLocationPoll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
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

    private void configLocationPoll() {
        AlarmManager locationAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent locationIntent = new Intent(this, LocationMonitor.class);
        locationIntent.putExtra(LocationMonitor.EXTRA_INTENT, new Intent(this, LocationReceiver.class));
        locationIntent.putExtra(LocationMonitor.EXTRA_PROVIDER, LocationManager.NETWORK_PROVIDER);
        PendingIntent locationPendingIntent = PendingIntent.getBroadcast(this, 0, locationIntent, 0);
        locationAlarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), LocationReceiver.pollingPeriod, locationPendingIntent);
    }

    private void configGridView() {
        gridview = (GridView) findViewById(R.id.gridview);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(CategoryOverView.this, GroceryOverView.class);
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
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
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
                android.R.layout.simple_list_item_1, android.R.id.text1, slidingMenuItems);
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
                        /* TODO: toggle() only works in a SlidingFragmentActivity, but converting this activity
                         * to a SlidingFragmentActivity leads to several issues: (1) sliding slidingMenu is sometimes blank
                         * (2) slidingmenu is sometimes fullscreen (3) Clicking on the home icon of the ActionBar
                         * causes the app to crash if homeAsUp is enabled.
                         */
                    //toggle();
                    startActivity(new Intent(CategoryOverView.this, CategoryOverView.class));
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
                    //toggle();
                } else if (selectedItem.equalsIgnoreCase(getString(R.string.slidingmenu_item_about))) {
                    // Selected About
                    //startActivity(new Intent(CategoryOverView.this, About.class));
                    //toggle();
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
        PendingIntent pi = createPendingResult(1, intent, PendingIntent.FLAG_ONE_SHOT);
        intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
        intent.putExtra("pendingIntent", pi);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast toast = null;
        RefreshAnimation.refreshIcon(context, false, refreshItem);
        if (resultCode == NetworkHandler.CONNECTION) {
            toast = Toast.makeText(context, "Categories Updated", Toast.LENGTH_LONG);
        } else if (resultCode == NetworkHandler.NO_CONNECTION) {
            toast = Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG);
        }
        assert toast != null;
        toast.show();
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

        @SuppressWarnings("deprecation")
        public CategoryGridCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            this.mContext = context;
            this.mLayout = layout;

            String[] allColours = mContext.getResources().getStringArray(R.array.colours);
            gridColours = new int[allColours.length];

            for (int i = 0; i < allColours.length; i++) {
                gridColours[i] = Color.parseColor(allColours[i]);
            }
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
            String categoryName = c.getString(c.getColumnIndex(colName));

            // Set the name of the next category in the grid view
            TextView name_text = (TextView) v.findViewById(R.id.category_row_label);
            if (name_text != null) {
                name_text.setText(categoryName);
            }

            int position = c.getPosition();
            v.setBackgroundColor(gridColours[position % gridColours.length]);
        }

    }

    public static Context getContext() {
        return context;
    }
}
