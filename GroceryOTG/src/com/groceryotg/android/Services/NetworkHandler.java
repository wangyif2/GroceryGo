package com.groceryotg.android.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.groceryotg.android.Utils.JSONParser;
import org.json.JSONArray;

/**
 * User: robert
 * Date: 06/02/13
 */
public class NetworkHandler extends IntentService {
    private final String getCategory = "http://groceryotg.elasticbeanstalk.com/GetGeneralInfo";

    public NetworkHandler() {
        super("NetworkHandler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("GroceryOTG", "in intent service");
        JSONParser categoryJSON = new JSONParser();
        JSONArray category = categoryJSON.getJSONFromUrl(getCategory);
        Log.i("GroceryOTG", category.toString());
    }

}
