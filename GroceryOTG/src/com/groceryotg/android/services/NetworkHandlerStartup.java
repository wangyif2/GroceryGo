package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.groceryotg.android.SplashScreen;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.database.objects.Grocery;
import com.groceryotg.android.utils.GroceryOTGUtils;
import com.groceryotg.android.utils.JSONParser;

/**
 * User: robert
 * Date: 06/02/13
 */
public class NetworkHandlerStartup extends IntentService {
    public static final String REFRESH_COMPLETED_ACTION = "com.groceryotg.android.service.REFRESH_COMPLETE";

    public static final String CONNECTION_STATE = "connectionStatus";
    public static final int CONNECTION = 10;
    public static final int NO_CONNECTION = 11;

    public static final String REFRESH_CONTENT = "content";
    public static final int CAT = 10;
    public static final int GRO = 20;
    public static final int STO_PAR = 30;
    public static final int STO = 40;
    public static final int FLY = 50;

    public static final String REQUEST_TYPE = "refresh_type";

    JSONParser jsonParser = new JSONParser();

    public NetworkHandlerStartup() {
        super("NetworkHandlerStartup");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SQLiteDatabase db = GroceryotgProvider.database.getWritableDatabase();
        Bundle extras = intent.getExtras();
        Integer requestType = null;
        int connectionState = NO_CONNECTION;

        if (ServerURL.checkNetworkStatus(this.getBaseContext()) && extras != null) {
            requestType = (Integer) extras.get(REFRESH_CONTENT);
            switch (requestType) {
                case GRO:
                    refreshGrocery(db);
                    break;
                default:
                    Log.e("GroceryOTG", "unknown request received by NetworkHandler");
                    break;
            }
            connectionState = CONNECTION;
        } else if (!ServerURL.checkNetworkStatus(this.getBaseContext())) {
            connectionState = NO_CONNECTION;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(CONNECTION_STATE, connectionState);
        bundle.putInt(REQUEST_TYPE, requestType);
        Intent localIntent = new Intent(REFRESH_COMPLETED_ACTION).putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void refreshGrocery(SQLiteDatabase db) {
//        this is here for testing purposes
//        String date = "?date=2013-03-13";
        String date = ServerURL.getDateNowAsArg();

        Cursor c = GroceryOTGUtils.getCategories(this);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            for (int i = 0; i < c.getCount(); i++) {
                int previousIncrement = 0;
                int windowLength = c.getCount() / 50;
                if (windowLength == 0)
                    windowLength = 1;

                String categroyID = "categoryId=" + c.getString(c.getColumnIndex(CategoryTable.COLUMN_CATEGORY_ID));
                String[] requestArgs = new String[]{date, categroyID};
                String getGrocery = buildGroceryURL(requestArgs);

                JsonArray groceryArray = jsonParser.getJSONFromUrl(getGrocery);

                if (groceryArray != null) {
                    addNewGroceries(groceryArray, db);
                }

                if (++previousIncrement == windowLength) {
                    previousIncrement = 0;
                    SplashScreen.incrementProgressBar(1);
                }

            }

            c.moveToNext();
        }
    }

    private void addNewGroceries(JsonArray groceryArray, SQLiteDatabase db) {
        //TODO: hard coded date format!! not good...
        Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, GroceryTable.TABLE_GROCERY);
        Grocery grocery;
        try {
            Log.i("GroceryOTG", "Number of grocery available: " + groceryArray.size());

            int grocery_id = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_ID);
            int grocery_name = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME);
            int grocery_price = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE);
            int grocery_category = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_CATEGORY);
            int grocery_expiry = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_EXPIRY);
            int grocery_flyer = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_FLYER);

            for (int i = 0; i < 5; i++) {
                grocery = gson.fromJson(groceryArray.get(i), Grocery.class);

                ih.prepareForInsert();
                ih.bind(grocery_id, grocery.getGroceryId());
                ih.bind(grocery_name, grocery.getRawString());
                if (grocery.getTotalPrice() != null)
                    ih.bind(grocery_price, grocery.getTotalPrice());
                if (grocery.getCategoryId() != null)
                    ih.bind(grocery_category, grocery.getCategoryId());
                if (grocery.getEndDate() != null)
                    ih.bind(grocery_expiry, grocery.getEndDate().getTime());
                ih.bind(grocery_flyer, grocery.getFlyer().getFlyerId());
                ih.execute();


            }
        } finally {
            ih.close();
        }
    }

    private String buildGroceryURL(String[] args) {
        StringBuilder url = new StringBuilder();
        url.append(ServerURL.getGroceryBaseUrl());
        for (String arg : args)
            url.append(arg).append("&");
        return url.toString();
    }


}
