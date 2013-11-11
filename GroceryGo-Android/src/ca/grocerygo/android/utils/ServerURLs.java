package ca.grocerygo.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServerURLs {
    public static final String GOOGLE_APP_ENGINE = "http://groceryotg-test.appspot.com/";

    //TODO: publish another env at grocerygo
	private static final String AMAZON_BEANSTALK = "http://grocerygo.elasticbeanstalk.com/";
	private static final String cateoryUrl = AMAZON_BEANSTALK + "/GetGeneralInfo";
	private static final String groceryBaseUrl = AMAZON_BEANSTALK + "/UpdateGroceryInfo";
	private static final String storeUrl = AMAZON_BEANSTALK + "/GetStoreInfo";
	private static final String storeParentUrl = AMAZON_BEANSTALK + "/GetStoreParentInfo";
	private static final String flyerUrl = AMAZON_BEANSTALK + "/GetFlyerInfo";

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
	private static final DecimalFormat getDecimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.CANADA));

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
		ServerURLs.lastRefreshed = lastRefreshed;
	}

	public static String getDateNowAsArg() {
		String date = getDateFormat().format(new Date());
		setLastRefreshed(date);
		return "?date=" + date;
	}
}
