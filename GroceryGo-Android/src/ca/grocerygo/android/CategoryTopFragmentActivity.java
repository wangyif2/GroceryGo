package ca.grocerygo.android;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import ca.grocerygo.android.fragment.ToggleLocationDialogFragment;
import ca.grocerygo.android.fragment.ToggleWifiDialogFragment;
import ca.grocerygo.android.gcm.GroceryGCMBroadcastReceiver;
import ca.grocerygo.android.services.NetworkHandler;
import ca.grocerygo.android.utils.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CategoryTopFragmentActivity extends SherlockFragmentActivity {
    public static final String INTENT_EXTRA_FLAG_LOCATION_SERVICE_BAD = "intent_extra_flag_location_service_bad";
    public static final String INTENT_EXTRA_FLAG_LOCATION_NOT_SUPPORTED = "intent_extra_flag_location_not_supported";
    private static final String SETTINGS_IS_REFRESHING = "isRefreshing";
    private Context mContext;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    RefreshStatusReceiver mRefreshStatusReceiver;
    MenuItem refreshItem;
    private Menu mMenu;

    public static Double mPriceRangeMin;
    public static Double mPriceRangeMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity);

        mContext = this;

        GroceryGoUtils.NavigationDrawerBundle drawerBundle = GroceryGoUtils.configNavigationDrawer(this, true, R.string.title_main);
        this.mDrawerLayout = drawerBundle.getDrawerLayout();
        this.mDrawerList = drawerBundle.getDrawerList();
        this.mDrawerToggle = drawerBundle.getDrawerToggle();

        handleIntent(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isLocAva = settings.getBoolean(GroceryGoUtils.LOCATION_IS_LOATION_AVA, false);
        boolean isLocTogglePromted = settings.getBoolean(GroceryGoUtils.LOCATION_IS_LOATION_TOGGLE_PROMTED, false);

        // If we see that user has disabled all locations, we prompt them to use it.
        if (!isLocAva & !isLocTogglePromted) {
            ToggleLocationDialogFragment toggleLocDialog = new ToggleLocationDialogFragment();
            toggleLocDialog.show(this.getSupportFragmentManager(), "toggle_location_dialog");

            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putBoolean(GroceryGoUtils.LOCATION_IS_LOATION_TOGGLE_PROMTED, true);
            settingsEditor.commit();

        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Gets the search query from the voice recognizer intent
            //String query = intent.getStringExtra(SearchManager.QUERY);

            // Collapse the search view as a search is performed
            MenuItem searchItem = mMenu.findItem(R.id.search);
            SearchView searchView = (SearchView) mMenu.findItem(R.id.search).getActionView();
            searchItem.collapseActionView();
            searchView.setQuery("", false);

            // If on the home page and doing a global search, send the intent
            // to the GlobalSearchActivity
            Intent globalSearchIntent = new Intent(this, GlobalSearchFragmentActivity.class);
            GroceryGoUtils.copyIntentData(intent, globalSearchIntent);
            globalSearchIntent.putExtra(GlobalSearchFragmentActivity.GLOBAL_SEARCH, true);
            startActivity(globalSearchIntent);
        } else {
            boolean localizationWarningFlag;
            localizationWarningFlag = intent.getBooleanExtra(INTENT_EXTRA_FLAG_LOCATION_SERVICE_BAD, false);
            if (localizationWarningFlag) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Could not determine location. Sales data will not be available. Make sure location sharing is turned on to get the most out of this app.").setTitle("WARNING");
                builder.setPositiveButton("Continue", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            localizationWarningFlag = intent.getBooleanExtra(INTENT_EXTRA_FLAG_LOCATION_NOT_SUPPORTED, false);
            if (localizationWarningFlag) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Your location is not fully supported. Sales data will not be available.").setTitle("WARNING");
                builder.setPositiveButton("Continue", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        promptIfEmptyDb(this);

        mRefreshStatusReceiver = new RefreshStatusReceiver();
        IntentFilter mStatusIntentFilter = new IntentFilter(NetworkHandler.REFRESH_COMPLETED_ACTION);
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshStatusReceiver, mStatusIntentFilter);

        // show a changelog dialog if it's new
        ChangeLogDialog cd = new ChangeLogDialog(this);
        cd.show();

        if (refreshItem != null)
            RefreshAnimation.refreshIcon(mContext, false, refreshItem);

        invalidateOptionsMenu();
        refreshItem = null;
    }

    private void promptIfEmptyDb(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDBPopulated = settings.getBoolean(SplashScreenActivity.SETTINGS_IS_DB_POPULATED, false);

        if (!ServerURLs.checkNetworkStatus(getBaseContext()) && !isDBPopulated) {
            ToggleWifiDialogFragment toggleWifiDialog = new ToggleWifiDialogFragment();
            toggleWifiDialog.show(this.getSupportFragmentManager(), "toggle_wifi_dialog");
        } else if (ServerURLs.checkNetworkStatus(getBaseContext()) && !isDBPopulated) {
            SharedPreferences.Editor settingsEditor = settings.edit();
            settingsEditor.putBoolean(GroceryGCMBroadcastReceiver.SETTINGS_IS_NEW_DATA_AVA, true);
            settingsEditor.commit();
        }
    }

    @Override
    protected void onPause() {
        GroceryRefreshTrigger.stopAll(this);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshStatusReceiver);

        RefreshAnimation.refreshIcon(this, false, refreshItem);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor settingsEditor = settings.edit();

        if (settings.getBoolean(SETTINGS_IS_REFRESHING, false)) {
            settingsEditor.putBoolean(GroceryGCMBroadcastReceiver.SETTINGS_IS_NEW_DATA_AVA, true);
            settingsEditor.putBoolean(SETTINGS_IS_REFRESHING, false);
            settingsEditor.commit();
        }
        invalidateOptionsMenu();
        refreshItem = null;

        Crouton.cancelAllCroutons();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isRefreshing = settings.getBoolean(CategoryTopFragmentActivity.SETTINGS_IS_REFRESHING, false);
        boolean isNewDataAva = settings.getBoolean(GroceryGCMBroadcastReceiver.SETTINGS_IS_NEW_DATA_AVA, false);

        menu.clear();
        configSearchView(menu);

        if (isRefreshing) {
            refreshItem = mMenu.findItem(R.id.refresh);
            Log.i(GroceryApplication.TAG, "refreshItem is: " + (refreshItem == null));
            if (refreshItem == null) {
                Log.i(GroceryApplication.TAG, "refreshItem is: null, gonna add it");
                menu.add(0, R.id.refresh, Menu.NONE, R.string.navdrawer_item_sync).setIcon(R.drawable.ic_menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                refreshItem = mMenu.findItem(R.id.refresh);
                if (refreshItem != null) {
                    Log.i(GroceryApplication.TAG, "refreshItem is: added, gonna start animation");
                    RefreshAnimation.refreshIcon(mContext, true, refreshItem);
                }
            }

            Style INFINITE = new Style.Builder().setBackgroundColorValue(Style.holoBlueLight).build();
            Configuration CONFIGURATION_INFINITE = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
            Crouton.makeText(this, R.string.gcm_newdata_notification, INFINITE).setConfiguration(CONFIGURATION_INFINITE).show();
        } else if (isNewDataAva) {
            if (refreshItem == null) {
                menu.add(0, R.id.refresh, Menu.NONE, R.string.navdrawer_item_sync).setIcon(R.drawable.ic_menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

            Style INFINITE = new Style.Builder().setBackgroundColorValue(Style.holoBlueLight).build();
            Configuration CONFIGURATION_INFINITE = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
            Crouton.makeText(this, R.string.gcm_newdata_notification, INFINITE).setConfiguration(CONFIGURATION_INFINITE).show();
        }

        if (this.mDrawerLayout != null && this.mDrawerList != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setVisible(!mDrawerLayout.isDrawerOpen(mDrawerList));
            }
        }

        return super.onPrepareOptionsMenu(menu);

    }

    private void configSearchView(Menu menu) {
        // Create a blank SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = new SearchView(getSherlock().getActionBar().getThemedContext());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        // Set the blank SearchView to the menu
        MenuItem searchItem = menu.add(0, R.id.search, Menu.NONE, R.string.groceryoverview_menu_item_search);
        searchItem.setIcon(R.drawable.ic_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchItem.setActionView(searchView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshContent(mContext);
                return true;
            case android.R.id.home:
                // When home is pressed
                if (mDrawerLayout.isDrawerOpen(mDrawerList))
                    mDrawerLayout.closeDrawer(mDrawerList);
                else
                    mDrawerLayout.openDrawer(mDrawerList);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshContent(Context context) {
        Toast t = Toast.makeText(this, "Fetching new items...", Toast.LENGTH_SHORT);
        t.show();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean(SETTINGS_IS_REFRESHING, true);
        settingsEditor.commit();

        refreshItem = mMenu.findItem(R.id.refresh);
        RefreshAnimation.refreshIcon(mContext, true, refreshItem);

        GroceryRefreshTrigger.refreshAll(getApplicationContext());
    }

    private class RefreshStatusReceiver extends BroadcastReceiver {
        private RefreshStatusReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor settingsEditor = settings.edit();

            int resultCode = intent.getBundleExtra("bundle").getInt(NetworkHandler.CONNECTION_STATE);
            int requestType = intent.getBundleExtra("bundle").getInt(NetworkHandler.REQUEST_TYPE);
            boolean newData = intent.getBundleExtra("bundle").getBoolean(GroceryGCMBroadcastReceiver.SETTINGS_IS_NEW_DATA_AVA);

            Toast toast = null;

            if (resultCode == NetworkHandler.NO_CONNECTION) {
                RefreshAnimation.refreshIcon(context, false, refreshItem);
                toast = Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT);
                toast.show();

                settingsEditor.putBoolean(SETTINGS_IS_REFRESHING, false);
                settingsEditor.commit();

                return;
            }

            if (newData) {
                invalidateOptionsMenu();

                settingsEditor.putBoolean(SETTINGS_IS_REFRESHING, false);
                settingsEditor.commit();
            }

            if (requestType == NetworkHandler.GRO && resultCode == NetworkHandler.CONNECTION) {
                Log.i(GroceryApplication.TAG, "******** Refresh is done... ********");

                RefreshAnimation.refreshIcon(context, false, refreshItem);

                settingsEditor.putBoolean(GroceryGCMBroadcastReceiver.SETTINGS_IS_NEW_DATA_AVA, false);
                settingsEditor.putBoolean(SETTINGS_IS_REFRESHING, false);
                settingsEditor.putBoolean(SplashScreenActivity.SETTINGS_IS_DB_POPULATED, true);
                settingsEditor.commit();

                invalidateOptionsMenu();
                refreshItem = null;

                Crouton.cancelAllCroutons();

                toast = Toast.makeText(mContext, "Groceries Updated", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
