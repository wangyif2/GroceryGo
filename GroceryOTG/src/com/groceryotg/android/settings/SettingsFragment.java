package com.groceryotg.android.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.groceryotg.android.R;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: robert
 * Date: 22/03/13
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final MultiSelectListPreference storeListPref = (MultiSelectListPreference) findPreference("store_select");

        setStoreListPrefData(storeListPref);

        storeListPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                setStoreListPrefData(storeListPref);
                return false;
            }
        });
    }

    private void setStoreListPrefData(MultiSelectListPreference storeListPref) {
        List<String> storeNames = new ArrayList<String>();
        List<String> storeIds = new ArrayList<String>();

        Cursor c = GroceryOTGUtils.getStoreParentNamesCursor(getActivity());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            storeNames.add(c.getString(c.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME)));
            storeIds.add(c.getString(c.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID)));
            c.moveToNext();
        }

        final String[] entries = new String[storeNames.size()];
        storeNames.toArray(entries);

        final String[] entryValues = new String[storeIds.size()];
        storeIds.toArray(entryValues);

        storeListPref.setTitle(getResources().getString(R.string.setting_store_title));
        storeListPref.setSummary(getResources().getString(R.string.setting_store_summary));
        storeListPref.setDialogTitle(getResources().getString(R.string.setting_store_dialog_title));
        storeListPref.setEntries(entries);
        storeListPref.setEntryValues(entryValues);
        storeListPref.setDefaultValue("1");
    }

}
