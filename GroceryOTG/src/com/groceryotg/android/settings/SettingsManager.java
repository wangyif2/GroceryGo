package com.groceryotg.android.settings;

import com.groceryotg.android.utils.MultiSelectListPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;

public class SettingsManager {
	public static final String SETTINGS_PREVIOUS_NOTIFICATION = "previous_notification_set";
	
	public static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean getNotificationsEnabled(Context context) {
		return getPrefs(context).getBoolean("notification_enabled", true);
	}
	
	public static int getNotificationFrequency(Context context) {
		return getPrefs(context).getInt("notification_freq", 3);
	}
	
	public static SparseBooleanArray getStoreFilter(Context context) {
		String strRaw = getPrefs(context).getString("store_select", "");
		String[] strs = MultiSelectListPreference.fromPersistedPreferenceValue(strRaw);
		SparseBooleanArray a = new SparseBooleanArray();
		
		if (strRaw == "") {
			return a;
		}
		
		for (String s : strs) {
			a.append(Integer.valueOf(s), true);
		}
		
		return a;
	}
	
	/*public static int setStoreFilter(Context context, SparseBooleanArray a) {
		SharedPreferences prefs = getPrefs(context);
		Editor editor = prefs.edit();
		
		Set<String> strs = new HashSet<String>();
		for (int i = 0; i < a.size(); i++) {
			if (a.valueAt(i)) {
				strs.add(Integer.toString(a.keyAt(i)));
			}
		}
		
		editor.putStringSet("store_select", strs);
		editor.commit();
		
		return 0;
	}*/
}
