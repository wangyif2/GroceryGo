package com.groceryotg.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * User: robert
 * Date: 06/02/13
 */
public class GroceryMapView extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SupportMapFragment fragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
    }

}
