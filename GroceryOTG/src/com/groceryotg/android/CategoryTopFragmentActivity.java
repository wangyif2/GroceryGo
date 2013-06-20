package com.groceryotg.android;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import com.groceryotg.android.services.NetworkHandler;
import com.groceryotg.android.utils.ChangeLogDialog;
import com.groceryotg.android.utils.GroceryOTGUtils;
import com.groceryotg.android.utils.RefreshAnimation;

public class CategoryTopFragmentActivity extends SherlockFragmentActivity {
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

		GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, true, R.string.title_main);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
		this.mDrawerToggle = drawerBundle.getDrawerToggle();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
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
			GroceryOTGUtils.copyIntentData(intent, globalSearchIntent);
			globalSearchIntent.putExtra(GlobalSearchFragmentActivity.GLOBAL_SEARCH, true);
			startActivity(globalSearchIntent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mRefreshStatusReceiver = new RefreshStatusReceiver();
		IntentFilter mStatusIntentFilter = new IntentFilter(NetworkHandler.REFRESH_COMPLETED_ACTION);
		mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		LocalBroadcastManager.getInstance(this).registerReceiver(mRefreshStatusReceiver, mStatusIntentFilter);
		
		// show a changelog dialog if it's new
		ChangeLogDialog cd = new ChangeLogDialog(this);
		cd.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRefreshStatusReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.category_activity_menu, menu);
		mMenu = menu;

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
				refreshCategories();
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

	private void refreshCategories() {
		Toast t = Toast.makeText(this, "Fetching new items...", Toast.LENGTH_LONG);
		t.show();

		Intent intent = new Intent(mContext, NetworkHandler.class);
		refreshItem = mMenu.findItem(R.id.refresh);
		RefreshAnimation.refreshIcon(mContext, true, refreshItem);
		intent.putExtra(NetworkHandler.REFRESH_CONTENT, NetworkHandler.CAT);
		startService(intent);
	}

	private class RefreshStatusReceiver extends BroadcastReceiver {
		private RefreshStatusReceiver() {

		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int resultCode = intent.getBundleExtra("bundle").getInt(NetworkHandler.CONNECTION_STATE);
			int requestType = intent.getBundleExtra("bundle").getInt(NetworkHandler.REQUEST_TYPE);

			Toast toast = null;
			if (requestType == NetworkHandler.CAT) {
				RefreshAnimation.refreshIcon(context, false, refreshItem);
			}
			if (resultCode == NetworkHandler.CONNECTION) {
				toast = Toast.makeText(mContext, "Groceries Updated", Toast.LENGTH_LONG);
			} else if (resultCode == NetworkHandler.NO_CONNECTION) {
				RefreshAnimation.refreshIcon(context, false, refreshItem);
				toast = Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_LONG);
			}
			assert toast != null;
			toast.show();
		}
	}
}
