package com.grocerygo.android.settings;

import com.grocerygo.android.R;
import com.grocerygo.android.utils.MultiSelectListPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;

public class SettingsManager {
	public static final String SETTINGS_PREVIOUS_NOTIFICATION = "previous_notification_set";
	public static final String SETTINGS_NAVIGATION_DRAWER_SEEN = "navigation_drawer_seen";
	public static final String SETTINGS_CHANGELOG_SEEN_VERSION = "changelog_seen_version";
	public static final String SETTINGS_NOTIFICATION_ENABLED = "notification_enabled";
	public static final String SETTINGS_NOTIFICATION_FREQUENCY = "notification_freq";
	public static final String SETTINGS_STORE_FILTER = "store_select";
	public static final String SETTINGS_STORE_LOCATION = "store_location";
	
	public static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean getNotificationsEnabled(Context context) {
		return getPrefs(context).getBoolean(SETTINGS_NOTIFICATION_ENABLED, true);
	}
	
	public static void setNotificationsEnabled(Context context, boolean state) {
		Editor editor = getPrefs(context).edit();
		editor.putBoolean(SETTINGS_NOTIFICATION_ENABLED, state);
		editor.commit();
	}
	
	public static int getNotificationFrequency(Context context) {
		String f = getPrefs(context).getString(SETTINGS_NOTIFICATION_FREQUENCY, null);
		String[] freqArray = context.getResources().getStringArray(R.array.notification_freq_value);
		
		int period = 60*60*1000;
		if (f.equals(freqArray[0])) {
			// 30 minutes
			period = 1*60*1000;
		} else if (f.equals(freqArray[1])) {
			// 1 hour
			period = 60*60*1000;
		} else if (f.equals(freqArray[2])) {
			// 3 hours
			period = 3*60*60*1000;
		}
		return period;
	}
	
	public static boolean getNavigationDrawerSeen(Context context) {
		return getPrefs(context).getBoolean(SettingsManager.SETTINGS_NAVIGATION_DRAWER_SEEN, false);
	}
	
	public static void setNavigationDrawerSeen(Context context, boolean state) {
		Editor editor = getPrefs(context).edit();
		editor.putBoolean(SettingsManager.SETTINGS_NAVIGATION_DRAWER_SEEN, state);
		editor.commit();
	}
	
	public static int getChangelogSeen(Context context) {
		return getPrefs(context).getInt(SettingsManager.SETTINGS_CHANGELOG_SEEN_VERSION, 0);
	}
	
	public static void setChangelogSeen(Context context, int version) {
		Editor editor = getPrefs(context).edit();
		editor.putInt(SettingsManager.SETTINGS_CHANGELOG_SEEN_VERSION, version);
		editor.commit();
	}
	
	public static SparseBooleanArray getStoreFilter(Context context) {
		String strRaw = getPrefs(context).getString(SETTINGS_STORE_FILTER, "");
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
	
	public static int getStoreLocationFilter(Context context) {
		String defaultValue = context.getResources().getString(R.string.setting_storelocation_default);
		String prefValue = getPrefs(context).getString(SETTINGS_STORE_LOCATION, defaultValue);
		return Integer.valueOf(prefValue);
	}
}
