package com.groceryotg.android;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.groceryotg.android.fragment.GlobalSearchFragment;
import com.groceryotg.android.utils.GroceryOTGUtils;

public class GlobalSearchFragmentActivity extends SherlockFragmentActivity {
	public static final String GLOBAL_SEARCH = "global_search";
	
	private String mQuery = "";
	private SearchView mSearchView;
	private GlobalSearchFragment mFrag;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search_activity);
        
        configActionBar();
        
        mFrag = (GlobalSearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);

        // Get the initial intent and pass it to the fragment as an argument
        Intent intent = getIntent();
        setIntent(intent);
        handleIntent(intent);
        mFrag.refreshQuery(mQuery);
    }
	
	@Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        mFrag.refreshQuery(mQuery);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.
            	
            	// Specify the parent activity
            	Intent parentActivityIntent = new Intent(this, GroceryPagerFragmentActivity.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	finish();
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
	    mSearchView.setIconifiedByDefault(true);
	    //mSearchView.setOnQueryTextListener(this);
	    
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
                mQuery = "";
                mFrag.refreshQuery(mQuery);
                return true;
            }
        });
	    
	    // When we have received a search intent from another activity (i.e. not by 
        // capturing user input in this activity), we need to programmatically expand 
        // the search menu item as if the user clicked the magnifying glass, set
        // the searchView text to the received query, and call clearFocus to collapse
        // the keyboard.
	    if (!mQuery.isEmpty()) {
	    	menu.findItem(R.id.search).expandActionView();
	    	mSearchView.setQuery(mQuery, false);
	    	mSearchView.clearFocus();
	    }
		return true;
	}
    
	public void handleIntent(Intent intent) {
		if (intent.getExtras().containsKey(GlobalSearchFragmentActivity.GLOBAL_SEARCH) || Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
			// Update the query - this is used by the loader when fetching results from database
			mQuery = intent.getStringExtra(SearchManager.QUERY).trim();
			
			// When we receive an intent from within this activity (i.e. after it
			// has been created), the searchView already exists. In this case,
			// update the searchView text to display the new query, and clear focus
			// in order to collapse the keyboard.
			if (mSearchView != null) {
				mSearchView.setQuery(mQuery, false);
				mSearchView.clearFocus();
			}
        }
	}
	
    private void configActionBar() {
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
    	return super.onKeyDown(keycode, e);
    }
}
