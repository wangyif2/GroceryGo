package com.groceryotg.android;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.groceryotg.android.database.StoreParentTable;
import com.groceryotg.android.utils.GroceryOTGUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.util.SparseArray;

public class GroceryApplication extends Application {
	
	private SparseArray<Float> mStoreDistanceMap;
	private Map<String, Integer> mMapIconMap = new HashMap<String, Integer>();
	private Map<String, Integer> mStoreParentIconMap = new HashMap<String, Integer>();
	
	public void constructGlobals(final Context context) {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		Thread t;
		
		// Calculates store distances
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				mStoreDistanceMap = GroceryOTGUtils.buildDistanceMap(context);
			}
		});
		threads.add(t);
		t.start();
		
		// Calculates icons for stores on the map
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor parents = GroceryOTGUtils.getStoreParentNamesCursor(context);
				parents.moveToFirst();
				while (!parents.isAfterLast()) {
					String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
					int markerImageID = context.getResources().getIdentifier("ic_mapmarker_" + name.toLowerCase(Locale.CANADA).replace(" ", ""), "drawable", context.getPackageName());
					if (markerImageID != 0) {
						mMapIconMap.put(name, markerImageID);
					}
					parents.moveToNext();
				}
			}
		});
		threads.add(t);
		t.start();
		
		// Calculates icons for stores in the grocery list
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor parents = GroceryOTGUtils.getStoreParentNamesCursor(context);
				parents.moveToFirst();
				while (!parents.isAfterLast()) {
					String name = parents.getString(parents.getColumnIndex(StoreParentTable.COLUMN_STORE_PARENT_NAME));
					int storeIconID = context.getResources().getIdentifier("ic_store_" + name.toLowerCase(Locale.CANADA).replace(" ", ""), "drawable", context.getPackageName());
					if (storeIconID != 0) {
						mStoreParentIconMap.put(name, storeIconID);
					}
					parents.moveToNext();
				}
			}
		});
		threads.add(t);
		t.start();
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the storeDistanceMap
	 */
	public SparseArray<Float> getStoreDistanceMap() {
		return mStoreDistanceMap;
	}

	/**
	 * @return the mapIconMap
	 */
	public Map<String, Integer> getMapIconMap() {
		return mMapIconMap;
	}
	
	/**
	 * @return the storeParentIconMap
	 */
	public Map<String, Integer> getStoreParentIconMap() {
		return mStoreParentIconMap;
	}
}
