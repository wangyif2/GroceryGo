package ca.grocerygo.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import ca.grocerygo.android.CategoryTopFragmentActivity;
import ca.grocerygo.android.R;
import ca.grocerygo.android.services.location.LocationServiceReceiver;
import ca.grocerygo.android.utils.GroceryOTGUtils;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {
	Activity mActivity;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		this.mActivity = this;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		// Register preference actions
		registerActions();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent parentActivityIntent = new Intent(this, CategoryTopFragmentActivity.class);
				parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
											Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(parentActivityIntent);
				this.finish();
				
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressWarnings("deprecation")
	private void registerActions() {
		PreferenceManager pm = this.getPreferenceManager();
		
		Preference notificationEnabledPreference = pm.findPreference(SettingsManager.SETTINGS_NOTIFICATION_ENABLED);
		assert (notificationEnabledPreference != null);
		
		notificationEnabledPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				CheckBoxPreference cb = (CheckBoxPreference) preference;
				boolean isChecked = cb.isChecked();
				
				Intent intent = new Intent(mActivity, LocationServiceReceiver.class);
				
				if (isChecked) {
					SettingsManager.setNotificationsEnabled(mActivity, true);
					intent.setAction(LocationServiceReceiver.LOCATION_SERVICE_RECEIVER_ENABLE);
					
				} else {
					SettingsManager.setNotificationsEnabled(mActivity, false);
					intent.setAction(LocationServiceReceiver.LOCATION_SERVICE_RECEIVER_DISABLE);
				}
				
				mActivity.sendBroadcast(intent);
				
				return true;
			}
		});
		
		Preference storeFilterPreference = pm.findPreference(SettingsManager.SETTINGS_STORE_FILTER);
		storeFilterPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				GroceryOTGUtils.restartGroceryLoaders(mActivity);
				return true;
			}
		});
		
		Preference storeLocationPreference = pm.findPreference(SettingsManager.SETTINGS_STORE_LOCATION);
		storeLocationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				GroceryOTGUtils.restartGroceryLoaders(mActivity);
				return true;
			}
		});
	}
}
