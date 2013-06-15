package com.groceryotg.android;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.fragment.GlobalSearchFragment;
import com.groceryotg.android.utils.GroceryOTGUtils;

public class GlobalSearchFragmentActivity extends SherlockFragmentActivity {
	public static final String GLOBAL_SEARCH = "global_search";
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_activity);
        
        GroceryOTGUtils.NavigationDrawerBundle drawerBundle = GroceryOTGUtils.configNavigationDrawer(this, false, R.string.title_search);
        this.mDrawerLayout = drawerBundle.getDrawerLayout();
        this.mDrawerList = drawerBundle.getDrawerList();
        
        // Does the actual search
        handleSearch(getIntent());
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
	    
		return true;
	}
    
	public void handleSearch(Intent intent) {
		String query = "";
		setIntent(intent);
		
		if (intent.getExtras().containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH)) {
			// Update the query - this is used by the loader when fetching results from database
			query = intent.getStringExtra(SearchManager.QUERY).trim();
        }
		
		GlobalSearchFragment frag = (GlobalSearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_activity_content_fragment);
        frag.setQuery(query);
	}
}
