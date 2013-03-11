package com.groceryotg.android;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
//import android.support.v4.app.FragmentActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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
    	
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng lastLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (fragment != null) {
	        GoogleMap map = fragment.getMap();
	        if (map != null)
	        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, CAM_ZOOM));
        }
		return null;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// This is called when the Home (Up) button is pressed
                // in the Action Bar. This handles Android < 4.1.
            	
            	// Specify the parent activity
            	Intent parentActivityIntent = new Intent(this, CategoryOverView.class);
            	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
            								Intent.FLAG_ACTIVITY_NEW_TASK);
            	startActivity(parentActivityIntent);
            	finish();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
