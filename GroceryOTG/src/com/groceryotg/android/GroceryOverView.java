package com.groceryotg.android;

import android.app.Activity;
import android.os.Bundle;

public class GroceryOverView extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

//    ContentValues values = new ContentValues();
//    JSONArray projects = new JSONArray(_webService.getStringResponse());
//    int counter = 0;
//    int projectSize = projects.length();
//
//    while(projectCount < projectSize) {
//        JSONObject projectData = projects.getJSONObject(projectCount);
//
//        values.put(DbProperties.PROJECT_ID, projectData.getString("pid"));
//        values.put(DbProperties.CUSTOMER_ID, projectData.getString("cid"));
//        values.put(DbProperties.SETTLEMENT, projectData.getString("settl"));
//        //Do some database saving for example
//
//        counter++;
//    }

}
