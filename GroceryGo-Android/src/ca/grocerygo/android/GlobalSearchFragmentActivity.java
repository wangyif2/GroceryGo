package ca.grocerygo.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import ca.grocerygo.android.fragment.GlobalSearchFragment;
import ca.grocerygo.android.utils.GroceryGoUtils;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class GlobalSearchFragmentActivity extends SherlockFragmentActivity {
	public static final String GLOBAL_SEARCH = "global_search";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	
	private String mQuery = "";
	
	private SearchView mSearchView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.search_activity);
		
		GroceryGoUtils.NavigationDrawerBundle drawerBundle = GroceryGoUtils.configNavigationDrawer(this, false, R.string.title_search);
		this.mDrawerLayout = drawerBundle.getDrawerLayout();
		this.mDrawerList = drawerBundle.getDrawerList();
		
		// Does the actual search
		handleIntent(getIntent());
	}
	
	@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        refreshQuery();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (mDrawerLayout.isDrawerOpen(mDrawerList))
					mDrawerLayout.closeDrawer(mDrawerList);
				else {
					Intent parentActivityIntent = new Intent(this, CategoryTopFragmentActivity.class);
					parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
												Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(parentActivityIntent);
					finish();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.search_activity_menu, menu);
		
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setIconifiedByDefault(false);
		mSearchView.clearFocus();
		
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (mQuery.equals(query))
					return true;
				
				// Only refresh if the query has changed
				mQuery = query;
				refreshQuery();
				return false;
			}
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		
		refreshQuery();
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

	private void refreshQuery() {
		if (mSearchView != null) {
			mSearchView.setQuery(mQuery, false);
			mSearchView.clearFocus();
			GlobalSearchFragment frag = (GlobalSearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_activity_content_fragment);
			frag.setQuery(mQuery);
		}
	}
	
	public void handleIntent(Intent intent) {
		setIntent(intent);
		
		if (intent.getExtras().containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH) || Intent.ACTION_SEARCH.equals(intent.getAction()) ) {
			// Update the query - this is used by the loader when fetching results from database
			mQuery = intent.getStringExtra(SearchManager.QUERY).trim();
		}
	}
}
