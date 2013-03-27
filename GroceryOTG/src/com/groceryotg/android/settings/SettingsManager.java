package com.groceryotg.android.settings;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;

public class SettingsManager {
	public static final String SETTINGS_PREVIOUS_NOTIFICATION = "previous_notification_set";
	
	public static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static boolean getNotificationsEnabled(Context context) {
		return getPrefs(context).getBoolean("notification_enabled", false);
	}
	
	public static int getNotificationFrequency(Context context) {
		return getPrefs(context).getInt("notification_freq", 3);
	}
	
	public static SparseBooleanArray getStoreFilter(Context context) {
		Set<String> strs = getPrefs(context).getStringSet("store_select", new HashSet<String>());
		SparseBooleanArray a = new SparseBooleanArray();
		for (String s : strs) {
			a.append(Integer.valueOf(s), true);
		}
		
		return a;
	}
	
	public static int setStoreFilter(Context context, SparseBooleanArray a) {
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
	}
}
