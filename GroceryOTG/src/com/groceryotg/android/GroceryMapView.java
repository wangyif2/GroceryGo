package com.groceryotg.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.utils.GroceryOTGUtils;

//import android.support.v4.app.FragmentActivity;

/**
 * User: robert
 * Date: 06/02/13
 */
public class GroceryMapView extends SherlockFragmentActivity {
    public static final int CAM_ZOOM = 14;
    public static final String MAP_FRAGMENT_TAG = "map_fragment_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment fragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, MAP_FRAGMENT_TAG).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        super.onCreateView(name, context, attrs);

        LatLng lastKnownLocation = getLastKnownLocation();
        Cursor storeLocations = GroceryOTGUtils.getStoreLocations(context);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (fragment != null) {
            GoogleMap map = fragment.getMap();
            if (map != null) {
                // move the camera to the current location
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, CAM_ZOOM), 2000, null);

                // add a marker at the current location
                buildMarker(map, getString(R.string.map_usermarker), lastKnownLocation);

                buildStoreMarkers(storeLocations, map);
            }
        }
        return null;
    }

    private void buildStoreMarkers(Cursor storeLocations, GoogleMap map) {
        storeLocations.moveToFirst();
        int storeNum = storeLocations.getColumnCount();
        while (!storeLocations.isAfterLast()) {
            for (int i = 0; i < storeNum; i++) {
                String storeName = storeLocations.getString(storeLocations.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
                double storeLat = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE));
                double storeLng = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE));
                LatLng storeLatLng = new LatLng(storeLat, storeLng);

                buildMarker(map, storeName, storeLatLng);
            }
            storeLocations.moveToNext();
        }
    }

    private void buildMarker(GoogleMap map, String storeName, LatLng storeLatLng) {
        BitmapDescriptor markerImage;

        //TODO: needs refactor
//        TypedArray storeArray = getResources().obtainTypedArray(R.array.store_names);
//        int markerImageName;
        if (storeName.equals("foodbasics")) {
            markerImage = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker_foodbasics);
        } else if (storeName.equals("nofrills")) {
            markerImage = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker_nofrills);
        } else if (storeName.equals("metro")) {
            markerImage = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker_metro);
        } else if (storeName.equals("loblaws")) {
            markerImage = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker_loblaws);
        } else if (storeName.equals("sobeys")) {
            markerImage = BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker_sobeys);
        } else {
            markerImage = null;
        }

        if (markerImage == null) {
            map.addMarker(new MarkerOptions()
                    .position(storeLatLng)
                    .title(storeName)
                    .draggable(false));
        } else {
            map.addMarker(new MarkerOptions()
                    .position(storeLatLng)
                    .title(storeName)
                    .draggable(false)
                    .icon(markerImage));
        }
    }

    private LatLng getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.

                // Specify the parent activity
                Intent parentActivityIntent = new Intent(this, GroceryFragmentActivity.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
