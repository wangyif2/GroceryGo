package com.groceryotg.android.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Date;

/**
 * User: robert
 * Date: 06/02/13
 */
public class NetworkHandler extends IntentService {
    public static final String REFRESH_CONTENT = "content";
    public static final int CAT = 10;
    public static final int GRO = 20;
    public static final int STO = 30;

    public static final int CONNECTION = 10;
    public static final int NO_CONNECTION = 11;

    JSONParser jsonParser = new JSONParser();

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        PendingIntent pendingIntent = (PendingIntent) extras.get("pendingIntent");
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

        try {
            pendingIntent.send(connectionState);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
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
                    contentValues.put(StoreTable.COLUMN_STORE_NAME, store.getStoreName());
                    contentValues.put(StoreTable.COLUMN_STORE_PARENT, store.getStoreParent());
                    contentValues.put(StoreTable.COLUMN_STORE_ADDR, store.getStoreAddress());

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
        String date = "?date=" + ServerURL.getDateFormat().format(new Date());
        String[] requestArgs = new String[]{date};
        String getGrocery = buildGroceryURL(requestArgs);

        //network request
        JSONArray groceryArray = jsonParser.getJSONFromUrl(getGrocery);
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        if (groceryArray != null) {
            addNewGroceries(groceryArray, contentValuesArrayList);
            removeExpiredGroceries(new Date());
        }
    }

    private void removeExpiredGroceries(Date date) {
        String selection = GroceryTable.COLUMN_GROCERY_EXPIRY + " < '" + date.getTime() + "'";
        getContentResolver().delete(GroceryotgProvider.CONTENT_URI_GRO, selection, null);
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
        // TODO: Accept a second argument, int categoryId, and append to querystring
        StringBuilder url = new StringBuilder();
        url.append(ServerURL.getGroceryBaseUrl());
        for (String arg : args)
            url.append(arg);
        return url.toString();
    }
}
