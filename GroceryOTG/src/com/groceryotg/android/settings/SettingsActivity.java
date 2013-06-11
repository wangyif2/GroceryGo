package com.groceryotg.android.settings;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends SherlockPreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    	
    	configActionBar();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.
            	
            	// Specify the parent activity
            	Intent parentActivityIntent = new Intent(this, GroceryFragmentActivity.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	this.finish();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void configActionBar() {
    	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
