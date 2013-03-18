package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groceryotg.android.SplashScreen;
import com.groceryotg.android.database.*;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.database.objects.*;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
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
    public static final int STO_PAR = 30;
    public static final int STO = 40;
    public static final int FLY = 50;

    public static final String REQUEST_TYPE = "refresh_type";

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

        if (ServerURL.checkNetworkStatus(this.getBaseContext()) && extras != null) {
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
        } else if (!ServerURL.checkNetworkStatus(this.getBaseContext())) {
            connectionState = NO_CONNECTION;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(CONNECTION_STATE, connectionState);
        bundle.putInt(REQUEST_TYPE, requestType);
        Intent localIntent = new Intent(REFRESH_COMPLETED_ACTION).putExtra("bundle",bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void refreshCategory(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray categoryArray = jsonParser.getJSONFromUrl(ServerURL.getCateoryUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, CategoryTable.TABLE_CATEGORY);
        Category category;

        if (categoryArray != null) {
            try {
            	int previousIncrement = 0;
            	int windowLength = categoryArray.length()/10;
            	if (windowLength == 0)
            		windowLength = 1;
                for (int i = 0; i < categoryArray.length(); i++) {
                    category = gson.fromJson(categoryArray.getJSONObject(i).toString(), Category.class);

                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_ID), category.getCategoryId());
                    ih.bind(ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_NAME), category.getCategoryName());

                    ih.execute();
                    
                    if (++previousIncrement == windowLength) {
                    	previousIncrement = 0;
                    	SplashScreen.incrementProgressBar(1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    private void refreshGrocery(SQLiteDatabase db) {
//        this is here for testing purposes
//        String date = "?date=2013-03-13";
        String date = ServerURL.getDateNowAsArg();
        String[] requestArgs = new String[]{date};
        String getGrocery = buildGroceryURL(requestArgs);

        JSONArray groceryArray = jsonParser.getJSONFromUrl(getGrocery);

        if (groceryArray != null) {
            addNewGroceries(groceryArray, db);
//            removeExpiredGroceries();
        }
    }

    private void refreshStoreParent(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray storeParentArray = jsonParser.getJSONFromUrl(ServerURL.getStoreParentUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreParentTable.TABLE_STORE_PARENT);
        StoreParent storeParent;

        if (storeParentArray != null) {
            try {
            	int previousIncrement = 0;
            	int windowLength = storeParentArray.length()/10;
            	if (windowLength == 0)
            		windowLength = 1;
                for (int i = 0; i < storeParentArray.length(); i++) {
                    storeParent = gson.fromJson(storeParentArray.getJSONObject(i).toString(), StoreParent.class);

                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID), storeParent.getStoreParentId());
                    ih.bind(ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME), storeParent.getName());
                    ih.execute();
                    
                    if (++previousIncrement == windowLength) {
                    	previousIncrement = 0;
                    	SplashScreen.incrementProgressBar(1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    private void refreshStore(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray storeArray = jsonParser.getJSONFromUrl(ServerURL.getStoreUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreTable.TABLE_STORE);
        Store store;

        if (storeArray != null) {
            try {
            	int previousIncrement = 0;
            	int windowLength = storeArray.length()/10;
            	if (windowLength == 0)
            		windowLength = 1;
                for (int i = 0; i < storeArray.length(); i++) {
                    store = gson.fromJson(storeArray.getJSONObject(i).toString(), Store.class);

                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_ID), store.getStoreId());
                    ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_PARENT), store.getStoreParent().getStoreParentId());
                    ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_FLYER), store.getFlyer().getFlyerId());
                    if (store.getStoreAddress() != null)
                        ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_ADDR), store.getStoreAddress());
                    if (store.getStoreLatitude() != null)
                        ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE), store.getStoreLatitude());
                    if (store.getStoreLongitude() != null)
                        ih.bind(ih.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE), store.getStoreLongitude());

                    ih.execute();
                    
                    if (++previousIncrement == windowLength) {
                    	previousIncrement = 0;
                    	SplashScreen.incrementProgressBar(1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    private void refreshFlyer(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray flyerArray = jsonParser.getJSONFromUrl(ServerURL.getFlyerUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, FlyerTable.TABLE_FLYER);
        Flyer flyer;

        if (flyerArray != null) {
            try {
            	int previousIncrement = 0;
            	int windowLength = flyerArray.length()/10;
            	if (windowLength == 0)
            		windowLength = 1;
                for (int i = 0; i < flyerArray.length(); i++) {
                    flyer = gson.fromJson(flyerArray.getJSONObject(i).toString(), Flyer.class);

                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_ID), flyer.getFlyerId());
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_URL), flyer.getUrl());
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_STOREPARENT), flyer.getStoreParent().getStoreParentId());
                    ih.execute();
                    
                    if (++previousIncrement == windowLength) {
                    	previousIncrement = 0;
                    	SplashScreen.incrementProgressBar(1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    private void addNewGroceries(JSONArray groceryArray, SQLiteDatabase db) {
        //TODO: hard coded date format!! not good...
        Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, GroceryTable.TABLE_GROCERY);
        Grocery grocery;
        try {
        	int previousIncrement = 0;
        	int windowLength = groceryArray.length()/50;
        	if (windowLength == 0)
        		windowLength = 1;
            for (int i = 0; i < groceryArray.length(); i++) {
                grocery = gson.fromJson(groceryArray.getJSONObject(i).toString(), Grocery.class);

                ih.prepareForInsert();
                ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_ID), grocery.getGroceryId());
                ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_NAME), grocery.getRawString());
                if (grocery.getTotalPrice() != null)
                    ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_PRICE), grocery.getTotalPrice());
                if (grocery.getCategoryId() != null)
                    ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_CATEGORY), grocery.getCategoryId());
                if (grocery.getEndDate() != null)
                    ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_EXPIRY), grocery.getEndDate().getTime());
                ih.bind(ih.getColumnIndex(GroceryTable.COLUMN_GROCERY_FLYER), grocery.getFlyer().getFlyerId());
                ih.execute();
                
                if (++previousIncrement == windowLength) {
                	previousIncrement = 0;
                	SplashScreen.incrementProgressBar(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            ih.close();
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

    private String buildGroceryURL(String[] args) {
        StringBuilder url = new StringBuilder();
        url.append(ServerURL.getGroceryBaseUrl());
        for (String arg : args)
            url.append(arg);
        return url.toString();
    }


}
