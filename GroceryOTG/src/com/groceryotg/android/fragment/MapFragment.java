package com.groceryotg.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.groceryotg.android.MapFragmentActivity;
import com.groceryotg.android.R;
import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.database.StoreTable;
import com.groceryotg.android.utils.GroceryOTGUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends SupportMapFragment {
    private Context mContext;
    
    private GoogleMap mMap = null;
    private Map<String, Integer> mIconMap = new HashMap<String, Integer>();
    private ArrayList<Marker> mMapMarkers = new ArrayList<Marker>();
    
    private ArrayList<Integer> filterStoreParents = null;
    private ArrayList<Integer> filterStores = null;

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	this.mContext = activity;
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle args = ((Activity) mContext).getIntent().getExtras();
        if (args != null) {
	        this.filterStoreParents = args.getIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE_PARENT);
	        this.filterStores = args.getIntegerArrayList(MapFragmentActivity.EXTRA_FILTER_STORE);
        }
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
	    buildIconMap(mContext);
	    Location lastKnownLocation = GroceryOTGUtils.getLastKnownLocation(mContext);
	    Cursor storeLocations = GroceryOTGUtils.getFilteredStores(mContext).loadInBackground();
	    
	    mMap = this.getMap();
	    if (mMap != null) {
	        mMap.setOnCameraChangeListener(getCameraChangeListener());
	
	        if (lastKnownLocation != null) {
	            // add a marker at the current location
	            buildUserMarker(mContext, mMap, getString(R.string.map_usermarker), lastKnownLocation);
	            // move the camera to the current location
	            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), MapFragmentActivity.CAM_ZOOM));
	        }
	
	        buildStoreMarkers(mContext, storeLocations, mMap);
	    }
    }
    
    private void buildIconMap(Context context) {
    	Cursor parents = GroceryOTGUtils.getStoreParentNamesCursor(context);
    	parents.moveToFirst();
    	while (!parents.isAfterLast()) {
    		String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
    		int markerImageID = context.getResources().getIdentifier("ic_mapmarker_" + name.toLowerCase(Locale.CANADA).replace(" ", ""), "drawable", mContext.getPackageName());
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
        while (!storeLocations.isAfterLast()) {
        	int storeID = storeLocations.getInt(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_ID));
        	int storeParentID = storeLocations.getInt(storeLocations.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_ID));
            String storeName = storeLocations.getString(storeLocations.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
            String storeAdr = storeLocations.getString(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_ADDR));
            double storeLat = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LATITUDE));
            double storeLng = storeLocations.getDouble(storeLocations.getColumnIndex(StoreTable.COLUMN_STORE_LONGITUDE));
            
            LatLng storeLatLng = new LatLng(storeLat, storeLng);
            
            // Now do filtering
            boolean isIncluded = true;
            if (this.filterStoreParents != null) {
            	isIncluded = false;
            	if (this.filterStoreParents.contains(storeParentID))
            		isIncluded = true;
            }
            if (this.filterStores != null) {
            	isIncluded = false;
            	if (this.filterStores.contains(storeID))
            		isIncluded = true;
            }
            
            if (isIncluded) {
            	buildStoreMarker(context, map, storeName, storeAdr, storeLatLng);
            }
            
            storeLocations.moveToNext();
        }
    }

    private void buildStoreMarker(Context context, GoogleMap map, String storeName, String storeAdr, LatLng storeLatLng) {
    	MarkerOptions markerOptions = new MarkerOptions()
        	.position(storeLatLng)
        	.title(storeName)
        	.snippet(storeAdr)
        	.draggable(false)
        	.visible(false);
        
        if (mIconMap.containsKey(storeName)) {
            markerOptions = markerOptions.icon(BitmapDescriptorFactory.fromResource(mIconMap.get(storeName)));
        }
        Marker marker = map.addMarker(markerOptions);
        mMapMarkers.add(marker);
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
}
