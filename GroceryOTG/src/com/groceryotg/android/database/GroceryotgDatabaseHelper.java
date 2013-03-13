package com.groceryotg.android.database;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groceryotg.android.CategoryOverView;
import com.groceryotg.android.database.objects.Category;
import com.groceryotg.android.database.objects.Grocery;
import com.groceryotg.android.database.objects.Store;
import com.groceryotg.android.database.objects.StoreParent;
import com.groceryotg.android.database.objects.Flyer;
import com.groceryotg.android.services.ServerURL;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * User: robert
 * Date: 07/02/13
 */
public class GroceryotgDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "groceryotg.db";
    private static final int DATABASE_VERSION = 1;

    JSONParser jsonParser = new JSONParser();


    public GroceryotgDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CategoryTable.onCreate(db);
        GroceryTable.onCreate(db);
        StoreParentTable.onCreate(db);
        FlyerTable.onCreate(db);
        StoreTable.onCreate(db);
        CartTable.onCreate(db);

        init(db);
    }

    private void init(SQLiteDatabase db) {
        if (ServerURL.checkNetworkStatus(CategoryOverView.getContext())) {
            initCategory(db);
            initGrocery(db);
            initStoreParent(db);
            initFlyer(db);
            initStore(db);
        }
    }

    private void initGrocery(SQLiteDatabase db) {
        Gson gson = new GsonBuilder().setDateFormat("MMM dd, yyyy").create();
        String date = ServerURL.getDateNowAsArg();
//        this is here for testing purposes
//        String date = "?date=2012-01-01";

        JSONArray groceryArray = jsonParser.getJSONFromUrl(ServerURL.getGroceryBaseUrl() + date);
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, GroceryTable.TABLE_GROCERY);
        Grocery grocery;

        if (groceryArray != null) {
            try {
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
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    private void initStoreParent(SQLiteDatabase db) {
    	Gson gson = new Gson();
    	JSONArray storeParentArray = jsonParser.getJSONFromUrl(ServerURL.getStoreParentUrl());
    	DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreParentTable.TABLE_STORE_PARENT);
    	StoreParent storeParent;
    	
    	if (storeParentArray != null) {
    		try {
    			for (int i = 0; i < storeParentArray.length(); i++) {
    				storeParent = gson.fromJson(storeParentArray.getJSONObject(i).toString(), StoreParent.class);
    				
                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID), storeParent.getStoreParentId());
                    ih.bind(ih.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME), storeParent.getName());
                    ih.execute();
                }
    		} catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
    	}
    }
    
    private void initFlyer(SQLiteDatabase db) {
    	Gson gson = new Gson();
    	JSONArray flyerArray = jsonParser.getJSONFromUrl(ServerURL.getFlyerUrl());
    	DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, FlyerTable.TABLE_FLYER);
    	Flyer flyer;
    	
    	if (flyerArray != null) {
    		try {
    			for (int i = 0; i < flyerArray.length(); i++) {
    				flyer = gson.fromJson(flyerArray.getJSONObject(i).toString(), Flyer.class);
    				
                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_ID), flyer.getFlyerId());
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_URL), flyer.getUrl());
                    ih.bind(ih.getColumnIndex(FlyerTable.COLUMN_FLYER_STOREPARENT), flyer.getStoreParent().getStoreParentId());
                    ih.execute();
                }
    		} catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
    	}
    }
    
    private void initStore(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray storeArray = jsonParser.getJSONFromUrl(ServerURL.getStoreUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, StoreTable.TABLE_STORE);
        Store store;

        if (storeArray != null) {
            try {
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
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }
    
    private void initCategory(SQLiteDatabase db) {
        Gson gson = new Gson();
        JSONArray categoryArray = jsonParser.getJSONFromUrl(ServerURL.getCateoryUrl());
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(db, CategoryTable.TABLE_CATEGORY);
        Category category;

        if (categoryArray != null) {
            try {
                for (int i = 0; i < categoryArray.length(); i++) {
                    category = gson.fromJson(categoryArray.getJSONObject(i).toString(), Category.class);

                    ih.prepareForInsert();
                    ih.bind(ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_ID), category.getCategoryId());
                    ih.bind(ih.getColumnIndex(CategoryTable.COLUMN_CATEGORY_NAME), category.getCategoryName());

                    ih.execute();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                ih.close();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CategoryTable.onUpgrade(db, oldVersion, newVersion);
        GroceryTable.onUpgrade(db, oldVersion, newVersion);
        StoreParentTable.onUpgrade(db, oldVersion, newVersion);
        FlyerTable.onUpgrade(db, oldVersion, newVersion);
        StoreTable.onUpgrade(db, oldVersion, newVersion);
    }
}
