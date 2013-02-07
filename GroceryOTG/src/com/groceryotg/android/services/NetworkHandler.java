package com.groceryotg.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.gson.Gson;
import com.groceryotg.android.database.objects.Category;
import com.groceryotg.android.utils.JSONParser;
import org.json.JSONArray;
import org.json.JSONException;

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
        Log.i("GroceryOTG", "in intent service");
        JSONParser categoryJSON = new JSONParser();
        JSONArray categoryArray = categoryJSON.getJSONFromUrl(getCategory);
        Category category = null;
        try {
            category = (Category) gson.fromJson(categoryArray.getJSONObject(0).toString(), Category.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("GroceryOTG", category.toString());
    }

}
