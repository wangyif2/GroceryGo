package ca.grocerygo.android;

import java.util.ArrayList;
import java.util.Map;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ListView;
import ca.grocerygo.android.fragment.GroceryListFragment;
import ca.grocerygo.android.utils.GroceryGoUtils;
import ca.grocerygo.android.utils.GroceryStoreDistanceMap;
import ca.grocerygo.android.utils.ParcelableStateVar;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class GroceryPagerFragmentActivity extends SherlockFragmentActivity {
	public static String EXTRA_LAUNCH_PAGE = "extra_launch_page";
	public static final String STATE_GLOBALVARS = "state_globalvars";
	
	private Context mContext;
	
	public static SparseArray<String> categories;
	public static SparseArray<String> storeNames;

	public static ViewPager mPager;
	private Menu mMenu;
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	GroceryAdapter mAdapter;

	private final int OFFPAGE_LIMIT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("GGDebug", "onCreate GroceryPagerFragmentActivity");
		
		// Check whether we're recreating a previously destroyed instance
		if (savedInstanceState != null) {
			// Restore values of members from saved state
			ParcelableStateVar stateFlyerStoreMap = savedInstanceState.getParcelable(STATE_GLOBALVARS);
			stateFlyerStoreMap.applyParcelableStateVar();
			Log.d("GGDebug", "restoring savedInstanceState GroceryPagerFragmentActivity");
			
			// Workaround: do a fresh reload of activity
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
			startActivity(i);
			Log.d("GGDebug", "restarting activity");
			return;
		}
		
		setContentView(R.layout.grocery_pager_activity);
		this.mContext = this;

		categories = GroceryGoUtils.getCategorySets(this);
		storeNames = GroceryGoUtils.getStoreParentNameSets(this);

		GroceryGoUtils.NavigationDrawerBundle drawerBundle = GroceryGoUtils.configNavigationDrawer(this, false, R.string.title_grocery_pager);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();

		configViewPager();
		
		handleIntent(getIntent());
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		
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
		} else if (extras != null) {
			int position = extras.getInt(GroceryPagerFragmentActivity.EXTRA_LAUNCH_PAGE);
			mPager.setCurrentItem(position);
		}
	}

	@Override
	protected void onStart() {
		Log.d("GGDebug", "onStart GroceryPagerFragmentActivity");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		Log.d("GGDebug", "onResume GroceryPagerFragmentActivity");
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d("GGDebug", "onPause GroceryPagerFragmentActivity");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		Log.d("GGDebug", "onStop GroceryPagerFragmentActivity");
		super.onStop();
	}
	
	@Override
	protected void onRestart() {
		Log.d("GGDebug", "onRestart GroceryPagerFragmentActivity");
		super.onRestart();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save extra variables pertaining to flyers and stores
		SparseArray<Float> storeDistanceMap = GroceryStoreDistanceMap.getmStoreDistanceMap();
		Map<String, Integer> mapIconMap = GroceryStoreDistanceMap.getmMapIconMap();
		Map<String, Integer> storeParentIconMap = GroceryStoreDistanceMap.getmStoreParentIconMap();
		SparseArray<ArrayList<Integer>> flyerStoreMap = GroceryStoreDistanceMap.getmFlyerStoreMap();
		SparseArray<ArrayList<Integer>> storeParentStoreMap = GroceryStoreDistanceMap.getmStoreParentStoreMap();
		
		ParcelableStateVar stateGlobalVars = new ParcelableStateVar(storeDistanceMap, mapIconMap, storeParentIconMap, flyerStoreMap, storeParentStoreMap);
		savedInstanceState.putParcelable(STATE_GLOBALVARS, stateGlobalVars);
		Log.d("GGDebug", "onSaveInstanceState GroceryPagerFragmentActivity - saving the flyer to store mapping");
		
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		Log.d("GGDebug", "onDestroy GroceryPagerFragmentActivity");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.grocery_pager_activity_menu, menu);

		this.mMenu = menu;
		
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true);
		
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.mDrawerLayout != null && this.mDrawerList != null) {
			for (int i = 0; i < menu.size(); i++) {
				MenuItem item = menu.getItem(i);
				item.setVisible(!mDrawerLayout.isDrawerOpen(mDrawerList));
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mDrawerLayout.isDrawerOpen(mDrawerList))
					mDrawerLayout.closeDrawer(mDrawerList);
				else {
					// Specify the parent activity
					Intent parentActivityIntent = new Intent(this, CategoryTopFragmentActivity.class);
					parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
												Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(parentActivityIntent);
					this.finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void configViewPager() {
		mPager = (ViewPager) findViewById(R.id.pager);

		mAdapter = new GroceryAdapter(getSupportFragmentManager());
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(OFFPAGE_LIMIT);
	}
	

	public class GroceryAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
		private SparseArray<GroceryListFragment> mPageReferenceMap;
		
		int mCurrentPage = 0;

		public GroceryAdapter(FragmentManager fm) {
			super(fm);
			mPageReferenceMap = new SparseArray<GroceryListFragment>();
			mPager.setOnPageChangeListener(this);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			// The hashmap is offset by one
			return categories.valueAt(position);
		}

		@Override
		public GroceryListFragment getItem(int position) {
			GroceryListFragment myFragment;
			if (mPageReferenceMap.get(position) == null) {
				myFragment = GroceryListFragment.newInstance(categories.keyAt(position));
				mPageReferenceMap.put(position, myFragment);
				return myFragment;
			} else
				return mPageReferenceMap.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			mPageReferenceMap.remove(position);
		}

		@Override
		public int getCount() {
			return categories.size();
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
		@Override
		public void onPageScrolled(int i, float v, int i2) {
		}

		@Override
		public void onPageSelected(int position) {
			mCurrentPage = position;
		}
	}
}
