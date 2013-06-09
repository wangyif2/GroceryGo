package com.groceryotg.android.settings;

import com.groceryotg.android.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);
    }
}
