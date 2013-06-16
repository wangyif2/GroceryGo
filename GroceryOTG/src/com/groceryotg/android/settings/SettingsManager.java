package com.groceryotg.android.settings;

import com.groceryotg.android.utils.MultiSelectListPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;

public class SettingsManager {
	public static final String SETTINGS_PREVIOUS_NOTIFICATION = "previous_notification_set";
	public static final String SETTINGS_NAVIGATION_DRAWER_SEEN = "navigation_drawer_seen";
	public static final String SETTINGS_CHANGELOG_SEEN = "changelog_seen";
	
	public static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean getNotificationsEnabled(Context context) {
		return getPrefs(context).getBoolean("notification_enabled", true);
	}
	
	public static int getNotificationFrequency(Context context) {
		return getPrefs(context).getInt("notification_freq", 3);
	}
	
	public static boolean getNavigationDrawerSeen(Context context) {
		return getPrefs(context).getBoolean(SettingsManager.SETTINGS_NAVIGATION_DRAWER_SEEN, false);
	}
	
	public static void setNavigationDrawerSeen(Context context, boolean state) {
		Editor editor = getPrefs(context).edit();
		editor.putBoolean(SettingsManager.SETTINGS_NAVIGATION_DRAWER_SEEN, state);
		editor.commit();
	}
	
	public static boolean getChangelogSeen(Context context) {
		return getPrefs(context).getBoolean(SettingsManager.SETTINGS_CHANGELOG_SEEN, false);
	}
	
	public static void setChangelogSeen(Context context, boolean state) {
		Editor editor = getPrefs(context).edit();
		editor.putBoolean(SettingsManager.SETTINGS_CHANGELOG_SEEN, state);
		editor.commit();
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
}
