package ca.grocerygo.android;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.util.SparseArray;
import ca.grocerygo.android.utils.GroceryOTGUtils;
import ca.grocerygo.android.database.StoreParentTable;
import ca.grocerygo.android.database.StoreTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GroceryApplication extends Application {

    public static final String TAG = "GroceryGo";
	
	private SparseArray<Float> mStoreDistanceMap;
	private Map<String, Integer> mMapIconMap = new HashMap<String, Integer>();
	private Map<String, Integer> mStoreParentIconMap = new HashMap<String, Integer>();
	private SparseArray<ArrayList<Integer>> mFlyerStoreMap = new SparseArray<ArrayList<Integer>>();
	private SparseArray<ArrayList<Integer>> mStoreParentStoreMap = new SparseArray<ArrayList<Integer>>();
	
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
		
		// Calculates mapping between flyer and stores, and 
		// mapping between storeParents and stores
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				
				Cursor storeIDs = GroceryOTGUtils.getStoreIDs(context);
				
				storeIDs.moveToFirst();
				while (!storeIDs.isAfterLast()) {
					int flyerId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_FLYER));
					int storeId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_ID));
					int storeParentId = storeIDs.getInt(storeIDs.getColumnIndex(StoreTable.COLUMN_STORE_PARENT));
					
					// Be careful: some stores have NULL flyers which gets translated to flyerId=0 here
					if (flyerId > 0) {
						if (mFlyerStoreMap.get(flyerId) == null)
							mFlyerStoreMap.put(flyerId, new ArrayList<Integer>());
						ArrayList<Integer> n = mFlyerStoreMap.get(flyerId);
						n.add(storeId);
						mFlyerStoreMap.put(flyerId, n);
						
						if (mStoreParentStoreMap.get(storeParentId) == null)
							mStoreParentStoreMap.put(storeParentId, new ArrayList<Integer>());
						// Now append the new value onto the end of the appropriate list
						ArrayList<Integer> m = mStoreParentStoreMap.get(storeParentId);
						m.add(storeId);
						mStoreParentStoreMap.put(storeParentId, m);
					}
					
					storeIDs.moveToNext();
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
	
	/**
	 * @return the flyerStoreMap
	 */
	public SparseArray<ArrayList<Integer>> getFlyerStoreMap() {
		return mFlyerStoreMap;
	}
	
	/**
	 * @return the storeParentStoreMap
	 */
	public SparseArray<ArrayList<Integer>> getStoreParentStoreMap() {
		return mStoreParentStoreMap;
	}
}
