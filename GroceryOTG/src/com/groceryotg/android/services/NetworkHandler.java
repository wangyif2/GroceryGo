package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.database.objects.Category;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private final String getCategory = "http://groceryotg.elasticbeanstalk.com/GetGeneralInfo";
    private final String getGrocery = "http://groceryotg.elasticbeanstalk.com/UpdateGroceryInfo?date=";

    Gson gson = new Gson();

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null) {
            Integer requestType = (Integer) extras.get(REFRESH_CONTENT);
            switch (requestType) {
                case CAT:
                    refreshCategory();
                    break;
                case GRO:
                    refreshGrocery();
                    break;
                default:
                    Log.e("GroceryOTG", "unknown request received by NetworkHandler");
                    break;
            }
        }
    }

    private void refreshGrocery() {
        StringBuilder url = new StringBuilder();
        url.append(getGrocery);

        Date now = new Date();
        Log.i("GroceryOTG", "freshGrocery with date: " + format.format(now));
        url.append(format.format(now));


    }

    private void refreshCategory() {
        Log.d("GroceryOTG", "in intent service");
        JSONParser categoryJSON = new JSONParser();
        JSONArray categoryArray = categoryJSON.getJSONFromUrl(getCategory);
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

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
