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
    private static final String cateoryUrl = "http://groceryotg.elasticbeanstalk.com/GetGeneralInfo";
    private static final String groceryBaseUrl = "http://groceryotg.elasticbeanstalk.com/UpdateGroceryInfo";
    private static final String storeUrl = "http://groceryotg.elasticbeanstalk.com/GetStoreInfo";

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

    public static boolean checkNetworkStatus(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getLastRefreshed() {
        return lastRefreshed;
    }

    public static void setLastRefreshed(String lastRefreshed) {
        ServerURL.lastRefreshed = lastRefreshed;
    }

    public static String getDateNowAsArg(){
        String date = getDateFormat().format(new Date());
        setLastRefreshed(date);
        return "?date=" + date;
    }
}
