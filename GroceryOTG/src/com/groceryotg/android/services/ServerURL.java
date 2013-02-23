package com.groceryotg.android.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: robert
 * Date: 23/02/13
 */
public class ServerURL {
    private static final String cateoryUrl = "http://groceryotg.elasticbeanstalk.com/GetGeneralInfo";
    private static final String groceryBaseUrl = "http://groceryotg.elasticbeanstalk.com/UpdateGroceryInfo";
    private static final String storeUrl = "http://groceryotg.elasticbeanstalk.com/GetStoreInfo";

    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public static DateFormat getFormat() {
        return format;
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
}
