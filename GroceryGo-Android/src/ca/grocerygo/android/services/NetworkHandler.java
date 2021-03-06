package ca.grocerygo.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ca.grocerygo.android.GroceryApplication;
import ca.grocerygo.android.SplashScreenActivity;
import ca.grocerygo.android.database.*;
import ca.grocerygo.android.database.contentprovider.GroceryotgProvider;
import ca.grocerygo.android.database.objects.*;
import ca.grocerygo.android.utils.GroceryGoUtils;
import ca.grocerygo.android.utils.JSONParser;
import ca.grocerygo.android.utils.ServerURLs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class NetworkHandler extends IntentService {
    public static final String REFRESH_COMPLETED_ACTION = "com.grocerygo.android.service.REFRESH_COMPLETE";

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

    private static boolean stopped = false;

    JSONParser jsonParser = new JSONParser();

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SQLiteDatabase db = GroceryotgProvider.database.getWritableDatabase();
        Bundle extras = intent.getExtras();
        Integer requestType = null;
        int connectionState = NO_CONNECTION;

        Bundle bundle = new Bundle();

        if (ServerURLs.checkNetworkStatus(this.getBaseContext()) && extras != null) {
            requestType = (Integer) extras.get(REFRESH_CONTENT);
            switch (requestType) {
                case CAT:
                    refreshCategory(db);
                    break;
                case GRO:
                    refreshGrocery(db);
                    break;
                case STO_PAR:
                    refreshStoreParent(db);
                    break;
                case STO:
                    refreshStore(db);
                    break;
                case FLY:
                    refreshFlyer(db);
                    break;
                default:
                    Log.e("GroceryOTG", "unknown request received by NetworkHandler");
                    break;
            }
            connectionState = CONNECTION;
            bundle.putInt(REQUEST_TYPE, requestType);
        } else if (!ServerURLs.checkNetworkStatus(this.getBaseContext())) {
            connectionState = NO_CONNECTION;
            bundle.putInt(REQUEST_TYPE, 0);
        }

        bundle.putInt(CONNECTION_STATE, connectionState);
        Intent localIntent = new Intent(REFRESH_COMPLETED_ACTION).putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onDestroy() {
        stopped = true;
        super.onDestroy();
    }

    private void refreshCategory(SQLiteDatabase db) {
        Log.i(GroceryApplication.TAG, "refreshing category...");

        Gson gson = new Gson();
        JsonArray categoryArray = jsonParser.getJSONFromUrl(ServerURLs.getCateoryUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, CategoryTable.TABLE_CATEGORY);
        Category category;

        if (categoryArray != null) {
            try {
                db.beginTransaction();

                int previousIncrement = 0;
                int windowLength = categoryArray.size() / 10;
                if (windowLength == 0)
                    windowLength = 1;

                int category_id = ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_ID);
                int category_name = ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_NAME);

                for (JsonElement jsonElement : categoryArray) {
                    category = gson.fromJson(jsonElement, Category.class);

                    ih.prepareForInsert();
                    ih.bind(category_id, category.getCategoryId());
                    ih.bind(category_name, category.getCategoryName());

                    ih.execute();

                    if (++previousIncrement == windowLength) {
                        previousIncrement = 0;
                        updateProgressBar(1);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                ih.close();
            }
        }

        Log.i(GroceryApplication.TAG, "refreshing category... DONE");
    }

    private void refreshGrocery(SQLiteDatabase db) {
        Log.i(GroceryApplication.TAG, "refreshing grocery...");

//		this is here for testing purposes
		String date = "?date=2013-03-13";
//        String date = ServerURLs.getDateNowAsArg();
        String[] requestArgs = new String[]{date};
        String getGrocery = buildGroceryURL(requestArgs);

        JsonReader groceryReader = jsonParser.getReaderFromUrl(getGrocery);

        if (groceryReader != null) {
            int maxGroceryIdBefore = addNewGroceries(groceryReader, db);
            int maxGroceryIdAfter = GroceryGoUtils.getMaxGroceryId(this);

            if (maxGroceryIdAfter > maxGroceryIdBefore)
                removeExpiredGroceries(maxGroceryIdBefore);
        }

        Log.i(GroceryApplication.TAG, "refreshing grocery...DONE");
    }

    private void refreshStoreParent(SQLiteDatabase db) {
        Log.i(GroceryApplication.TAG, "refreshing store parent...");

        Gson gson = new Gson();
        JsonArray storeParentArray = jsonParser.getJSONFromUrl(ServerURLs.getStoreParentUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreParentTable.TABLE_STORE_PARENT);
        StoreParent storeParent;

        if (storeParentArray != null) {
            try {
                db.beginTransaction();

                int previousIncrement = 0;
                int windowLength = storeParentArray.size() / 10;
                if (windowLength == 0)
                    windowLength = 1;

                int store_parent_id = ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID);
                int store_parent_name = ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME);

                for (JsonElement jsonElement : storeParentArray) {
                    storeParent = gson.fromJson(jsonElement, StoreParent.class);

                    ih.prepareForInsert();
                    ih.bind(store_parent_id, storeParent.getStoreParentId());
                    ih.bind(store_parent_name, storeParent.getName());
                    ih.execute();

                    if (++previousIncrement == windowLength) {
                        previousIncrement = 0;
                        updateProgressBar(1);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                ih.close();
            }
        }

        Log.i(GroceryApplication.TAG, "refreshing store parent...DONE");
    }

    private void refreshStore(SQLiteDatabase db) {
        Log.i(GroceryApplication.TAG, "refreshing store...");

        Gson gson = new Gson();
        JsonArray storeArray = jsonParser.getJSONFromUrl(ServerURLs.getStoreUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreTable.TABLE_STORE);
        Store store;

        if (storeArray != null) {
            try {
                db.beginTransaction();

                int previousIncrement = 0;
                int windowLength = storeArray.size() / 10;
                if (windowLength == 0)
                    windowLength = 1;

                int store_id = ih.getColumnIndex(StoreTable.COLUMN_STORE_ID);
                int store_parent = ih.getColumnIndex(StoreTable.COLUMN_STORE_PARENT);
                int store_flyer = ih.getColumnIndex(StoreTable.COLUMN_STORE_FLYER);
                int store_addr = ih.getColumnIndex(StoreTable.COLUMN_STORE_ADDR);
                int store_lat = ih.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE);
                int store_lng = ih.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE);

                for (JsonElement jsonElement : storeArray) {
                    store = gson.fromJson(jsonElement, Store.class);

                    ih.prepareForInsert();
                    ih.bind(store_id, store.getStoreId());
                    ih.bind(store_parent, store.getStoreParent().getStoreParentId());
                    if (store.getFlyer() != null)
                        ih.bind(store_flyer, store.getFlyer().getFlyerId());
                    if (store.getStoreAddress() != null)
                        ih.bind(store_addr, store.getStoreAddress());
                    if (store.getStoreLatitude() != null)
                        ih.bind(store_lat, store.getStoreLatitude());
                    if (store.getStoreLongitude() != null)
                        ih.bind(store_lng, store.getStoreLongitude());

                    ih.execute();

                    if (++previousIncrement == windowLength) {
                        previousIncrement = 0;
                        updateProgressBar(1);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                ih.close();
            }
        }
        Log.i(GroceryApplication.TAG, "refreshing store...DONE");
    }

    private void refreshFlyer(SQLiteDatabase db) {
        Log.i(GroceryApplication.TAG, "refreshing flyer...");

        Gson gson = new Gson();
        JsonArray flyerArray = jsonParser.getJSONFromUrl(ServerURLs.getFlyerUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, FlyerTable.TABLE_FLYER);
        Flyer flyer;

        if (flyerArray != null) {
            try {
                db.beginTransaction();

                int previousIncrement = 0;
                int windowLength = flyerArray.size() / 10;
                if (windowLength == 0)
                    windowLength = 1;

                int flyer_id = ih.getColumnIndex(FlyerTable.COLUMN_FLYER_ID);
                int flyer_url = ih.getColumnIndex(FlyerTable.COLUMN_FLYER_URL);
                int flyer_store_parent = ih.getColumnIndex(FlyerTable.COLUMN_FLYER_STOREPARENT);

                for (JsonElement jsonElement : flyerArray) {
                    flyer = gson.fromJson(jsonElement, Flyer.class);

                    ih.prepareForInsert();
                    ih.bind(flyer_id, flyer.getFlyerId());
                    ih.bind(flyer_url, flyer.getUrl());
                    ih.bind(flyer_store_parent, flyer.getStoreParent().getStoreParentId());
                    ih.execute();

                    if (++previousIncrement == windowLength) {
                        previousIncrement = 0;
                        updateProgressBar(1);
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                ih.close();
            }
        }

        Log.i(GroceryApplication.TAG, "refreshing flyer...DONE");
    }

    private int addNewGroceries(JsonReader groceryReader, SQLiteDatabase db) {
        //TODO: hard coded date format!! not good...
        Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, GroceryTable.TABLE_GROCERY);
        Grocery[] groceries = gson.fromJson(groceryReader, Grocery[].class);

        int maxGroceryIdBefore = GroceryGoUtils.getMaxGroceryId(this);
        try {
            db.beginTransaction();

            int previousIncrement = 0;
            int numGrocery = groceries.length;
            int windowLength = numGrocery / 50;
            if (windowLength == 0)
                windowLength = 1;

            Log.i("GroceryOTG", "Number of grocery available: " + numGrocery);

            int grocery_id = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_ID);
            int grocery_name = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME);
            int grocery_price = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE);
            int grocery_category = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_CATEGORY);
            int grocery_expiry = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_EXPIRY);
            int grocery_score = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_SCORE);
            int grocery_flyer = ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_FLYER);

            for (int i = 0; i < numGrocery; i++) {
                if (stopped) {
                    Log.i(GroceryApplication.TAG, "intent serverice stopped");
                    break;
                }

                ih.prepareForInsert();
                ih.bind(grocery_id, groceries[i].getGroceryId());
                ih.bind(grocery_name, groceries[i].getRawString());
                if (groceries[i].getTotalPrice() != null)
                    ih.bind(grocery_price, groceries[i].getTotalPrice());
                if (groceries[i].getCategoryId() != null)
                    ih.bind(grocery_category, groceries[i].getCategoryId());
                if (groceries[i].getEndDate() != null)
                    ih.bind(grocery_expiry, groceries[i].getEndDate().getTime());
                if (groceries[i].getScore() != null)
                    ih.bind(grocery_score, groceries[i].getScore());
                ih.bind(grocery_flyer, groceries[i].getFlyer().getFlyerId());
                ih.execute();

                if (++previousIncrement == windowLength) {
                    previousIncrement = 0;
                    updateProgressBar(1);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            ih.close();
        }

        return maxGroceryIdBefore;
    }

    private void removeExpiredGroceries(int maxGroceryIdBefore) {
        String selection = GroceryTable.COLUMN_GROCERY_ID + " <= '" + maxGroceryIdBefore + "'";
        getContentResolver().delete(GroceryotgProvider.CONTENT_URI_GRO, selection, null);
    }

    private String buildGroceryURL(String[] args) {
        StringBuilder url = new StringBuilder();
        url.append(ServerURLs.getGroceryBaseUrl());
        for (String arg : args)
            url.append(arg);
        return url.toString();
    }

    private void updateProgressBar(int inc) {
        Intent intent = new Intent();
        intent.setAction(SplashScreenActivity.BROADCAST_ACTION_UPDATE_PROGRESS);
        intent.putExtra(SplashScreenActivity.BROADCAST_ACTION_UPDATE_PROGRESS_INCREMENT, inc);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
