package com.groceryotg.android.settings;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.services.location.LocationServiceReceiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends SherlockPreferenceActivity {
	Activity mActivity;
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    	
    	configActionBar();
    	
    	this.mActivity = this;
    	
    	// Register preference actions
    	registerActions();
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
    
    @SuppressWarnings("deprecation")
	private void registerActions() {
    	PreferenceManager pm = this.getPreferenceManager();
    	
    	Preference notificationEnabledPreference = pm.findPreference("notification_enabled");
    	assert (notificationEnabledPreference != null);
    	
    	notificationEnabledPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				CheckBoxPreference cb = (CheckBoxPreference) preference;
				boolean isChecked = cb.isChecked();
				
				Intent intent = new Intent(mActivity, LocationServiceReceiver.class);
				
				if (isChecked) {
					intent.setAction(LocationServiceReceiver.LOCATION_SERVICE_RECEIVER_ENABLE);
					
				} else {
					intent.setAction(LocationServiceReceiver.LOCATION_SERVICE_RECEIVER_DISABLE);
				}
				
				mActivity.sendBroadcast(intent);
				
				return true;
			}
    	});
    }
}
