package com.groceryotg.android.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ServerURL {
	private static final String baseUrl = "http://groceryotg.elasticbeanstalk.com/";
	private static final String cateoryUrl = baseUrl + "/GetGeneralInfo";
	private static final String groceryBaseUrl = baseUrl + "/UpdateGroceryInfo";
	private static final String storeUrl = baseUrl + "/GetStoreInfo";
	private static final String storeParentUrl = baseUrl + "/GetStoreParentInfo";
	private static final String flyerUrl = baseUrl + "/GetFlyerInfo";

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DecimalFormat getDecimalFormat = new DecimalFormat("0.00");

	private static String lastRefreshed = null;

	public static DecimalFormat getGetDecimalFormat() {
		return getDecimalFormat;
	}

	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	public static String getCateoryUrl() {
		return cateoryUrl;
	}

	public static String getGroceryBaseUrl() {
		return groceryBaseUrl;
	}

	public static String getStoreUrl() {
		return storeUrl;
	}

	public static String getStoreParentUrl() {
		return storeParentUrl;
	}

	public static String getFlyerUrl() {
		return flyerUrl;
	}

	public static boolean checkNetworkStatus(Context context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	public static String getLastRefreshed() {
		return lastRefreshed;
	}

	public static void setLastRefreshed(String lastRefreshed) {
		ServerURL.lastRefreshed = lastRefreshed;
	}

	public static String getDateNowAsArg() {
		String date = getDateFormat().format(new Date());
		setLastRefreshed(date);
		return "?date=" + date;
	}
}
