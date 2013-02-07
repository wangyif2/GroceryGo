package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.groceryotg.android.database.CategoryTable;
import com.groceryotg.android.database.contentprovider.GroceryotgProvider;
import com.groceryotg.android.database.objects.Category;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * User: robert
 * Date: 06/02/13
 */
public class NetworkHandler extends IntentService {
    private final String getCategory = "http://groceryotg.elasticbeanstalk.com/GetGeneralInfo";
    Gson gson = new Gson();

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        refreshCategory();
    }

    private void refreshCategory() {
        Log.i("GroceryOTG", "in intent service");
        JSONParser categoryJSON = new JSONParser();
        JSONArray categoryArray = categoryJSON.getJSONFromUrl(getCategory);
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<ContentValues>();

        try {
            for (int i = 0; i < categoryArray.length(); i++){
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

        getContentResolver().bulkInsert(GroceryotgProvider.CONTENT_URI, categories);
    }
}
