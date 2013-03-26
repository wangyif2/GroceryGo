package com.groceryotg.android.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.groceryotg.android.R;

/**
 * User: robert
 * Date: 22/03/13
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
