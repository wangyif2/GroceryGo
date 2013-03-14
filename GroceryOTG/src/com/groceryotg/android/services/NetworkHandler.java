package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.GroceryTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.database.objects.Category;
import com.groceryotg.android.database.objects.Grocery;
import com.groceryotg.android.database.objects.Store;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: robert
 * Date: 06/02/13
 */
public class NetworkHandler extends IntentService {
    public static final String REFRESH_COMPLETED_ACTION = "com.groceryotg.android.service.REFRESH_COMPLETE";

    public static final String CONNECTION_STATE = "connectionStatus";
    public static final int CONNECTION = 10;
    public static final int NO_CONNECTION = 11;

    public static final String REFRESH_CONTENT = "content";
    public static final int CAT = 10;
    public static final int GRO = 20;
    public static final int STO = 30;

    JSONParser jsonParser = new JSONParser();

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        int connectionState = NO_CONNECTION;

        if (ServerURL.checkNetworkStatus(this.getBaseContext()) && extras != null) {
            Integer requestType = (Integer) extras.get(REFRESH_CONTENT);
            switch (requestType) {
                case CAT:
                    refreshCategory();
                    break;
                case GRO:
                    refreshGrocery();
                    break;
                case STO:
                    refreshStore();
                    break;
                default:
                    Log.e("GroceryOTG", "unknown request received by NetworkHandler");
                    break;
            }
            connectionState = CONNECTION;
        } else if (!ServerURL.checkNetworkStatus(this.getBaseContext())) {
            connectionState = NO_CONNECTION;
        }

        Intent localIntent = new Intent(REFRESH_COMPLETED_ACTION).putExtra(CONNECTION_STATE,connectionState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void refreshCategory() {
        Gson gson = new Gson();
        JSONArray categoryArray = jsonParser.getJSONFromUrl(ServerURL.getCateoryUrl());
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        if (categoryArray != null) {
            try {
                for (int i = 0; i < categoryArray.length(); i++) {
                    Category category = gson.fromJson(categoryArray.getJSONObject(i).toString(), Category.class);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(CategoryTable.COLUMN_CATEGORY_ID, category.getCategoryId());
                    contentValues.put(CategoryTable.COLUMN_CATEGORY_NAME, category.getCategoryName());

                    contentValuesArrayList.add(contentValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ContentValues[] categories = new ContentValues[contentValuesArrayList.size()];
            contentValuesArrayList.toArray(categories);

            getContentResolver().bulkInsert(GroceryotgProvider.CONTENT_URI_CAT, categories);
        }
    }

    private void refreshStore() {
        Gson gson = new Gson();
        JSONArray storeArray = jsonParser.getJSONFromUrl(ServerURL.getStoreUrl());
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        if (storeArray != null) {
            try {
                for (int i = 0; i < storeArray.length(); i++) {
                    Store store = gson.fromJson(storeArray.getJSONObject(i).toString(), Store.class);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(StoreTable.COLUMN_STORE_ID, store.getStoreId());
                    contentValues.put(StoreTable.COLUMN_STORE_ADDR, store.getStoreAddress());
                    contentValues.put(StoreTable.COLUMN_STORE_LATITUDE, store.getStoreLatitude());
                    contentValues.put(StoreTable.COLUMN_STORE_LONGITUDE, store.getStoreLongitude());
                    contentValues.put(StoreTable.COLUMN_STORE_PARENT, store.getStoreParent().getStoreParentId());
                    contentValues.put(StoreTable.COLUMN_STORE_FLYER, store.getFlyer().getFlyerId());

                    contentValuesArrayList.add(contentValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ContentValues[] stores = new ContentValues[contentValuesArrayList.size()];
            contentValuesArrayList.toArray(stores);

            getContentResolver().bulkInsert(GroceryotgProvider.CONTENT_URI_STO, stores);
        }
    }

    private void refreshGrocery() {
        //build request url
        String date = ServerURL.getDateNowAsArg();
        String[] requestArgs = new String[]{date};
        String getGrocery = buildGroceryURL(requestArgs);

        //network request
        JSONArray groceryArray = jsonParser.getJSONFromUrl(getGrocery);
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        if (groceryArray != null) {
            addNewGroceries(groceryArray, contentValuesArrayList);
            removeExpiredGroceries();
        }
    }

    private void removeExpiredGroceries() {
//        Get today's date without time
        try {
            Date dateWithoutTime = ServerURL.getDateFormat().parse(ServerURL.getDateFormat().format(new Date()));
            String selection = GroceryTable.COLUMN_GROCERY_EXPIRY + " < '" + dateWithoutTime.getTime() + "'";
            getContentResolver().delete(GroceryotgProvider.CONTENT_URI_GRO, selection, null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void addNewGroceries(JSONArray groceryArray, ArrayList<ContentValues> contentValuesArrayList) {
        //TODO: hard coded date format!! not good...
        Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
        try {
            for (int i = 0; i < groceryArray.length(); i++) {
                Grocery grocery = gson.fromJson(groceryArray.getJSONObject(i).toString(), Grocery.class);

                ContentValues contentValues = new ContentValues();
                contentValues.put(GroceryTable.COLUMN_GROCERY_ID, grocery.getGroceryId());
                contentValues.put(GroceryTable.COLUMN_GROCERY_NAME, grocery.getRawString());
                contentValues.put(GroceryTable.COLUMN_GROCERY_PRICE, grocery.getTotalPrice());
                contentValues.put(GroceryTable.COLUMN_GROCERY_CATEGORY, grocery.getCategoryId());
                Log.i("GroceryOTG", String.valueOf(grocery.getEndDate().getTime()));
                contentValues.put(GroceryTable.COLUMN_GROCERY_EXPIRY, grocery.getEndDate().getTime());

                contentValuesArrayList.add(contentValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ContentValues[] groceries = new ContentValues[contentValuesArrayList.size()];
        contentValuesArrayList.toArray(groceries);

        getContentResolver().bulkInsert(GroceryotgProvider.CONTENT_URI_GRO, groceries);
    }

    private String buildGroceryURL(String[] args) {
        StringBuilder url = new StringBuilder();
        url.append(ServerURL.getGroceryBaseUrl());
        for (String arg : args)
            url.append(arg);
        return url.toString();
    }


}
