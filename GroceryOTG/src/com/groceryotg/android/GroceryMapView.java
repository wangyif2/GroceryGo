package com.groceryotg.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.GroceryFragmentActivity;
import com.groceryotg.android.utils.GroceryOTGUtils;

/**
 * User: robert
 * Date: 06/02/13
 */
public class GroceryMapView extends SherlockFragmentActivity {
    public static final int CAM_ZOOM = 14;
    public static final String MAP_FRAGMENT_TAG = "map_fragment_tag";
    
    private GoogleMap mMap = null;
    private Map<String, Integer> mIconMap = new HashMap<String, Integer>();
    private ArrayList<Marker> mMapMarkers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable ancestral navigation ("Up" button in ActionBar) for Android < 4.1
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        boolean isGooglePlaySuccess = checkGooglePlayService();
        
        if (isGooglePlaySuccess) {
	        // Set map options
	        GoogleMapOptions options = new GoogleMapOptions();
	        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
	        	.compassEnabled(true)
	        	.zoomControlsEnabled(false)
	        	.rotateGesturesEnabled(false)
	        	.tiltGesturesEnabled(false);
	        
	        SupportMapFragment fragment = SupportMapFragment.newInstance(options);
	        getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, MAP_FRAGMENT_TAG).commit();
	        getSupportFragmentManager().executePendingTransactions();
        }
    }
    
    private boolean checkGooglePlayService() {
    	int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext());
        if (errorCode != ConnectionResult.SUCCESS) {
        	Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, -1);
        	if (errorDialog != null) {
        		errorDialog.show();
        		return false;
        	}
        }
        return true;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        super.onCreateView(name, context, attrs);
        
        buildIconMap(context);

        Location lastKnownLocation = getLastKnownLocation();
        Cursor storeLocations = GroceryOTGUtils.getStoreLocations(context);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (fragment != null) {
            mMap = fragment.getMap();
            if (mMap != null) {
                // move the camera to the current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), CAM_ZOOM));
                
                mMap.setOnCameraChangeListener(getCameraChangeListener());

                // add a marker at the current location
                buildUserMarker(context, mMap, getString(R.string.map_usermarker), lastKnownLocation);

                buildStoreMarkers(context, storeLocations, mMap);
            }
        }
        return null;
    }
    
    private void buildIconMap(Context context) {
    	Cursor parents = GroceryOTGUtils.getStoreParentNames(context);
    	parents.moveToFirst();
    	while (!parents.isAfterLast()) {
    		String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
    		int markerImageID = context.getResources().getIdentifier("ic_mapmarker_" + name, "drawable", getPackageName());
    		if (markerImageID != 0) {
    			mIconMap.put(name, markerImageID);
    		}
    		parents.moveToNext();
    	}
    }

    private void buildUserMarker(Context context, GoogleMap map, String str, Location loc) {
    	LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
        map.addMarker(new MarkerOptions()
                .position(ll)
                .title(str)
                .draggable(false)
                .visible(true));
        
		CircleOptions circleOptions = new CircleOptions()
			.center(ll)
			.radius(loc.getAccuracy())
			.fillColor(0x100000FF)
			.strokeColor(0xFF0000FF)
			.strokeWidth(2);
		
		mMap.addCircle(circleOptions);
    }

    private void buildStoreMarkers(Context context, Cursor storeLocations, GoogleMap map) {
        storeLocations.moveToFirst();
        int storeNum = storeLocations.getColumnCount();
        while (!storeLocations.isAfterLast()) {
            for (int i = 0; i < storeNum; i++) {
                String storeName = storeLocations.getString(storeLocations.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
                double storeLat = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE));
                double storeLng = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE));
                LatLng storeLatLng = new LatLng(storeLat, storeLng);

                buildStoreMarker(context, map, storeName, storeLatLng);
            }
            storeLocations.moveToNext();
        }
    }

    private void buildStoreMarker(Context context, GoogleMap map, String storeName, LatLng storeLatLng) {
    	MarkerOptions markerOptions = new MarkerOptions()
        	.position(storeLatLng)
        	.title(storeName)
        	.draggable(false)
        	.visible(false);
        
        if (mIconMap.containsKey(storeName)) {
            markerOptions = markerOptions.icon(BitmapDescriptorFactory.fromResource(mIconMap.get(storeName)));
        }
        Marker marker = map.addMarker(markerOptions);
        mMapMarkers.add(marker);
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return loc;
    }

    private OnCameraChangeListener getCameraChangeListener() {
    	return new OnCameraChangeListener() {
    		@Override
    		public void onCameraChange(CameraPosition position) {
    			showItemsOnMap();
    		}
    	};
    }

    private void showItemsOnMap() {
    	if(this.mMap != null) {
    		LatLngBounds bounds = this.mMap.getProjection().getVisibleRegion().latLngBounds;
    		
    		for(Marker marker : this.mMapMarkers) {
    			LatLng pos = marker.getPosition();
    			if(bounds.contains(new LatLng(pos.latitude, pos.longitude))) {
    				if (!marker.isVisible()) {
    					marker.setVisible(true);
    				}
    			} else {
    				if (marker.isVisible()) {
    					marker.setVisible(false);
    				}
    			}
    		}
    	}
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
